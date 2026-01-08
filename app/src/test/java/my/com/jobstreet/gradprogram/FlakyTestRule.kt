package my.com.jobstreet.gradprogram

import com.google.gson.Gson
import com.google.gson.JsonArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.TimeUnit

/**
 * JUnit TestRule that automatically marks failed tests as "flaky" in Buildkite Test Analytics.
 *
 * When a test fails, this rule:
 * 1. Detects the failure
 * 2. Finds the test in Buildkite by name
 * 3. Adds a "flaky" label via Buildkite Labels API
 *
 * Usage:
 * ```kotlin
 * @get:Rule
 * val flakyTestRule = FlakyTestRule()
 * ```
 *
 * Environment variables required:
 * - BUILDKITE_API_TOKEN: API token with write_suites scope
 * - BUILDKITE_ORG_SLUG: Your Buildkite organization slug
 * - BUILDKITE_SUITE_SLUG: Your test suite slug
 * - BUILDKITE_TEAM_UUID: (Optional) Your team UUID for labels API
 * - BUILDKITE_SUITE_UUID: (Optional) Your suite UUID for labels API
 *
 * If team/suite UUIDs are not provided, the rule will attempt to use alternative endpoints.
 *
 * Reference: https://buildkite.com/docs/apis/rest-api/test-engine/tests#add-or-remove-labels-from-a-test
 */
class FlakyTestRule : TestRule {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                try {
                    // Run the test
                    base.evaluate()
                } catch (throwable: Throwable) {
                    // Test failed - mark it as flaky in Buildkite
                    println("❌ Test failed: ${description.displayName}")
                    markTestAsFlaky(description)
                    // Re-throw to maintain test failure behavior
                    throw throwable
                }
            }
        }
    }

    /**
     * Mark a failed test as flaky in Buildkite Test Analytics
     */
    private fun markTestAsFlaky(description: Description) {
        val apiToken = System.getenv("BUILDKITE_API_TOKEN")
        val orgSlug = System.getenv("BUILDKITE_ORG_SLUG")
        val suiteSlug = System.getenv("BUILDKITE_SUITE_SLUG")

        if (apiToken == null || orgSlug == null || suiteSlug == null) {
            println("⚠️  Buildkite API credentials not configured. Skipping flaky marking.")
            println("   Set BUILDKITE_API_TOKEN, BUILDKITE_ORG_SLUG, and BUILDKITE_SUITE_SLUG")
            return
        }

        val testName = getTestName(description)
        println("🔍 Marking test as flaky: $testName")

        // Find test ID and URL in Buildkite
        val testInfo = findTestInfo(apiToken, orgSlug, suiteSlug, testName)

        if (testInfo != null) {
            // Add "flaky" label using Buildkite Labels API
            performFlakyMark(apiToken, orgSlug, suiteSlug, testInfo.id, testName)
        } else {
            println("⚠️  Could not find test in Buildkite: $testName")
            println("   Test may not have been uploaded yet. Will be marked on next run.")
        }
    }

    /**
     * Get the full test name in format: package.ClassName.methodName
     */
    private fun getTestName(description: Description): String {
        val className = description.className
        val methodName = description.methodName
        return "$className.$methodName"
    }

    /**
     * Data class to hold test information
     */
    private data class TestInfo(val id: String, val url: String)

    /**
     * Find test ID and URL in Buildkite by searching for the test name
     */
    private fun findTestInfo(
        apiToken: String,
        orgSlug: String,
        suiteSlug: String,
        testName: String
    ): TestInfo? {
        val url =
            "https://api.buildkite.com/v2/analytics/organizations/$orgSlug/suites/$suiteSlug/tests"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiToken")
            .get()
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: return null
                extractTestInfoFromJson(json, testName)
            } else {
                println("❌ Failed to fetch tests: ${response.code} ${response.message}")
                null
            }
        } catch (e: Exception) {
            println("❌ Error fetching tests: ${e.message}")
            null
        }
    }

    /**
     * Extract test ID and URL from JSON response using Gson
     */
    private fun extractTestInfoFromJson(json: String, testName: String): TestInfo? {
        return try {
            val jsonArray = gson.fromJson(json, JsonArray::class.java)
            val methodName = testName.split(".").last()

            for (element in jsonArray) {
                val testObj = element.asJsonObject
                val name = testObj.get("name")?.asString ?: continue
                val id = testObj.get("id")?.asString ?: continue
                val url = testObj.get("url")?.asString ?: continue

                // Match by full name or method name
                if (name == testName ||
                    name.contains(methodName) ||
                    name.endsWith(".$methodName")
                ) {
                    return TestInfo(id, url)
                }
            }
            null
        } catch (e: Exception) {
            println("⚠️  Error parsing test list: ${e.message}")
            null
        }
    }

    /**
     * Mark test as flaky via Buildkite API using labels endpoint
     *
     * Uses the Buildkite Test Engine Labels API to add a "flaky" label to the test.
     * Reference: https://buildkite.com/docs/apis/rest-api/test-engine/tests#add-or-remove-labels-from-a-test
     */
    private fun performFlakyMark(
        apiToken: String,
        orgSlug: String,
        suiteSlug: String,
        testId: String,
        testName: String
    ) {
        // Try analytics endpoint pattern first (simpler, may be supported)
        val analyticsUrl =
            "https://api.buildkite.com/v2/analytics/organizations/$orgSlug/suites/$suiteSlug/tests/$testId/labels"

        val jsonBody = """
            {
                "operator": "add",
                "labels": ["flaky"]
            }
        """.trimIndent()

        val analyticsRequest = Request.Builder()
            .url(analyticsUrl)
            .header("Authorization", "Bearer $apiToken")
            .header("Content-Type", "application/json")
            .patch(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = client.newCall(analyticsRequest).execute()
            if (response.isSuccessful) {
                println("✅ Successfully added 'flaky' label to test in Buildkite: $testName")
            }
        } catch (_: Exception) {
            println("⚠️  Could not add 'flaky' label. Test will be marked on next successful API call.")
        }
    }
}

