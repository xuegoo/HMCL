/*
 * Hello Minecraft! Launcher.
 * Copyright (C) 2017  huangyuhui <huanghongxun2008@126.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */
package org.jackhuang.hmcl.setting

import com.google.gson.*
import javafx.beans.InvalidationListener
import org.jackhuang.hmcl.Main
import org.jackhuang.hmcl.game.LaunchOptions
import org.jackhuang.hmcl.util.*
import java.io.File
import java.io.IOException
import java.lang.reflect.Type

class VersionSetting() {

    var isGlobal: Boolean = false

    /**
     * HMCL Version Settings have been divided into 2 parts.
     * 1. Global settings.
     * 2. Version settings.
     * If a version claims that it uses global settings, its version setting will be disabled.
     *
     * Defaults false because if one version uses global first, custom version file will not be generated.
     */
    val usesGlobalProperty = ImmediateBooleanProperty(this, "usesGlobal", false)
    var usesGlobal: Boolean by usesGlobalProperty

    // java

    /**
     * Java version or null if user customizes java directory.
     */
    val javaProperty = ImmediateStringProperty(this, "java", "")
    var java: String by javaProperty

    /**
     * User customized java directory or null if user uses system Java.
     */
    val javaDirProperty = ImmediateStringProperty(this, "javaDir", "")
    var javaDir: String by javaDirProperty

    /**
     * The command to launch java, i.e. optirun.
     */
    val wrapperProperty = ImmediateStringProperty(this, "wrapper", "")
    var wrapper: String by wrapperProperty

    /**
     * The permanent generation size of JVM garbage collection.
     */
    val permSizeProperty = ImmediateStringProperty(this, "permSize", "")
    var permSize: String by permSizeProperty

    /**
     * The maximum memory that JVM can allocate for heap.
     */
    val maxMemoryProperty = ImmediateIntegerProperty(this, "maxMemory", OS.SUGGESTED_MEMORY)
    var maxMemory: Int by maxMemoryProperty

    /**
     * The minimum memory that JVM can allocate for heap.
     */
    val minMemoryProperty = ImmediateObjectProperty<Int?>(this, "minMemory", null)
    var minMemory: Int? by minMemoryProperty

    /**
     * The command that will be executed before launching the Minecraft.
     * Operating system relevant.
     */
    val precalledCommandProperty = ImmediateStringProperty(this, "precalledCommand", "")
    var precalledCommand: String by precalledCommandProperty

    // options

    /**
     * The user customized arguments passed to JVM.
     */
    val javaArgsProperty = ImmediateStringProperty(this, "javaArgs", "")
    var javaArgs: String by javaArgsProperty

    /**
     * The user customized arguments passed to Minecraft.
     */
    val minecraftArgsProperty = ImmediateStringProperty(this, "minecraftArgs", "")
    var minecraftArgs: String by minecraftArgsProperty

    /**
     * True if disallow HMCL use default JVM arguments.
     */
    val noJVMArgsProperty = ImmediateBooleanProperty(this, "noJVMArgs", false)
    var noJVMArgs: Boolean by noJVMArgsProperty

    /**
     * True if HMCL does not check game's completeness.
     */
    val notCheckGameProperty = ImmediateBooleanProperty(this, "notCheckGame", false)
    var notCheckGame: Boolean by notCheckGameProperty

    /**
     * True if HMCL does not find/download libraries in/to common path.
     */
    val noCommonProperty = ImmediateBooleanProperty(this, "noCommon", false)
    var noCommon: Boolean by noCommonProperty

    /**
     * True if show the logs after game launched.
     */
    val showLogsProperty = ImmediateBooleanProperty(this, "showLogs", false)
    var showLogs: Boolean by showLogsProperty

    // Minecraft settings.

    /**
     * The server ip that will be entered after Minecraft successfully loaded immediately.
     *
     * Format: ip:port or without port.
     */
    val serverIpProperty = ImmediateStringProperty(this, "serverIp", "")
    var serverIp: String by serverIpProperty

