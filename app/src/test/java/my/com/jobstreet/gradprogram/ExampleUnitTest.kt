package my.com.jobstreet.gradprogram

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @get:Rule
    val quarantineRule = QuarantineTestRule()
    
    @Category(SlowTests::class)
    @Quarantined(reason = "Test is flaky and needs investigation")
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}