# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/funky/workspace/android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-dontskipnonpubliclibraryclasses
-repackageclasses ''

# okio
-dontwarn okio.**

# retrofit2
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# RxJava
-dontwarn rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef
-dontwarn rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef
-dontwarn rx.internal.util.unsafe.ConcurrentCircularArrayQueue
-dontwarn rx.internal.util.unsafe.ConcurrentSequencedCircularArrayQueue
-dontwarn rx.internal.util.unsafe.MpmcArrayQueueConsumerField
-dontwarn rx.internal.util.unsafe.MpmcArrayQueueProducerField
-dontwarn rx.internal.util.unsafe.MpscLinkedQueue
-dontwarn rx.internal.util.unsafe.SpmcArrayQueueConsumerField
-dontwarn rx.internal.util.unsafe.SpmcArrayQueueProducerField
-dontwarn rx.internal.util.unsafe.SpscArrayQueue
-dontwarn rx.internal.util.unsafe.SpscUnboundedArrayQueue
-dontwarn rx.internal.util.unsafe.UnsafeAccess