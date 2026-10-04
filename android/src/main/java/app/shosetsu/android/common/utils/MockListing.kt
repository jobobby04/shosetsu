package app.shosetsu.android.common.utils

import app.shosetsu.lib.Extension
import app.shosetsu.lib.Filter
import app.shosetsu.lib.Novel

class MockListing(override val name: String = "Mock Listing") : Extension.Listing {
	override val link: String = "/"
	override val search: Extension.Listing.Search = Search()

	class Search : Extension.Listing.Search {
		override val filters: Array<Filter<*>> = arrayOf(
			Filter.Header("Mock Filter"),
			Filter.Separator,
			Filter.Text(0, "Mock Field"),
		)
		override val isIncrementing: Boolean get() = false

		override fun getListing(query: String, filters: Map<Int, *>, page: Int): Array<Novel.Info> =
			arrayOf(
				Novel.Info("Mock Novel 1"),
				Novel.Info("Mock Novel 2"),
				Novel.Info("Mock Novel 3"),
			)
	}

	override val novels: Extension.Listing.Novels = Novels()

	class Novels : Extension.Listing.Novels {
		override val isIncrementing: Boolean get() = false

		override fun get(data: Map<Int, *>, page: Int): Array<Novel.Info> = arrayOf(
			Novel.Info("Mock Novel 1"),
			Novel.Info("Mock Novel 2"),
			Novel.Info("Mock Novel 3"),
		)
	}

	override val listings: Extension.Listing.Listings = Listings()

	class Listings : Extension.Listing.Listings {
		override fun get(): Array<Extension.Listing> = arrayOf(
			MockListing("Mock Listing 1"),
			MockListing("Mock Listing 2"),
			MockListing("Mock Listing 3"),
		)
	}
}
