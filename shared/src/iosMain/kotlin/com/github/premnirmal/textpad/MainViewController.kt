package com.github.premnirmal.textpad

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.github.premnirmal.textpad.ui.App
import platform.UIKit.UIViewController

// PascalCase factory name is the iOS entry point referenced from Swift (MainViewControllerKt).
@Suppress("FunctionNaming")
fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        // The app already reserves space for the keyboard with Modifier.imePadding(), so the
        // default FocusableAboveKeyboard behaviour would pan the whole scene up by the keyboard
        // height a second time, leaving a large empty gap above the keyboard.
        onFocusBehavior = OnFocusBehavior.DoNothing
    },
) { App() }
