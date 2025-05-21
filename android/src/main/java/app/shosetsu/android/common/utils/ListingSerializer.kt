package app.shosetsu.android.common.utils

import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import androidx.navigation.NavType
import app.shosetsu.android.application.ShosetsuApplication
import app.shosetsu.lib.Filter
import app.shosetsu.lib.IExtension
import app.shosetsu.lib.Novel
import app.shosetsu.lib.lua.LuaExtension
import app.shosetsu.lib.lua.ShosetsuLuaLib
import app.shosetsu.lib.lua.shosetsuGlobals
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ByteArraySerializer
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.IntArraySerializer
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ObjectArraySerializer
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.StringArraySerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import okhttp3.CacheControl
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.ByteString
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import org.jsoup.select.NodeVisitor
import org.luaj.vm2.Globals
import org.luaj.vm2.LocVars
import org.luaj.vm2.LuaBoolean
import org.luaj.vm2.LuaClosure
import org.luaj.vm2.LuaDouble
import org.luaj.vm2.LuaInteger
import org.luaj.vm2.LuaNil
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaThread
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Prototype
import org.luaj.vm2.UpValue
import org.luaj.vm2.Upvaldesc
import org.luaj.vm2.lib.BaseLib
import org.luaj.vm2.lib.Bit32Lib
import org.luaj.vm2.lib.CoroutineLib
import org.luaj.vm2.lib.MathLib
import org.luaj.vm2.lib.OsLib
import org.luaj.vm2.lib.PackageLib
import org.luaj.vm2.lib.StringLib
import org.luaj.vm2.lib.TableLib
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseMathLib
import org.luaj.vm2.lib.jse.JseOsLib
import org.luaj.vm2.lib.jse.LuajavaLib
import org.objenesis.strategy.StdInstantiatorStrategy
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import com.esotericsoftware.minlog.Log as KryoLog

@OptIn(ExperimentalEncodingApi::class)
class ListingSerializer : KSerializer<ListingSerializer.SerializableListing> {
	override val descriptor: SerialDescriptor
		get() = PrimitiveSerialDescriptor(
			"ExtensionListingSerializer",
			PrimitiveKind.STRING
		)

