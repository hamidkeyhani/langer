package com.mizogy.langer.ui

import androidx.compose.runtime.Composable

@Composable
expect fun RegisterBackHandler(enabled: Boolean, onBack: () -> Unit)
