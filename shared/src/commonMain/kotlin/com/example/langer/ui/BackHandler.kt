package com.example.langer.ui

import androidx.compose.runtime.Composable

@Composable
expect fun RegisterBackHandler(enabled: Boolean, onBack: () -> Unit)
