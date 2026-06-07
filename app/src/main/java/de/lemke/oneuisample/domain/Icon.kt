package de.lemke.oneuisample.domain

data class Icon(
    val resId: Int,
    val resEntryName: String,
) {
    val id get() = resId.toLong()
    val name get() = resEntryName.removePrefix("ic_oui_")
    val beautifiedName get() = name.replace('_', ' ').replaceFirstChar { it.uppercase() }
    val indexChar get() = name.first().uppercaseChar()

    fun containsKeywords(keywords: Set<String>): Boolean = keywords.any { name.contains(it, ignoreCase = true) }
}
