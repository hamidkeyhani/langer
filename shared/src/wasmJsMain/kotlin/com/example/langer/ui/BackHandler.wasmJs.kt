package com.example.langer.ui

import androidx.compose.runtime.Composable

@Composable
actual fun RegisterBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on Web
}