    /**
     * True if Minecraft started in fullscreen mode.
     */
    val fullscreenProperty = ImmediateBooleanProperty(this, "fullscreen", false)
    var fullscreen: Boolean by fullscreenProperty

    /**
     * The width of Minecraft window, defaults 800.
     *
     * The field saves int value.
     * String type prevents unexpected value from causing JsonSyntaxException.
     * We can only reset this field instead of recreating the whole setting file.
     */
    val widthProperty = ImmediateIntegerProperty(this, "width", 854)
    var width: Int by widthProperty


    /**
     * The height of Minecraft window, defaults 480.
     *
     * The field saves int value.
     * String type prevents unexpected value from causing JsonSyntaxException.
     * We can only reset this field instead of recreating the whole setting file.
     */
    val heightProperty = ImmediateIntegerProperty(this, "height", 480)
    var height: Int by heightProperty


    /**
     * 0 - .minecraft<br/>
     * 1 - .minecraft/versions/&lt;version&gt;/<br/>
     */
    val gameDirTypeProperty = ImmediateObjectProperty<EnumGameDirectory>(this, "gameDirTypeProperty", EnumGameDirectory.ROOT_FOLDER)
    var gameDirType: EnumGameDirectory by gameDirTypeProperty

    // launcher settings

    /**
     * 0 - Close the launcher when the game starts.<br/>
     * 1 - Hide the launcher when the game starts.<br/>
     * 2 - Keep the launcher open.<br/>
     */
    val launcherVisibilityProperty = ImmediateObjectProperty<LauncherVisibility>(this, "launcherVisibility", LauncherVisibility.HIDE)
    var launcherVisibility: LauncherVisibility by launcherVisibilityProperty

    val javaVersion: JavaVersion? get() {
        // TODO: lazy initialization may result in UI suspension.
        if (java.isBlank())
            java = if (javaDir.isBlank()) "Default" else "Custom"
        if (java == "Default") return JavaVersion.fromCurrentEnvironment()
        else if (java == "Custom") {
            try {
                return JavaVersion.fromExecutable(File(javaDir))
            } catch (e: IOException) {
                return null // Custom Java Directory not found,
            }
        } else if (java.isNotBlank()) {
            val c = JavaVersion.getJREs()[java]
            if (c == null) {
                java = "Default"
                return JavaVersion.fromCurrentEnvironment()
            } else
                return c
        } else throw Error()
    }

    fun addPropertyChangedListener(listener: InvalidationListener) {
        usesGlobalProperty.addListener(listener)
        javaProperty.addListener(listener)
        javaDirProperty.addListener(listener)
        wrapperProperty.addListener(listener)
        permSizeProperty.addListener(listener)
        maxMemoryProperty.addListener(listener)
        minMemoryProperty.addListener(listener)
        precalledCommandProperty.addListener(listener)
        javaArgsProperty.addListener(listener)
        minecraftArgsProperty.addListener(listener)
        noJVMArgsProperty.addListener(listener)
        notCheckGameProperty.addListener(listener)
        noCommonProperty.addListener(listener)
        showLogsProperty.addListener(listener)
        serverIpProperty.addListener(listener)
        fullscreenProperty.addListener(listener)
        widthProperty.addListener(listener)
        heightProperty.addListener(listener)
        gameDirTypeProperty.addListener(listener)
        launcherVisibilityProperty.addListener(listener)
    }

    @Throws(IOException::class)
    fun toLaunchOptions(gameDir: File): LaunchOptions {
        return LaunchOptions(
                gameDir = gameDir,
                java = javaVersion ?: JavaVersion.fromCurrentEnvironment(),
                versionName = Main.TITLE,
                profileName = Main.TITLE,
                minecraftArgs = minecraftArgs,
                javaArgs = javaArgs,
                maxMemory = maxMemory,
                minMemory = minMemory,
                metaspace = permSize.toIntOrNull(),
                width = width,
                height = height,
                fullscreen = fullscreen,
                serverIp = serverIp,
                wrapper = wrapper,
                proxyHost = Settings.proxyHost,
                proxyPort = Settings.proxyPort,
                proxyUser = Settings.proxyUser,
                proxyPass = Settings.proxyPass,
                precalledCommand = precalledCommand,
                noGeneratedJVMArgs = noJVMArgs
        )
    }

