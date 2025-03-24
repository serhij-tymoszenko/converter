package dev.tymoshenko.converter.converter

import java.awt.Color
import java.io.File

private fun readFile(file: File): MutableList<String> {
    val lines = mutableListOf<String>()
    file.forEachLine { lines.add(it) }
    return lines
}

private fun correctSvg(lines: MutableList<String>) {
    lines.removeIf { it.contains("<!DOCTYPE") }
    val svgTagIndex = lines.indexOfFirst { it.contains("<svg") }
    lines[svgTagIndex] = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 380 600\">"
}

private fun addClasses(lines: MutableList<String>): MutableList<String> {
    var clsIndex = 0
    val classes = mutableMapOf<Int, String>()
    val newLines = lines.map { line: String ->
        if (line.contains(Regex("style=\"*\""))) {
            val startIndex = line.indexOfLast { c -> c == '=' } + 2
            val lastIndex = line.indexOfFirst { c -> c == '/' } - 1
            val style = line.substring(startIndex, lastIndex)

            if (!classes.values.contains(style)) {
                classes[clsIndex++] = style
            }

            line.replace(style, "cls-${classes.filter { it.value == style }.keys.toList()[0]}")
        } else line
    }.toMutableList()

    newLines.add(2, "<defs>")
    newLines.add(3, "<style>")
    newLines.add(4, "</style>")
    newLines.add(5, "</defs>")

    classes.forEach { (k, v) ->
        val startIndex = v.indexOf("rgb")
        val lastIndex = v.indexOf(")") + 1
        val rgbSubstr = try {
            v.substring(startIndex, lastIndex)
        } catch (e: IndexOutOfBoundsException) {
            return@forEach
        }
        val hex = rgbSubstrToHex(rgbSubstr)
        classes[k] = v.replace(rgbSubstr, hex)
    }

    classes.forEach { (k, v) -> newLines.add(4, ".cls-$k { $v }") }
    newLines.replaceAll { it.replace("style=", "class=") }

    return newLines
}

private fun rgbSubstrToHex(substr: String): String {
    val startIndex = substr.indexOf('(') + 1
    val endIndex = substr.indexOf(')')
    val (r, g, b) = substr.substring(startIndex, endIndex).split(",")
    val color = Color(r.toInt(), g.toInt(), b.toInt())
    return "#" + Integer.toHexString(color.rgb).substring(2)
}

fun convert(file: File): String {
    var lines = readFile(file)
    correctSvg(lines)
    lines = addClasses(lines)
    lines.replaceAll { it.replace("#0000ff", "blue") }
    return lines.joinTo(StringBuilder(""), separator = "\n").toString()
}