	private val kryo by lazy {
		KryoLog.setLogger(object : KryoLog.Logger() {
			override fun print(message: String) {
				Log.i("Kryo", message)
			}

			override fun log(level: Int, category: String?, message: String?, ex: Throwable?) {
				Log.println(level + 1, "Kryo", buildString {
					if (category != null) append('[').append(category).append("] ")
					if (message != null) append(message)
					if (ex != null) append('\n').append(Log.getStackTraceString(ex))
				})
			}
		})
		Kryo().apply {
			register<IExtension.Listing>()
			register<IExtension.Listing.List>()
			register<IExtension.Listing.Item>()
			register<Novel.Chapter>()
			register<Novel.Info>()
			register<LuaExtension.LuaStableFunction>()
			registerSingleton<Globals> { shosetsuGlobals() }
			registerSingleton<ShosetsuApplication.ShosetsuLibLoader.PrintLib> { ShosetsuApplication.ShosetsuLibLoader.PrintLib }
//			register<Globals>(object : FieldSerializer<Globals>(this, Globals::class.java) {
//				override fun create(kryo: Kryo?, input: Input?, type: Class<out Globals>?) = shosetsuGlobals()
//				override fun createCopy(kryo: Kryo?, original: Globals?) = throw UnsupportedOperationException()
//
//				override fun write(kryo: Kryo, output: Output, `object`: Globals) {
//					if (`object`.getmetatable()?.get("__index") !is Globals) {
//						output.writeBoolean(true)
//						return
//					}
//					output.writeBoolean(false)
//					super.write(kryo, output, `object`)
//				}
//
//				override fun read(kryo: Kryo, input: Input, type: Class<out Globals>): Globals {
//					return if (input.readBoolean()) shosetsuGlobals()
//					else super.read(kryo, input, type)
//				}
//
//				init {
//					removeField("STDIN")
//					removeField("STDOUT")
//					removeField("STDERR")
//					removeField("loader")
//					removeField("compiler")
//					removeField("undumper")
//				}
//			})

			register<IntArray>(IntArraySerializer())
			register<ByteArray>(ByteArraySerializer())
			register<Array<String>>(StringArraySerializer())
//			register<Random>()
//			register<WeakReference<*>>(object : Serializer<WeakReference<*>>() {
//				override fun write(kryo: Kryo, output: Output, `object`: WeakReference<*>) {
//					kryo.writeClassAndObject(output, `object`.get())
//				}
//
//				override fun read(kryo: Kryo, input: Input, type: Class<out WeakReference<*>>): WeakReference<*> {
//					return WeakReference(kryo.readClassAndObject(input))
//				}
//			})

			register<LuaBoolean>(object : Serializer<LuaBoolean>() {
				override fun write(kryo: Kryo, output: Output, `object`: LuaBoolean) = output.writeBoolean(`object`.booleanValue())
				override fun read(kryo: Kryo, input: Input, type: Class<out LuaBoolean>): LuaBoolean = LuaBoolean.valueOf(input.readBoolean())
			})
			register<LuaClosure>()
			register<LuaDouble>(object : Serializer<LuaDouble>() {
				override fun write(kryo: Kryo, output: Output, `object`: LuaDouble) = output.writeDouble(`object`.todouble())
				override fun read(kryo: Kryo, input: Input, type: Class<out LuaDouble>): LuaDouble = LuaDouble.valueOf(input.readDouble()) as LuaDouble
			})
			register<LuaInteger>(object : Serializer<LuaInteger>() {
				override fun write(kryo: Kryo, output: Output, `object`: LuaInteger) = output.writeInt(`object`.toint())
				override fun read(kryo: Kryo, input: Input, type: Class<out LuaInteger>): LuaInteger = LuaInteger.valueOf(input.readInt())
			})
			registerSingleton<LuaNil> { LuaValue.NIL as LuaNil }
			register(LuaValue.NONE.javaClass, SingletonSerializer { LuaValue.NONE })
			register<LuaString>()
			register<LuaThread>()
			register<LuaThread.State>()
			registerWithSubclasses<LuaTable>()
			registerArray<LuaValue>()
			register<Prototype>()
			registerArray<Prototype>()
			register<UpValue>()

			register<LocVars>()
			registerArray<LocVars>()
			register<Upvaldesc>()
			registerArray<Upvaldesc>()
			registerArray<UpValue>()
			registerArray(Class.forName("org.luaj.vm2.LuaTable\$Slot"))

			register(Class.forName("org.luaj.vm2.lib.jse.JavaArray"))
			register(Class.forName("org.luaj.vm2.lib.jse.JavaInstance"))
			register(Class.forName("org.luaj.vm2.lib.jse.JavaMethod"))

//			register(Class.forName("app.shosetsu.lib.lua.GlobalsKt\$frozen\$2"))
//			register(Class.forName("app.shosetsu.lib.lua.GlobalsKt\$frozen\$3"))

			register<Headers>()
			register<Headers.Builder>()
			register<RequestBody>()
			register<FormBody>()
			register<FormBody.Builder>()
			register<ByteString>()
			register<MultipartBody>()
			register<Request>()
			register<Request.Builder>()
			register<CacheControl>()
			register<MediaType>()
			register<Cookie>()
			register<Cookie.Builder>()
			registerSingleton<CookieManager> { CookieManager.getInstance() }
			register<CookieJar>()
			register<CookieJarSync>()
			register<ShosetsuLuaLib.LibFunctions.CustomCookieJar>()
			register<HttpUrl>()
			register<HttpUrl.Builder>()
			register<Interceptor>()
			register<ShosetsuLuaLib.LibFunctions.CustomInterceptor>()
			register<OkHttpClient>()
			register<OkHttpClient.Builder>()
			register<Response>()
			register<Response.Builder>()

			register<Attribute>()
			register<Attributes>()
			register<Node>()
			register<Document>()
			register<Element>()
			register<Elements>()
			register<NodeVisitor>()
			register<ShosetsuLuaLib.LibFunctions.CustomNodeVisitor>()

			registerWithSubclasses<Filter<*>>()

			registerWithSubclasses<BaseLib>()
			registerWithSubclasses<JseBaseLib>()
			registerWithSubclasses<PackageLib>()
			registerWithSubclasses<Bit32Lib>()
			registerWithSubclasses<TableLib>()
			registerWithSubclasses<StringLib>()
			registerWithSubclasses<CoroutineLib>()
			registerWithSubclasses<MathLib>()
			registerWithSubclasses<JseMathLib>()
			registerWithSubclasses<OsLib>()
			registerWithSubclasses<JseOsLib>()
			registerWithSubclasses<LuajavaLib>()

			instantiatorStrategy = StdInstantiatorStrategy()
			isRegistrationRequired = true
			warnUnregisteredClasses = true
			references = true
		}
	}

