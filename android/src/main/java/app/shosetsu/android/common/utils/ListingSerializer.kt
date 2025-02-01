package app.shosetsu.android.common.utils

import android.os.Bundle
import android.util.Log
import androidx.navigation.NavType
import app.shosetsu.lib.IExtension
import app.shosetsu.lib.lua.LuaExtension
import app.shosetsu.lib.lua.shosetsuGlobals
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ByteArraySerializer
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.IntArraySerializer
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ObjectArraySerializer
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.StringArraySerializer
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.OkHttp
import org.luaj.vm2.Globals
import org.luaj.vm2.LocVars
import org.luaj.vm2.LuaBoolean
import org.luaj.vm2.LuaClosure
import org.luaj.vm2.LuaDouble
import org.luaj.vm2.LuaInteger
import org.luaj.vm2.LuaNil
import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Prototype
import org.luaj.vm2.UpValue
import org.luaj.vm2.Upvaldesc
import org.luaj.vm2.lib.BaseLib
import org.luaj.vm2.lib.Bit32Lib
import org.luaj.vm2.lib.CoroutineLib
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
            register<IExtension.Listing.List>()
            register<IExtension.Listing.Item>()
            register<LuaExtension.LuaStableFunction>()
            registerSingleton<Globals>(::shosetsuGlobals)

            register<IntArray>(IntArraySerializer())
            register<ByteArray>(ByteArraySerializer())
            register<Array<String>>(StringArraySerializer())

            register<LuaBoolean>()
            register<LuaClosure>()
            register<LuaDouble>()
            register<LuaInteger>()
            register<LuaNil>()
            register<LuaString>()
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

            register<Headers>()

            registerWithSubclasses<BaseLib>()
            registerWithSubclasses<JseBaseLib>()
            registerWithSubclasses<PackageLib>()
            registerWithSubclasses<Bit32Lib>()
            registerWithSubclasses<TableLib>()
            registerWithSubclasses<StringLib>()
            registerWithSubclasses<CoroutineLib>()
            registerWithSubclasses<JseMathLib>()
            registerWithSubclasses<JseOsLib>()
            registerWithSubclasses<LuajavaLib>()

            instantiatorStrategy = StdInstantiatorStrategy()
            isRegistrationRequired = true
            warnUnregisteredClasses = true
        }
    }

    private inline fun <reified T> Kryo.register() = register(T::class.java)
    private inline fun <reified T> Kryo.register(serializer: Serializer<T>) = register(T::class.java, serializer)
    private inline fun <reified T> Kryo.registerSingleton(crossinline supplier: () -> T) = register(object: Serializer<T>() {
        override fun write(kryo: Kryo?, output: Output?, `object`: T) {
            // Singleton
        }

        override fun read(kryo: Kryo?, input: Input?, type: Class<out T>?): T {
            return supplier()
        }
    })
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
