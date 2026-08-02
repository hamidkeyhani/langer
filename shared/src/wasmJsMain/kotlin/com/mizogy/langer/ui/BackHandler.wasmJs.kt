package com.mizogy.langer.ui

import androidx.compose.runtime.Composable

@Composable
actual fun RegisterBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on Web
}
