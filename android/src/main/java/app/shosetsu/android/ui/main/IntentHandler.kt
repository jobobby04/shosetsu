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
import app.shosetsu.android.common.ext.logD
import app.shosetsu.android.common.ext.logE
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest

fun handleIntentAction(
	intent: Intent,
	onNavigate: (String) -> Unit,
	onUpdate: () -> Unit
) {
	intent.logD("Intent received was ${intent.action}")
	when (intent.action) {
		ACTION_OPEN_CATALOGUE -> onNavigate(Destination.BROWSE.route)

		ACTION_OPEN_UPDATES -> onNavigate(Destination.UPDATES.route)

		ACTION_OPEN_LIBRARY -> onNavigate(Destination.LIBRARY.route)

		Intent.ACTION_SEARCH -> {
			onNavigate(
				Destination.SEARCH.routeWith(
					query = intent.getStringExtra(
						SearchManager.QUERY
					)
				)
			)
		}

		ACTION_OPEN_SEARCH -> {
			onNavigate(
				Destination.SEARCH.routeWith(
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
						Destination.ADD_SHARE.routeWith(
							intent.data!!.scheme + "://" + intent.data!!.host
						)
					)
				} else intent.logE("Scheme was null")
			} else intent.logE("View action data null")
		}

		Intent.ACTION_MAIN -> {}
		else -> {}
	}
}

@Composable
fun IntentHandler(
	onNavigate: (String) -> Unit,
	onUpdate: () -> Unit
) {
	val context = LocalContext.current


	LaunchedEffect(Unit) {
		callbackFlow<Intent> {
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