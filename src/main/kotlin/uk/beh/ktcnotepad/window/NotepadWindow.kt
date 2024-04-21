package uk.beh.ktcnotepad.window

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import kotlinx.coroutines.launch
import uk.beh.ktcnotepad.common.LocalAppResources
import uk.beh.ktcnotepad.util.fileDialog
import uk.beh.ktcnotepad.util.yesNoCancelDialog

@Composable
fun notepadWindow(state: NotepadWindowState) {
    val scope = rememberCoroutineScope()

    fun exit() = scope.launch { state.exit() }

    Window(
        state = state.window,
        title = titleOf(state),
        icon = LocalAppResources.current.icon,
        onCloseRequest = { exit() },
    ) {
        LaunchedEffect(Unit) { state.run() }

        windowNotifications(state)
        windowMenuBar(state)

        // TextField isn't efficient for big text files, we use it for simplicity
        BasicTextField(
            state.text,
            state::text::set,
            enabled = state.isInit,
            modifier = Modifier.fillMaxSize(),
        )

        if (state.openDialog.isAwaiting) {
            fileDialog(
                title = "Notepad",
                isLoad = true,
                onResult = {
                    state.openDialog.onResult(it)
                },
            )
        }

        if (state.saveDialog.isAwaiting) {
            fileDialog(
                title = "Notepad",
                isLoad = false,
                onResult = { state.saveDialog.onResult(it) },
            )
        }

        if (state.exitDialog.isAwaiting) {
            yesNoCancelDialog(
                title = "Notepad",
                message = "Save changes?",
                onResult = { state.exitDialog.onResult(it) },
            )
        }
    }
}

private fun titleOf(state: NotepadWindowState): String {
    val changeMark = if (state.isChanged) "*" else ""
    val filePath = state.path ?: "Untitled"
    return "$changeMark$filePath - Notepad"
}

@Composable
private fun windowNotifications(state: NotepadWindowState) {
    // Usually we take into account something like LocalLocale.current here
    fun NotepadWindowNotification.format() =
        when (this) {
            is NotepadWindowNotification.SaveSuccess ->
                Notification(
                    "File is saved",
                    path.toString(),
                    Notification.Type.Info,
                )
            is NotepadWindowNotification.SaveError ->
                Notification(
                    "File isn't saved",
                    path.toString(),
                    Notification.Type.Error,
                )
        }

    LaunchedEffect(Unit) {
        state.notifications.collect {
            state.sendNotification(it.format())
        }
    }
}

@Composable
private fun FrameWindowScope.windowMenuBar(state: NotepadWindowState) =
    MenuBar {
        val scope = rememberCoroutineScope()

        fun save() = scope.launch { state.save() }

        fun open() = scope.launch { state.open() }

        fun exit() = scope.launch { state.exit() }

        Menu("File") {
            Item("New window", onClick = state::newWindow)
            Item("Open...", onClick = { open() })
            Item("Save", onClick = { save() }, enabled = state.isChanged || state.path == null)
            Separator()
            Item("Exit", onClick = { exit() })
        }

        Menu("Settings") {
            Item(
                if (state.settings.isTrayEnabled) "Hide tray" else "Show tray",
                onClick = state.settings::toggleTray,
            )
            Item(
                if (state.window.placement == WindowPlacement.Fullscreen) "Exit fullscreen" else "Enter fullscreen",
                onClick = state::toggleFullscreen,
            )
        }
    }
