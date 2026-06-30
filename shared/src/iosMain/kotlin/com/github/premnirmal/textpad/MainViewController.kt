package com.github.premnirmal.textpad

import androidx.compose.ui.window.ComposeUIViewController
import com.github.premnirmal.textpad.ui.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
