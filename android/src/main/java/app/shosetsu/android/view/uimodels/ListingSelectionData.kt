package app.shosetsu.android.view.uimodels

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/**
 * Used as a data model for listing selections in the UI objects.
 */
@Immutable
data class ListingSelectionData(
	val choices: ImmutableList<String>,
	val selection: Int
)