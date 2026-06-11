package de.lemke.oneuisample.ui.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ListTypesTest : ShouldSpec({
    should("all seven enum values are accessible") {
        ListTypes.entries.size shouldBe 7
    }

    should("valueOf returns the correct constant") {
        ListTypes.valueOf("LIST_TYPE") shouldBe ListTypes.LIST_TYPE
    }

    should("each entry has a non-null builder class") {
        ListTypes.entries.forEach { it.builder shouldNotBe null }
    }

    should("each entry has a positive description resource id") {
        ListTypes.entries.forEach { (it.description > 0) shouldBe true }
    }

    should("LIST_TYPE builder is distinct from TYPE_GRID builder") {
        ListTypes.LIST_TYPE.builder shouldNotBe ListTypes.TYPE_GRID.builder
    }

    should("TYPE_LIST_CHECKBOX and TYPE_LIST_RADIOBUTTON have distinct builders") {
        ListTypes.TYPE_LIST_CHECKBOX.builder shouldNotBe ListTypes.TYPE_LIST_RADIOBUTTON.builder
    }
})