	private inline fun <reified T> Kryo.register() = register(T::class.java)
	private inline fun <reified T> Kryo.register(serializer: Serializer<T>) = register(T::class.java, serializer)
	private inline fun <reified T> Kryo.registerSingleton(noinline supplier: () -> T) = register(SingletonSerializer(supplier))
	class SingletonSerializer<T>(private val supplier: () -> T) : Serializer<T>() {
		override fun write(kryo: Kryo?, output: Output?, `object`: T) = Unit // Singleton
		override fun read(kryo: Kryo?, input: Input?, type: Class<out T>?): T = supplier()
	}
	private inline fun <reified T> Kryo.registerArray() = registerArray(T::class.java)
	private inline fun Kryo.registerArray(clazz: Class<*>) = Class.forName("[L${clazz.name};").let { register(it, ObjectArraySerializer(this, it)) }
	private inline fun <reified T> Kryo.registerWithSubclasses() = (listOf(T::class) + T::class.nestedClasses).forEach { register(it.java) }

	private val base64 = Base64.UrlSafe

	fun serializeBytes(value: IExtension.Listing?): ByteArray = if (value == null) byteArrayOf() else ByteArrayOutputStream().use { Output(it).use { output ->
		kryo.writeClassAndObject(output, value)
	}; it.toByteArray() }

	fun deserializeBytes(bytes: ByteArray): IExtension.Listing? = if (bytes.isEmpty()) null else Input(bytes).use { input ->
		kryo.readClassAndObject(input) as IExtension.Listing
	}

	override fun serialize(encoder: Encoder, value: SerializableListing) =
		encoder.encodeString(base64.encode(serializeBytes(value.listing)))

	override fun deserialize(decoder: Decoder): SerializableListing =
		SerializableListing(deserializeBytes(base64.decode(decoder.decodeString())))

	@Serializable(with = ListingSerializer::class)
	data class SerializableListing(val listing: IExtension.Listing?)

	object NavParameter : NavType<SerializableListing>(isNullableAllowed = false) {
		private val serializer by lazy { ListingSerializer() }

		override fun get(bundle: Bundle, key: String): SerializableListing {
			return bundle.getString(key)?.let { parseValue(it) } ?: SerializableListing(null)
		}

		override fun put(bundle: Bundle, key: String, value: SerializableListing) {
			bundle.putString(key, serializeAsValue(value))
		}

		override fun parseValue(value: String): SerializableListing {
			return if (value.isEmpty()) SerializableListing(null) else Json.decodeFromString(serializer, value)
		}

		override fun serializeAsValue(value: SerializableListing): String {
			return if (value.listing == null) "" else Json.encodeToString(serializer, value)
		}
	}
}
