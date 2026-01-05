package my.com.jobstreet.gradprogram

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.TimeUnit

/**
 * JUnit TestRule that automatically mutes tests annotated with @Quarantined
 * in Buildkite Test Analytics by calling the Buildkite API.
 * 
 * Usage:
 * ```kotlin
 * @get:Rule
 * val quarantineRule = QuarantineTestRule()
 * ```
 * 
 * Environment variables required:
 * - BUILDKITE_API_TOKEN: API token with write_suites scope
 * - BUILDKITE_ORG_SLUG: Your Buildkite organization slug
 * - BUILDKITE_SUITE_SLUG: Your test suite slug
 */
class QuarantineTestRule : TestRule {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                // Check if test or class has @Quarantined annotation
                val isQuarantined = description.getAnnotation(Quarantined::class.java) != null ||
                    description.testClass.getAnnotation(Quarantined::class.java) != null
                
                if (isQuarantined) {
                    val annotation = description.getAnnotation(Quarantined::class.java)
                        ?: description.testClass.getAnnotation(Quarantined::class.java)
                    
                    val reason = annotation?.reason ?: "Test is quarantined"
                    println("🔇 Quarantined test detected: ${description.displayName}")
                    println("   Reason: $reason")
                    
                    // Mute the test in Buildkite
                    muteTestInBuildkite(description)
                }
                
                // Run the test (or skip if you want to skip quarantined tests)
                base.evaluate()
            }
        }
    }
    
    /**
     * Mute a test in Buildkite Test Analytics via API
     */
    private fun muteTestInBuildkite(description: Description) {
        val apiToken = System.getenv("BUILDKITE_API_TOKEN")
        val orgSlug = System.getenv("BUILDKITE_ORG_SLUG")
        val suiteSlug = System.getenv("BUILDKITE_SUITE_SLUG")
        
        if (apiToken == null || orgSlug == null || suiteSlug == null) {
            println("⚠️  Buildkite API credentials not configured. Skipping mute.")
            println("   Set BUILDKITE_API_TOKEN, BUILDKITE_ORG_SLUG, and BUILDKITE_SUITE_SLUG")
            return
        }
        
        // Get test ID from Buildkite
        // First, we need to find the test by name
        val testName = getTestName(description)
        val testId = findTestId(apiToken, orgSlug, suiteSlug, testName)
        
        if (testId != null) {
            performMute(apiToken, orgSlug, suiteSlug, testId, testName)
        } else {
            println("⚠️  Could not find test in Buildkite: $testName")
            println("   Test may not have been uploaded yet. Will be muted on next run.")
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
     * Find test ID in Buildkite by searching for the test name
     */
    private fun findTestId(apiToken: String, orgSlug: String, suiteSlug: String, testName: String): String? {
        val url = "https://api.buildkite.com/v2/analytics/organizations/$orgSlug/suites/$suiteSlug/tests"
        
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiToken")
            .get()
            .build()
        
        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: return null
                // Parse JSON to find matching test
                // Simple JSON parsing - in production, use a proper JSON library
                val testId = extractTestIdFromJson(json, testName)
                testId
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
     * Extract test ID from JSON response using Gson
     */
    private fun extractTestIdFromJson(json: String, testName: String): String? {
        return try {
            val jsonArray = gson.fromJson(json, JsonArray::class.java)
            val methodName = testName.split(".").last()
            
            for (element in jsonArray) {
                val testObj = element.asJsonObject
                val name = testObj.get("name")?.asString ?: continue
                val id = testObj.get("id")?.asString ?: continue
                
                // Match by full name or method name
                if (name == testName || 
                    name.contains(methodName) || 
                    name.endsWith(".$methodName")) {
                    return id
                }
            }
            null
        } catch (e: Exception) {
            println("⚠️  Error parsing test list: ${e.message}")
            null
        }
    }
    
    /**
     * Perform the mute operation via Buildkite API
     */
    private fun performMute(apiToken: String, orgSlug: String, suiteSlug: String, testId: String, testName: String) {
        val url = "https://api.buildkite.com/v2/analytics/organizations/$orgSlug/suites/$suiteSlug/tests/$testId/mute"
        
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiToken")
            .put("".toRequestBody("application/json".toMediaType()))
            .build()
        
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                println("✅ Successfully muted test in Buildkite: $testName")
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                println("❌ Failed to mute test: ${response.code} $errorBody")
            }
        } catch (e: Exception) {
            println("❌ Error muting test: ${e.message}")
        }
    }
}

