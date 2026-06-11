package de.lemke.oneuisample.domain

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class AppStartTest : ShouldSpec({

    fun appStart(
        result: AppStartResult = AppStartResult.NORMAL,
        versionCode: Int = 10,
        versionName: String = "1.0",
        lastVersionCode: Int = 10,
        lastVersionName: String = "1.0",
        tosVersion: Int = 1,
        acceptedTosVersion: Int = 1,
    ) = AppStart(result, versionCode, versionName, lastVersionCode, lastVersionName, tosVersion, acceptedTosVersion)

    context("isFirstTime") {
        should("true when lastVersionCode is -1") {
            appStart(lastVersionCode = -1).isFirstTime shouldBe true
        }
        should("false when lastVersionCode is 0") {
            appStart(lastVersionCode = 0).isFirstTime shouldBe false
        }
        should("false when lastVersionCode equals versionCode") {
            appStart(lastVersionCode = 10, versionCode = 10).isFirstTime shouldBe false
        }
    }

    context("isFirstTimeVersion") {
        should("true when lastVersionCode is less than versionCode and not -1") {
            appStart(versionCode = 10, lastVersionCode = 5).isFirstTimeVersion shouldBe true
        }
        should("true when lastVersionCode is 0") {
            appStart(versionCode = 10, lastVersionCode = 0).isFirstTimeVersion shouldBe true
        }
        should("false when versions match") {
            appStart(versionCode = 10, lastVersionCode = 10).isFirstTimeVersion shouldBe false
        }
        should("false when lastVersionCode is -1 (first install)") {
            appStart(versionCode = 10, lastVersionCode = -1).isFirstTimeVersion shouldBe false
        }
        should("false when lastVersionCode exceeds versionCode (downgrade)") {
            appStart(versionCode = 5, lastVersionCode = 10).isFirstTimeVersion shouldBe false
        }
    }

    context("tosAccepted") {
        should("true when acceptedTosVersion equals tosVersion") {
            appStart(tosVersion = 2, acceptedTosVersion = 2).tosAccepted shouldBe true
        }
        should("true when acceptedTosVersion exceeds tosVersion") {
            appStart(tosVersion = 1, acceptedTosVersion = 3).tosAccepted shouldBe true
        }
        should("false when acceptedTosVersion is below tosVersion") {
            appStart(tosVersion = 2, acceptedTosVersion = 1).tosAccepted shouldBe false
        }
        should("false when acceptedTosVersion is -1") {
            appStart(tosVersion = 1, acceptedTosVersion = -1).tosAccepted shouldBe false
        }
    }

    context("shouldShowOOBE") {
        should("true when first install") {
            appStart(lastVersionCode = -1, acceptedTosVersion = 1, tosVersion = 1).shouldShowOOBE shouldBe true
        }
        should("true when tos not accepted") {
            appStart(lastVersionCode = 5, tosVersion = 2, acceptedTosVersion = 1).shouldShowOOBE shouldBe true
        }
        should("false on normal launch with tos accepted") {
            appStart(lastVersionCode = 10, tosVersion = 1, acceptedTosVersion = 2).shouldShowOOBE shouldBe false
        }
    }

    context("versionThresholdPassed") {
        should("true when threshold is inside lastVersionCode until versionCode") {
            appStart(versionCode = 10, lastVersionCode = 3).versionThresholdPassed(5) shouldBe true
        }
        should("true at lastVersionCode + 1 (inclusive lower via lastVersionCode in range)") {
            appStart(versionCode = 10, lastVersionCode = 4).versionThresholdPassed(5) shouldBe true
        }
        should("true when threshold equals lastVersionCode (inclusive lower)") {
            appStart(versionCode = 10, lastVersionCode = 5).versionThresholdPassed(5) shouldBe true
        }
        should("false when threshold is below lastVersionCode") {
            appStart(versionCode = 10, lastVersionCode = 5).versionThresholdPassed(4) shouldBe false
        }
        should("false when threshold equals versionCode (exclusive upper)") {
            appStart(versionCode = 10, lastVersionCode = 3).versionThresholdPassed(10) shouldBe false
        }
        should("false when threshold exceeds versionCode") {
            appStart(versionCode = 10, lastVersionCode = 3).versionThresholdPassed(15) shouldBe false
        }
        should("false when lastVersionCode is -1 (fresh install)") {
            appStart(versionCode = 10, lastVersionCode = -1).versionThresholdPassed(5) shouldBe false
        }
    }

    context("toString") {
        should("includes all fields") {
            val s =
                appStart(
                    result = AppStartResult.FIRST_TIME,
                    versionCode = 5,
                    versionName = "2.0",
                    lastVersionCode = 0,
                    lastVersionName = "1.0",
                    tosVersion = 3,
                    acceptedTosVersion = 2,
                ).toString()
            s shouldContain "FIRST_TIME"
            s shouldContain "versionCode=5"
            s shouldContain "versionName='2.0'"
            s shouldContain "lastVersionCode=0"
            s shouldContain "tosVersion=3"
            s shouldContain "acceptedTosVersion=2"
        }

        should("includes FIRST_TIME_VERSION result") {
            appStart(result = AppStartResult.FIRST_TIME_VERSION).toString() shouldContain "FIRST_TIME_VERSION"
        }
    }
})
