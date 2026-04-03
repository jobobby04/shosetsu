package app.shosetsu.android.view.compose.setting

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.view.compose.setting.widget.MultiSelectListPreferenceWidget

/**
 * Suppress for call chain, the way it is done is for composable features.
 */
@Suppress("SimplifiableCallChain")
@Composable
fun RestrictionSelectPreferenceWidget(
	title: String,
	@StringRes subtitle: Int,
	restrictions: Map<Int, SettingKey<Boolean>>,
	repo: ISettingsRepository
) {
	val restrictionStates = restrictions.mapValues { (_, key) ->
		repo.getBooleanFlow(key).collectAsState()
	}
	val subtitleSuffix = restrictionStates.toList()
		.filter { it.second.value }
		.map { stringResource(it.first) }
		.joinToString(", ")
	MultiSelectListPreferenceWidget(
		title = title,
		subtitle = stringResource(subtitle, subtitleSuffix),
		possibleValues = restrictions.keys.toList(),
		selectedValues = restrictions.keys.filter { restrictionStates[it]!!.value }.toSet(),
		stringify = { stringResource(it) },
		onValuesChange = {
			restrictions.forEach { (title, key) ->
				launchIO { repo.setBoolean(key, title in it) }
			}
		}
	)
}