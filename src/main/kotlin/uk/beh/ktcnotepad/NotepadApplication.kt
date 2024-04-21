package uk.beh.ktcnotepad

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.MenuScope
import androidx.compose.ui.window.Tray
import kotlinx.coroutines.launch
import uk.beh.ktcnotepad.common.LocalAppResources
import uk.beh.ktcnotepad.window.notepadWindow

@Composable
fun ApplicationScope.notepadApplication(state: NotepadApplicationState) {
    if (state.settings.isTrayEnabled && state.windows.isNotEmpty()) {
        applicationTray(state)
    }

    for (window in state.windows) {
        key(window) {
            notepadWindow(window)
        }
    }
}

@Composable
private fun ApplicationScope.applicationTray(state: NotepadApplicationState) {
    Tray(
        LocalAppResources.current.icon,
        state = state.tray,
        tooltip = "Notepad",
        menu = { applicationMenu(state) },
    )
}

@Composable
private fun MenuScope.applicationMenu(state: NotepadApplicationState) {
    val scope = rememberCoroutineScope()

    fun exit() = scope.launch { state.exit() }

    Item("New", onClick = state::newWindow)
    Separator()
    Item("Exit", onClick = { exit() })
}
