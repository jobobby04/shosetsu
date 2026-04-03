import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap

object Contributors {
    /**
     * Associations between different usernames.
     */
    val knownLinks = bidiMultimapOf(
        "clocks" to "doomsdayrs"
    )

    /**
     * Association between a name and an image url.
     *
     * Name can be preferred name.
     */
    val knownImages = mapOf(
        "clocks" to "https://gitlab.com/uploads/-/system/user/avatar/3931112/avatar.png?width=256"
    )

    /**
     * Association between preferred names.
     *
     * For example, "doomsdayrs" should be mapped to "Clocks".
     */
    val preferredNames = mapOf(
        "doomsdayrs" to "Clocks"
    )

    /**
     * Association between a name and a website.
     *
     * Name can be preferred name.
     */
    val websites = mapOf(
        "clocks" to "https://doomsdayrs.page"
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