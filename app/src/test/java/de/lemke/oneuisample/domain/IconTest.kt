package de.lemke.oneuisample.domain

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class IconTest : ShouldSpec({

    should("id returns resId as Long") {
        Icon(resId = 42, resEntryName = "ic_oui_star").id shouldBe 42L
    }

    should("name strips ic_oui_ prefix") {
        Icon(resId = 1, resEntryName = "ic_oui_star").name shouldBe "star"
    }

    should("name is unchanged when no ic_oui_ prefix") {
        Icon(resId = 1, resEntryName = "custom_icon").name shouldBe "custom_icon"
    }

    should("name is empty string when entry name equals the prefix exactly") {
        Icon(resId = 1, resEntryName = "ic_oui_").name shouldBe ""
    }

    should("beautifiedName replaces underscores with spaces and capitalizes first letter") {
        Icon(resId = 1, resEntryName = "ic_oui_star_filled").beautifiedName shouldBe "Star filled"
    }

    should("beautifiedName for single-word name") {
        Icon(resId = 1, resEntryName = "ic_oui_star").beautifiedName shouldBe "Star"
    }

    should("beautifiedName for empty name is empty string") {
        Icon(resId = 1, resEntryName = "ic_oui_").beautifiedName shouldBe ""
    }

    should("indexChar returns first char of name uppercased") {
        Icon(resId = 1, resEntryName = "ic_oui_star").indexChar shouldBe 'S'
    }

    should("indexChar returns # when name is empty after prefix removal") {
        Icon(resId = 1, resEntryName = "ic_oui_").indexChar shouldBe '#'
    }

    should("containsKeywords returns true when name contains keyword") {
        Icon(resId = 1, resEntryName = "ic_oui_star").containsKeywords(setOf("star")) shouldBe true
    }

    should("containsKeywords is case insensitive") {
        Icon(resId = 1, resEntryName = "ic_oui_star").containsKeywords(setOf("STAR")) shouldBe true
    }

    should("containsKeywords returns true when any keyword matches") {
        Icon(resId = 1, resEntryName = "ic_oui_moon").containsKeywords(setOf("star", "moon")) shouldBe true
    }

    should("containsKeywords returns false when no keyword matches") {
        Icon(resId = 1, resEntryName = "ic_oui_star").containsKeywords(setOf("moon", "sun")) shouldBe false
    }

    should("containsKeywords returns false for empty keyword set") {
        Icon(resId = 1, resEntryName = "ic_oui_star").containsKeywords(emptySet()) shouldBe false
    }

    should("data class equals compares resId and resEntryName") {
        Icon(resId = 1, resEntryName = "ic_oui_star") shouldBe Icon(resId = 1, resEntryName = "ic_oui_star")
    }

    should("toString includes resId and resEntryName") {
        val s = Icon(resId = 7, resEntryName = "ic_oui_moon").toString()
        s.contains("7") shouldBe true
        s.contains("ic_oui_moon") shouldBe true
    }
})
