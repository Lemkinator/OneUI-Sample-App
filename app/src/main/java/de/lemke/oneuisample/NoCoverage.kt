package de.lemke.oneuisample

/**
 * Excludes a declaration from Kover coverage instrumentation.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class NoCoverage
