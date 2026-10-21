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
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration

/** What a [MockedName] names. */
internal enum class MockedKind {
    /** A type name. */
    Type,

    /** The package-qualified class name in a `mockkStatic` string. */
    ClassName,

    /** A top-level function in a `mockkStatic(::name)` reference. */
    Function,
}

/** One mocked name. */
internal data class MockedName(
    val line: Int,
    val name: String,
    val kind: MockedKind = MockedKind.Type,
)

internal object MockingConventions {
    private const val TYPE = """[A-Za-z_][\w.]*"""
    private val typeArgument = Regex("""\b(?:mockk|spyk)\s*<\s*($TYPE)""")
    private val classArguments = Regex("""\b(?:mockkClass|mockkStatic|mockkObject|mockkConstructor)\s*\(([^)]*)\)""")
    private val declaredType = Regex("""\bva[lr]\s+\w+\s*:\s*($TYPE)(?:<[^=]*>)?\??\s*=\s*(?:mockk|spyk)\s*\(""")
    private val annotatedType =
        Regex(
            """@(?:\w+:)?(?:MockK|RelaxedMockK|SpyK)\b(?:\([^)]*\))?""" +
                """(?:[\s\w]|@(?:\w+:)?\w+(?:\([^)]*\))?)*?\bva[lr]\s+\w+\s*:\s*($TYPE)""",
        )
    private val argument =
        Regex("""\s*(?:($TYPE)(?:::class(?:\.java)?)?|"([^"]*)"|($TYPE)?::(\w+))\s*""")

    /** The names [source] mocks, outside comments, in source order. */
    fun mockedNames(source: String): List<MockedName> {
        val code = SourceComments.withoutComments(source)
        val typed =
            (
                typeArgument.findAll(
                    code,
                ) + declaredType.findAll(code) + annotatedType.findAll(code)
            ).map { it.at(code, it.groupValues[1]) }
        val listed =
            classArguments.findAll(code).flatMap { call ->
                call.groupValues[1].split(',').mapNotNull { argument.matchEntire(it) }.map { arg ->
                    val (type, className, receiver) = arg.destructured
                    when {
                        type.isNotEmpty() -> call.at(code, type)
                        className.isNotEmpty() -> call.at(code, className, MockedKind.ClassName)
                        receiver.isNotEmpty() -> call.at(code, receiver)
                        else -> call.at(code, arg.groupValues[4], MockedKind.Function)
                    }
                }
            }
        return (typed + listed).distinct().sortedWith(compareBy({ it.line }, { it.name })).toList()
    }

    private fun MatchResult.at(
        code: String,
        name: String,
        kind: MockedKind = MockedKind.Type,
    ): MockedName = MockedName(1 + code.take(range.first).count { it == '\n' }, name, kind)
}

/**
 * Fails on a test that mocks a type declared in this project. MockK belongs at external boundaries only; own types
 * get a fake.
 *
 * Covered, with the mocked type taken from the type argument, the declared type or the class argument:
 * - `mockk<T>()` and `spyk<T>()`;
 * - `val x: T = mockk(...)` and `val x: T = spyk(...)`;
 * - `mockkClass(T::class)`, `mockkObject(T)`, `mockkConstructor(T::class)` and `mockkStatic(T::class)`, each with
 *   several arguments;
 * - `mockkStatic("own.pkg.FooKt")`, matched against the packages the project declares;
 * - `mockkStatic(::ownTopLevelFun)`, resolved to a top-level function the project declares, and `mockkStatic(Own::fn)`,
 *   resolved through the type `Own`;
 * - `@MockK`, `@RelaxedMockK` and `@SpyK` on a `val` or `var`, with other annotations and modifiers in between and the
 *   property on the same or a later line.
 *
 * Comments never match, a mock call in a string literal does. Konsist has no type resolution, so a name resolves
 * through the file's explicit and alias imports, its package and its wildcard imports against the classes, interfaces
 * and objects the whole project declares. Not detected: a type alias of an own type, a type inferred from the
 * right-hand side as in `spyk(OwnType())`, a `lateinit` property that a later `x = mockk()` assigns, a mocked
 * instance taken from a variable, and `mockkStatic(External::ownExtension)`, an own extension on an external type.
 */
public fun KoScope.assertNoMockedOwnTypes(projectScope: KoScope = Konsist.scopeFromProject()) {
    val ownTypes = projectScope.files.flatMap { it.declaredTypeNames() }.toSet()
    val ownFunctions = projectScope.files.flatMap { it.topLevelFunctionNames() }.toSet()
    val ownPackages = projectScope.files.mapNotNull { it.packagee?.name }.toSet()
    val hits =
        files
            .flatMap { file ->
                MockingConventions
                    .mockedNames(file.text)
                    .filter { file.mocksOwnType(it, ownTypes, ownFunctions, ownPackages) }
                    .map { "${portableProjectPath(file.projectPath)}:${it.line}: mocks own type ${it.name}" }
            }.sorted()
    if (hits.isNotEmpty()) throw AssertionError(hits.joinToString("\n", prefix = "${hits.size} mocked own type(s), use a fake:\n"))
}

private fun KoFileDeclaration.mocksOwnType(
    mocked: MockedName,
    ownTypes: Set<String>,
    ownFunctions: Set<String>,
    ownPackages: Set<String>,
): Boolean =
    when (mocked.kind) {
        MockedKind.ClassName -> mocked.name.substringBeforeLast('.', "") in ownPackages
        MockedKind.Type -> resolveNames(mocked.name).any { it in ownTypes }
        MockedKind.Function -> resolveNames(mocked.name).any { it in ownFunctions }
    }

private fun KoFileDeclaration.topLevelFunctionNames(): List<String> =
    functions(includeNested = false, includeLocal = false).map { "${packagee?.name.orEmpty()}.${it.name}".removePrefix(".") }

private fun KoFileDeclaration.declaredTypeNames(): List<String> =
    classes(includeNested = true).mapNotNull { it.fullyQualifiedName } +
        interfaces(includeNested = true).mapNotNull { it.fullyQualifiedName } +
        objects(includeNested = true).mapNotNull { it.fullyQualifiedName }

private fun KoFileDeclaration.resolveNames(name: String): List<String> {
    val head = name.substringBefore('.')
    val tail = name.removePrefix(head)
    val named = imports.filter { !it.isWildcard && (it.alias?.name ?: it.name.substringAfterLast('.')) == head }
    return if (named.isNotEmpty()) {
        named.map { it.name + tail }
    } else {
        listOf(name, "${packagee?.name.orEmpty()}.$name") +
            imports.filter { it.isWildcard }.map { "${it.name.removeSuffix(".*").removeSuffix(".")}.$name" }
    }
}
