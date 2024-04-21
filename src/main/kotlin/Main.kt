import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.application
import uk.beh.ktcnotepad.common.LocalAppResources
import uk.beh.ktcnotepad.common.rememberAppResources
import uk.beh.ktcnotepad.notepadApplication
import uk.beh.ktcnotepad.rememberApplicationState

fun main() =
    application {
        CompositionLocalProvider(LocalAppResources provides rememberAppResources()) {
            notepadApplication(rememberApplicationState())
        }
    }
