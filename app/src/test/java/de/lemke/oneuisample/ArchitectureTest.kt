package de.lemke.oneuisample

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArchitectureTest {
    private val scope = Konsist.scopeFromProduction()

    @Test
    fun `data layer does not depend on ui`() {
        scope.files
            .withPackage("de.lemke.oneuisample.data..")
            .forEach { file ->
                assertTrue(
                    file.imports.none { it.name.contains("de.lemke.oneuisample.ui.") },
                    "Data file '${file.name}' must not import from ui layer",
                )
            }
    }

    @Test
    fun `domain layer does not depend on ui`() {
        scope.files
            .withPackage("de.lemke.oneuisample.domain..")
            .forEach { file ->
                assertTrue(
                    file.imports.none { it.name.contains("de.lemke.oneuisample.ui.") },
                    "Domain file '${file.name}' must not import from ui layer",
                )
            }
    }

    @Test
    fun `data layer does not depend on domain`() {
        scope.files
            .withPackage("de.lemke.oneuisample.data..")
            .forEach { file ->
                assertTrue(
                    file.imports.none { it.name.contains("de.lemke.oneuisample.domain.") },
                    "Data file '${file.name}' must not import from domain layer",
                )
            }
    }
}
