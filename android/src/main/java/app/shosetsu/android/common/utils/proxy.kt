package app.shosetsu.android.common.utils

data class ProxyConfig(
    val hostname: String,
    val port: Int,
    val authUsed: Boolean,
    val username: String,
    val password: String,
) {
    companion object {
        fun fromString(proxyString: String): ProxyConfig {
            val (auth, hostname) = if (proxyString.contains('@')) {
                val (auth_, host_) = proxyString.split('@', limit=2)
                Pair(auth_, host_)
            } else Pair("", proxyString)
            val (username, password) = if (auth.isNotEmpty()) {
                if (auth.contains(':')) {
                    val split = auth.split(':')
                    when (split.size) {
                        2 -> Pair(split[0], split[1])
                        1 -> Pair(split[0], "")
                        else -> Pair("", "")
                    }
                } else {
                    Pair(auth, "")
                }
            } else Pair("", "")

            val (host, port) = if (hostname.contains(':')) {
                val (host_, port_) = hostname.split(':', limit=2)
                Pair(host_, port_.toIntOrNull() ?: -1)
            } else Pair(hostname, -1)

            return ProxyConfig(host, port, username.isNotEmpty(), username, password)
        }
    }

    fun valid(): Boolean {
        val authValid = when {
            !authUsed -> true
            username.isEmpty() -> false
            password.isEmpty() -> false
            else -> true
        }
        val hostnameValid = hostname.isNotEmpty()
        val portValid = port in (80..65535)

        return hostnameValid and portValid and authValid
    }

    override fun toString(): String {
        if (hostname.isEmpty())
            return ""

        val sb = StringBuilder()
        if (authUsed) {
            sb.append(username)

            if (password.isNotEmpty())
                sb.append(":${password}@")
        }
        sb.append("${hostname}:${port}")
        return sb.toString()
    }
}

