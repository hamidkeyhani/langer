package com.example.langer.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*

sealed interface Screen {
    object DeckList : Screen
    data class Study(val deckId: String) : Screen
    data class CardManager(val deckId: String) : Screen
    data class AddEditCard(val deckId: String, val cardId: String? = null) : Screen
    data class BulkImport(val deckId: String) : Screen
}

class Navigator(initialScreen: Screen) {
    private val _backstack = mutableStateListOf<Screen>(initialScreen)
    val backstack: List<Screen> get() = _backstack

    val currentScreen: Screen
        get() = _backstack.last()

    val canGoBack: Boolean
        get() = _backstack.size > 1

    fun navigateTo(screen: Screen) {
        _backstack.add(screen)
    }

    fun pop(): Boolean {
        if (_backstack.size > 1) {
            _backstack.removeLast()
            return true
        }
        return false
    }

    fun popToRoot() {
        while (_backstack.size > 1) {
            _backstack.removeLast()
        }
    }
}

@Composable
fun rememberNavigator(initialScreen: Screen = Screen.DeckList): Navigator {
    return remember { Navigator(initialScreen) }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedNavigation(
    navigator: Navigator,
    content: @Composable (Screen) -> Unit
) {
    // We animate screen switches. To make it smooth, we slide in from right on push, and left on pop.
    // We can detect push vs pop by tracking the size of the backstack.
    var previousSize by remember { mutableStateOf(navigator.backstack.size) }
    val isPush = navigator.backstack.size >= previousSize
    previousSize = navigator.backstack.size

    AnimatedContent(
        targetState = navigator.currentScreen,
        transitionSpec = {
            if (isPush) {
                // Slide in from right, fade out to left
                (slideInHorizontally(animationSpec = tween(400)) { it } + fadeIn(animationSpec = tween(400))) togetherWith
                        (slideOutHorizontally(animationSpec = tween(400)) { -it / 3 } + fadeOut(animationSpec = tween(300)))
            } else {
                // Slide in from left, fade out to right
                (slideInHorizontally(animationSpec = tween(400)) { -it } + fadeIn(animationSpec = tween(400))) togetherWith
                        (slideOutHorizontally(animationSpec = tween(400)) { it / 3 } + fadeOut(animationSpec = tween(300)))
            }
        }
    ) { screen ->
        content(screen)
    }
}
