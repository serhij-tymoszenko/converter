package dev.tymoshenko.converter

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.application
import dev.tymoshenko.converter.converter.convert
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.writeString

fun main() = application {
    FileKit.init(appId = "converter")

    LaunchedEffect(true) {
        val svg = FileKit.openFilePicker()
        svg?.let {
            val contentToSave = convert(it.file)
            val newSvg = FileKit.openFileSaver(suggestedName = "awesomeSvg", extension = "svg")
            newSvg?.writeString(contentToSave)
        }
    }
}