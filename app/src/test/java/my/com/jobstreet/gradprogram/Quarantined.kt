package my.com.jobstreet.gradprogram

/**
 * Annotation to mark tests that should be quarantined/muted in Buildkite Test Analytics.
 * Tests annotated with @Quarantined will be automatically muted via Buildkite API.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Quarantined(
    val reason: String = ""
)

