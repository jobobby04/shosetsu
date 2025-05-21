package app.shosetsu.android.common.utils

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.util.Formatter
import java.util.Locale

typealias Logger = (String) -> Unit

class LoggingPrintStream(private val logger: Logger) : PrintStream(LoggingOutputStream(logger)) {
	private fun maybeLog(param: Any?) {
		if (param is String && param.isBlank()) return
		logger(param.toString())
	}

	override fun println(x: Int) = maybeLog(x)
	override fun println(x: Long) = maybeLog(x)
	override fun println(x: Float) = maybeLog(x)
	override fun println(x: Double) = maybeLog(x)
	override fun println(x: CharArray?) = maybeLog(x)
	override fun println(x: String?) = maybeLog(x)
	override fun println(x: Any?) = maybeLog(x)
	override fun format(format: String?, vararg args: Any?): PrintStream {
		maybeLog(Formatter().format(format, *args).toString().trimEnd())
		return this
	}

	override fun format(l: Locale?, format: String?, vararg args: Any?): PrintStream {
		maybeLog(Formatter(l).format(format, *args).toString().trimEnd())
		return this
	}
}

class LoggingOutputStream(private val logger: Logger) : OutputStream() {
	private val baos = ByteArrayOutputStream(1024)
	override fun write(p0: Int) = baos.write(p0)
	override fun flush() {
		super.flush()
		val line = baos.toString()
		baos.reset()
		if (line.isNotBlank()) {
			logger(line.trimEnd())
		}
	}

	override fun close() {
		super.close()
		flush()
		baos.close()
	}
}