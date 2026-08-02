package com.mizogy.langer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Langer",
    ) {
        App(onExit = { exitApplication() })
    }
}