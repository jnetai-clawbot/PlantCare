# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.jnetai.plantcare.data.entity.** { *; }
-keep class com.jnetai.plantcare.data.dao.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory