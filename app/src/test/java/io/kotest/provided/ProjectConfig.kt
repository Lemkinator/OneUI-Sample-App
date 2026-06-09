package io.kotest.provided

import de.lemke.oneuisample.TestDispatcherListener
import io.kotest.core.config.AbstractProjectConfig

class ProjectConfig : AbstractProjectConfig() {
    override val extensions = listOf(TestDispatcherListener())
}
