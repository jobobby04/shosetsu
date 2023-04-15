# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Open source app, lol who cares about obfuscating
-dontobfuscate

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.* {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.* {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class app.shosetsu.android.common.* { *; }

# Keep lib intact
-keepclasseswithmembers class app.shosetsu.lib.** { *; }
-keepclasseswithmembers class org.luaj.** { *; }
-keepclasseswithmembers class org.jsoup.** { *; }
-keepclasseswithmembers class okhttp3.** { *; }

-keep,includedescriptorclasses class app.shosetsu.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class app.shosetsu.* { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class app.shosetsu.* { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}

# Ignore java packages as included by qrcode BufferedImageCanvas
-dontwarn java.awt.Color
-dontwarn java.awt.Graphics2D
-dontwarn java.awt.Image
-dontwarn java.awt.RenderingHints$Key
-dontwarn java.awt.RenderingHints
-dontwarn java.awt.image.BufferedImage
-dontwarn java.awt.image.ImageObserver
-dontwarn java.awt.image.RenderedImage
-dontwarn javax.imageio.ImageIO

# Ignore lua script engine, we directly call it
-dontwarn javax.script.ScriptEngineFactory

# Seems to be related to okhttp3, not too important i think
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier

# Related to DateTimeZone from joda time
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString

# More related to okhttp3
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Ignore javax.scrpt
-dontwarn javax.script.AbstractScriptEngine
-dontwarn javax.script.Bindings
-dontwarn javax.script.Compilable
-dontwarn javax.script.CompiledScript
-dontwarn javax.script.ScriptContext
-dontwarn javax.script.ScriptEngine
-dontwarn javax.script.ScriptException
-dontwarn javax.script.SimpleBindings
-dontwarn javax.script.SimpleScriptContext

# Ignore apache.bcel errors
-dontwarn org.apache.bcel.classfile.Field
-dontwarn org.apache.bcel.classfile.JavaClass
-dontwarn org.apache.bcel.classfile.Method
-dontwarn org.apache.bcel.generic.AASTORE
-dontwarn org.apache.bcel.generic.ALOAD
-dontwarn org.apache.bcel.generic.ANEWARRAY
-dontwarn org.apache.bcel.generic.ASTORE
-dontwarn org.apache.bcel.generic.ArrayInstruction
-dontwarn org.apache.bcel.generic.ArrayType
-dontwarn org.apache.bcel.generic.BasicType
-dontwarn org.apache.bcel.generic.BranchHandle
-dontwarn org.apache.bcel.generic.BranchInstruction
-dontwarn org.apache.bcel.generic.ClassGen
-dontwarn org.apache.bcel.generic.CompoundInstruction
-dontwarn org.apache.bcel.generic.ConstantPoolGen
-dontwarn org.apache.bcel.generic.FieldGen
-dontwarn org.apache.bcel.generic.FieldInstruction
-dontwarn org.apache.bcel.generic.GETSTATIC
-dontwarn org.apache.bcel.generic.GOTO
-dontwarn org.apache.bcel.generic.IFEQ
-dontwarn org.apache.bcel.generic.IFNE
-dontwarn org.apache.bcel.generic.Instruction
-dontwarn org.apache.bcel.generic.InstructionConstants
-dontwarn org.apache.bcel.generic.InstructionFactory
-dontwarn org.apache.bcel.generic.InstructionHandle
-dontwarn org.apache.bcel.generic.InstructionList
-dontwarn org.apache.bcel.generic.InvokeInstruction
-dontwarn org.apache.bcel.generic.LineNumberGen
-dontwarn org.apache.bcel.generic.LocalVariableGen
-dontwarn org.apache.bcel.generic.LocalVariableInstruction
-dontwarn org.apache.bcel.generic.MethodGen
-dontwarn org.apache.bcel.generic.NEW
-dontwarn org.apache.bcel.generic.ObjectType
-dontwarn org.apache.bcel.generic.PUSH
-dontwarn org.apache.bcel.generic.PUTSTATIC
-dontwarn org.apache.bcel.generic.ReturnInstruction
-dontwarn org.apache.bcel.generic.StackInstruction
-dontwarn org.apache.bcel.generic.Type

# Kodein
-keep, allowobfuscation, allowoptimization class org.kodein.type.TypeReference
-keep, allowobfuscation, allowoptimization class org.kodein.type.JVMAbstractTypeToken$Companion$WrappingTest

-keep, allowobfuscation, allowoptimization class * extends org.kodein.type.TypeReference
-keep, allowobfuscation, allowoptimization class * extends org.kodein.type.JVMAbstractTypeToken$Companion$WrappingTest

