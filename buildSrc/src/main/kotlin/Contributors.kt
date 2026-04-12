import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap

object Contributors {
    /**
     * Association between a name and an image url.
     *
     * Name can be preferred name.
     */
    val knownImages = mapOf(
        "clocks" to "https://gitlab.com/uploads/-/system/user/avatar/3931112/avatar.png?width=256",
        "jfronny" to "https://gitlab.com/uploads/-/system/user/avatar/6260391/avatar.png?width=256",
    )

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
     * Associations between different usernames.
     */
    val knownLinks = bidiMultimapOf(
        *preferredNames.map { it.key.lowercase() to it.value.lowercase() }.toTypedArray(),
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



    private fun <K> bidiMultimapOf(vararg pairs: Pair<K & Any, K & Any>): Multimap<K, K> {
        val builder = ImmutableMultimap.builder<K, K>()
        pairs.forEach {
            builder.put(it.first, it.second)
            builder.put(it.second, it.first)
        }
        return builder.build()
    }
}