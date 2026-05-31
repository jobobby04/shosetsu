package app.shosetsu.android.ui.main

import android.app.SearchManager
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.ACTION_OPEN_APP_UPDATE
import app.shosetsu.android.common.consts.ACTION_OPEN_CATALOGUE
import app.shosetsu.android.common.consts.ACTION_OPEN_LIBRARY
import app.shosetsu.android.common.consts.ACTION_OPEN_SEARCH
import app.shosetsu.android.common.consts.ACTION_OPEN_UPDATES
import app.shosetsu.android.common.consts.ACTION_VIEW_SETTING_BACKUP_SELECT_FOLDER
import app.shosetsu.android.common.ext.logD
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logW
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest

private fun handleIntentAction(
	intent: Intent,
	onNavigate: (ShosetsuDestination) -> Unit,
	onUpdate: () -> Unit
) {
	intent.logD("Intent received was ${intent.action}")
	when (intent.action) {
		ACTION_OPEN_CATALOGUE -> onNavigate(Destination.Browse)
		ACTION_OPEN_UPDATES -> onNavigate(Destination.Updates)
		ACTION_OPEN_LIBRARY -> onNavigate(Destination.Library)

		Intent.ACTION_SEARCH, ACTION_OPEN_SEARCH -> {
			onNavigate(
				Destination.Search(
					query = intent.getStringExtra(SearchManager.QUERY) ?: ""
				)
			)
		}

		ACTION_OPEN_APP_UPDATE -> {
			onUpdate()
		}

		Intent.ACTION_VIEW -> {
			if (intent.data != null) {
				if (intent.data!!.scheme != null) {
					onNavigate(
						Destination.More.AddShare(
							intent.data.toString()
						)
					)
				} else intent.logE("Scheme was null")
			} else intent.logE("View action data null")
		}

		ACTION_VIEW_SETTING_BACKUP_SELECT_FOLDER -> {
			intent.logI("Navigating to backup settings...")
			onNavigate(
				Destination.More.Settings.Backup(true)
			)
		}

		Intent.ACTION_APPLICATION_PREFERENCES -> {
			intent.logI("Navigating to settings...")
			onNavigate(Destination.More.Settings)
		}

		Intent.ACTION_MAIN -> {}
		else -> {
			intent.logW("Cannot handle this intent.")
		}
	}
}

@Composable
fun IntentHandler(
	onNavigate: (ShosetsuDestination) -> Unit,
	onUpdate: () -> Unit
) {
	val context = LocalContext.current

	LaunchedEffect(Unit) {
		callbackFlow {
			val activity = context as ComponentActivity
			val consumer = Consumer<Intent> { trySend(it) }
			consumer.accept(activity.intent)
			activity.addOnNewIntentListener(consumer)
			awaitClose { activity.removeOnNewIntentListener(consumer) }
		}.collectLatest {
			try {
				handleIntentAction(it, onNavigate, onUpdate)
			} catch (e: Exception) {
				Toast.makeText(context, R.string.error_intent_handle, Toast.LENGTH_SHORT)
					.show()
			}
		}
	}
}
