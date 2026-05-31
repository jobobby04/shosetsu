object Contributors {
	/**
	 * Association between preferred names.
	 *
	 * For example, "doomsdayrs" should be mapped to "Clocks".
	 */
	val preferredNames = mapOf(
		"doomsdayrs" to "Clocks",
		"j. fronny" to "JFronny",
		"jobobby04" to "Jobobby04",
		"suhan-paradkar" to "Suhan G Paradkar",
		"wasu-code" to "wasu",
		"wasu dev" to "wasu",
	)

	/**
	 * Association between preferred emails.
	 *
	 * For example, "doomsdayrs@gmail.com" should be mapped to "me@doomsdayrs.page".
	 */
	val preferredEmails = mapOf(
		"doomsdayrs@proton.me" to "me@doomsdayrs.page",
		"doomsdayrs@gmail.com" to "me@doomsdayrs.page"
	)

	/**
	 * Association between a name and an image url.
	 *
	 * Name can be preferred name.
	 */
	val images = mapOf(
		"clocks" to "https://gitlab.com/uploads/-/system/user/avatar/3931112/avatar.png?width=256",
		"jfronny" to "https://gitlab.com/uploads/-/system/user/avatar/6260391/avatar.png?width=256",
	)

	/**
	 * Association between a name and a website.
	 *
	 * Name can be preferred name.
	 */
	val websites = mapOf(
		"clocks" to "https://doomsdayrs.page",
		"jfronny" to "https://jfronny.gitlab.io",
	)
}
