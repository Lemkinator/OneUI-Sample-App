/*
 * Copyright 2022-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.io.path.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.name
import kotlin.io.path.writeText

class MockingConventionsTest : ShouldSpec() {
    init {
        should("report the type argument of mockk and spyk") {
            val source =
                """
                val a = mockk<Widget>()
                val b = spyk<Gadget>(real)
                val c = mockk<Box<Widget>>(relaxed = true)
                """.trimIndent()

            MockingConventions.mockedNames(source) shouldBe
                listOf(MockedName(1, "Widget"), MockedName(2, "Gadget"), MockedName(3, "Box"))
        }
        should("report the declared type of a property that mockk or spyk initializes") {
            val source =
                """
                val a: Widget = mockk(relaxed = true)
                private var b: Gadget? = spyk(real)
                val c: Box<Widget> = mockk()
                """.trimIndent()

            MockingConventions.mockedNames(source) shouldBe
                listOf(MockedName(1, "Widget"), MockedName(2, "Gadget"), MockedName(3, "Box"))
        }
        should("report a declared type once when the call repeats it") {
            MockingConventions.mockedNames("val a: Widget = mockk<Widget>()") shouldBe listOf(MockedName(1, "Widget"))
        }
        should("report the class argument of mockkClass, mockkObject, mockkConstructor and mockkStatic") {
            val source =
                """
                mockkClass(Widget::class, relaxed = true)
                mockkObject(Gadget)
                mockkConstructor(Gizmo::class)
                mockkStatic(Tool::class.java)
                mockkObject(own.Part)
                """.trimIndent()

            MockingConventions.mockedNames(source) shouldBe
                listOf(
                    MockedName(1, "Widget"),
                    MockedName(2, "Gadget"),
                    MockedName(3, "Gizmo"),
                    MockedName(4, "Tool"),
                    MockedName(5, "own.Part"),
                )
        }
        should("report every class argument of a call") {
            val source =
                """
                mockkStatic(
                    Tool::class,
                    Part::class,
                )
                """.trimIndent()

            MockingConventions.mockedNames(source) shouldBe listOf(MockedName(1, "Part"), MockedName(1, "Tool"))
        }
        should("report the class name string of mockkStatic") {
            MockingConventions.mockedNames("""mockkStatic("demo.ToolsKt")""") shouldBe
                listOf(MockedName(1, "demo.ToolsKt", MockedKind.ClassName))
        }
        should("report an annotated property with the property on the same line") {
            val source =
                """
                @MockK lateinit var a: Widget
                @RelaxedMockK private val b: Gadget = TODO()
                @SpyK var c: Gizmo? = null
                @field:MockK lateinit var d: Tool
                @MockK(relaxed = true) lateinit var e: Part
                """.trimIndent()

            MockingConventions.mockedNames(source) shouldBe
                listOf(
                    MockedName(1, "Widget"),
                    MockedName(2, "Gadget"),
                    MockedName(3, "Gizmo"),
                    MockedName(4, "Tool"),
                    MockedName(5, "Part"),
                )
        }
        should("report an annotated property with other annotations between the mock annotation and the property") {
            val source =
                """
                @MockK @Suppress("unused") lateinit var a: Widget
                @RelaxedMockK
                @JvmField
                @Named("x") private val b: Gadget = TODO()
                @SpyK @field:Inject var c: Gizmo? = null
                """.trimIndent()

            MockingConventions.mockedNames(source) shouldBe
                listOf(MockedName(1, "Widget"), MockedName(2, "Gadget"), MockedName(5, "Gizmo"))
        }
        should("report a top-level function reference and a receiver type of mockkStatic") {
            val source =
                """
                mockkStatic(::ownTopLevelFun)
                mockkStatic(Own::fn, ::other)
                mockkStatic(Tool::class, Part::class.java)
                """.trimIndent()

            MockingConventions.mockedNames(source) shouldBe
                listOf(
                    MockedName(1, "ownTopLevelFun", MockedKind.Function),
                    MockedName(2, "Own"),
                    MockedName(2, "other", MockedKind.Function),
                    MockedName(3, "Part"),
                    MockedName(3, "Tool"),
                )
        }
        should("report an annotated property with the property on the next line") {
            val source =
                """
                @MockK
                lateinit var a: Widget

                @RelaxedMockK
                private lateinit var b: Gadget

                @SpyK
                val c: Gizmo = Gizmo()
                """.trimIndent()

            MockingConventions.mockedNames(source) shouldBe
                listOf(MockedName(1, "Widget"), MockedName(4, "Gadget"), MockedName(7, "Gizmo"))
        }
        should("ignore a mock call in a comment") {
            val source =
                """
                // val a = mockk<Widget>()
                /* @MockK lateinit var b: Gadget
                   mockkStatic("demo.ToolsKt") */
                val c = mockk<Gizmo>()
                """.trimIndent()

            MockingConventions.mockedNames(source) shouldBe listOf(MockedName(4, "Gizmo"))
        }
        should("keep a string that contains comment markers") {
            MockingConventions.mockedNames("""mockkStatic("https://demo.ToolsKt")""") shouldBe
                listOf(MockedName(1, "https://demo.ToolsKt", MockedKind.ClassName))
        }
        should("ignore declarations without a mock call") {
            val source =
                """
                val a: Widget = Widget()
                val b = Gadget()
                mockkClass(relaxed = true)
                """.trimIndent()

            MockingConventions.mockedNames(source).shouldBeEmpty()
        }
        should("list every own type a scope mocks as project path, line and name") {
            val sources =
                mapOf(
                    "Own.kt" to "package own\n\ninterface Repo\n\nclass Store\n\nclass Outer {\n    class Inner\n}\n\nobject Registry\n",
                    "Mocks.kt" to
                        """
                        package demo

                        import io.mockk.mockk
                        import io.mockk.mockkObject
                        import io.mockk.mockkStatic
                        import own.Outer
                        import own.Registry as Reg
                        import own.Repo

                        val byImport = mockk<Repo>()
                        val byAlias = mockkObject(Reg)
                        val byNested = mockk<Outer.Inner>()
                        val byQualified = mockk<own.Store>()
                        val byPackageString = mockkStatic("own.OwnKt")
                        """.trimIndent(),
                    "Wild.kt" to "package other\n\nimport own.*\n\nval byWildcard = mockk<Store>()\n",
                    "Same.kt" to "package own\n\n@MockK\nlateinit var bySamePackage: Repo\n",
                )

            withScope(sources) { scope, base ->
                val error = shouldThrow<AssertionError> { scope.assertNoMockedOwnTypes(projectScope = scope) }

                error.message shouldBe
                    """
                    7 mocked own type(s), use a fake:
                    $base/Mocks.kt:10: mocks own type Repo
                    $base/Mocks.kt:11: mocks own type Reg
                    $base/Mocks.kt:12: mocks own type Outer.Inner
                    $base/Mocks.kt:13: mocks own type own.Store
                    $base/Mocks.kt:14: mocks own type own.OwnKt
                    $base/Same.kt:3: mocks own type Repo
                    $base/Wild.kt:5: mocks own type Store
                    """.trimIndent()
            }
        }
        should("accept an external type that shares its simple name with an own type") {
            val sources =
                mapOf(
                    "Own.kt" to "package own\n\nclass Bitmap\n\nclass Uri\n",
                    "Mocks.kt" to
                        """
                        package demo

                        import android.graphics.Bitmap
                        import android.net.*

                        val explicit = mockk<Bitmap>()
                        val qualified = mockk<android.net.Uri>()
                        val staticClass = mockkStatic("android.net.UriKt")
                        val declared: Bitmap = mockk()
                        """.trimIndent(),
                )

            withScope(sources) { scope, _ -> shouldNotThrowAny { scope.assertNoMockedOwnTypes(projectScope = scope) } }
        }
        should("list an own top-level function and an own receiver type that mockkStatic references") {
            val sources =
                mapOf(
                    "Own.kt" to
                        "package own\n\nclass Store {\n    fun member() = Unit\n}\n\nfun topLevel() = Unit\n\nfun aliased() = Unit\n",
                    "Mocks.kt" to
                        """
                        package demo

                        import own.aliased as renamed
                        import own.topLevel

                        val byImport = mockkStatic(::topLevel)
                        val byAlias = mockkStatic(::renamed)
                        val byReceiver = mockkStatic(own.Store::member)
                        """.trimIndent(),
                    "Same.kt" to "package own\n\nval bySamePackage = mockkStatic(::topLevel)\n",
                    "Wild.kt" to "package other\n\nimport own.*\n\nval byWildcard = mockkStatic(::topLevel)\n",
                )

            withScope(sources) { scope, base ->
                val error = shouldThrow<AssertionError> { scope.assertNoMockedOwnTypes(projectScope = scope) }

                error.message shouldBe
                    """
                    5 mocked own type(s), use a fake:
                    $base/Mocks.kt:6: mocks own type topLevel
                    $base/Mocks.kt:7: mocks own type renamed
                    $base/Mocks.kt:8: mocks own type own.Store
                    $base/Same.kt:3: mocks own type topLevel
                    $base/Wild.kt:5: mocks own type topLevel
                    """.trimIndent()
            }
        }
        should("accept an external function reference that shares its name with an own function") {
            val sources =
                mapOf(
                    "Own.kt" to "package own\n\nfun toast() = Unit\n\nfun parse() = Unit\n",
                    "Mocks.kt" to
                        """
                        package demo

                        import android.widget.toast
                        import android.net.Uri

                        val explicit = mockkStatic(::toast)
                        val unresolved = mockkStatic(::parse)
                        val receiver = mockkStatic(Uri::parse)
                        """.trimIndent(),
                )

            withScope(sources) { scope, _ -> shouldNotThrowAny { scope.assertNoMockedOwnTypes(projectScope = scope) } }
        }
        should("pass a scope that mocks no own type") {
            val sources =
                mapOf(
                    "Own.kt" to "package own\n\nclass Store\n",
                    "Clean.kt" to "package demo\n\nimport own.Store\n\nval fake = Store()\n// mockk<Store>()\n",
                )

            withScope(sources) { scope, _ -> shouldNotThrowAny { scope.assertNoMockedOwnTypes(projectScope = scope) } }
        }
    }
}

private fun withScope(
    sources: Map<String, String>,
    block: (scope: KoScope, base: String) -> Unit,
) {
    val dir = createTempDirectory(Path("build", "tmp").toAbsolutePath(), "mocking")
    try {
        sources.forEach { (name, text) -> dir.resolve(name).writeText(text) }
        block(Konsist.scopeFromExternalDirectories(listOf(dir.toString())), "/app/build/tmp/${dir.name}")
    } finally {
        dir.toFile().deleteRecursively()
    }
}