    companion object Serializer: JsonSerializer<VersionSetting>, JsonDeserializer<VersionSetting> {
        override fun serialize(src: VersionSetting?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            if (src == null) return JsonNull.INSTANCE
            val jsonObject = JsonObject()
            with(jsonObject) {
                addProperty("usesGlobal", src.usesGlobal)
                addProperty("javaArgs", src.javaArgs)
                addProperty("minecraftArgs", src.minecraftArgs)
                addProperty("maxMemory", if (src.maxMemory <= 0) OS.SUGGESTED_MEMORY else src.maxMemory)
                addProperty("minMemory", src.minMemory)
                addProperty("permSize", src.permSize)
                addProperty("width", src.width)
                addProperty("height", src.height)
                addProperty("javaDir", src.javaDir)
                addProperty("precalledCommand", src.precalledCommand)
                addProperty("serverIp", src.serverIp)
                addProperty("java", src.java)
                addProperty("wrapper", src.wrapper)
                addProperty("fullscreen", src.fullscreen)
                addProperty("noJVMArgs", src.noJVMArgs)
                addProperty("notCheckGame", src.notCheckGame)
                addProperty("noCommon", src.noCommon)
                addProperty("showLogs", src.showLogs)
                addProperty("launcherVisibility", src.launcherVisibility.ordinal)
                addProperty("gameDirType", src.gameDirType.ordinal)
            }

            return jsonObject
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): VersionSetting? {
            if (json == null || json == JsonNull.INSTANCE || json !is JsonObject) return null

            var maxMemoryN = parseJsonPrimitive(json["maxMemory"]?.asJsonPrimitive, OS.SUGGESTED_MEMORY)
            if (maxMemoryN <= 0) maxMemoryN = OS.SUGGESTED_MEMORY

            return VersionSetting().apply {
                usesGlobal = json["usesGlobal"]?.asBoolean ?: false
                javaArgs = json["javaArgs"]?.asString ?: ""
                minecraftArgs = json["minecraftArgs"]?.asString ?: ""
                maxMemory = maxMemoryN
                minMemory = json["minMemory"]?.asInt
                permSize = json["permSize"]?.asString ?: ""
                width = parseJsonPrimitive(json["width"]?.asJsonPrimitive)
                height = parseJsonPrimitive(json["height"]?.asJsonPrimitive)
                javaDir = json["javaDir"]?.asString ?: ""
                precalledCommand = json["precalledCommand"]?.asString ?: ""
                serverIp = json["serverIp"]?.asString ?: ""
                java = json["java"]?.asString ?: ""
                wrapper = json["wrapper"]?.asString ?: ""
                fullscreen = json["fullscreen"]?.asBoolean ?: false
                noJVMArgs = json["noJVMArgs"]?.asBoolean ?: false
                notCheckGame = json["notCheckGame"]?.asBoolean ?: false
                noCommon = json["noCommon"]?.asBoolean ?: false
                showLogs = json["showLogs"]?.asBoolean ?: false
                launcherVisibility = LauncherVisibility.values()[json["launcherVisibility"]?.asInt ?: 1]
                gameDirType = EnumGameDirectory.values()[json["gameDirType"]?.asInt ?: 0]
            }
        }

        fun parseJsonPrimitive(primitive: JsonPrimitive?, defaultValue: Int = 0): Int {
            if (primitive != null)
                if (primitive.isNumber)
                    return primitive.asInt
                else
                    return primitive.asString.toIntOrNull() ?: defaultValue
            else
                return defaultValue
        }

    }
}