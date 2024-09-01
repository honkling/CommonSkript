package org.skriptlang.skript.minestom

import ch.njol.skript.Skript
import ch.njol.skript.events.wrapper.EventWrapper
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.Event
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.block.Block
import org.bukkit.Bukkit
import org.bukkit.scheduler.DefaultTicker
import org.skriptlang.skript.minestom.impl.MinestomTicker
import org.skriptlang.skript.minestom.impl.ProxyTicker
import org.skriptlang.skript.minestom.registration.registerFunctions
import org.skriptlang.skript.minestom.registration.registerClasses
import java.io.File

fun main() {
	val server = MinecraftServer.init()

	val instanceManager = MinecraftServer.getInstanceManager()
	val container = instanceManager.createInstanceContainer()

	container.setGenerator {
		it.modifier().fillHeight(0, 40, Block.GRASS_BLOCK)
	}

	val events = MinecraftServer.getGlobalEventHandler()
	events.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
		val player = event.player
		event.spawningInstance = container
		player.respawnPoint  = Pos(0.0, 42.0, 0.0)
	}

	val pluginManager = Bukkit.getPluginManager()
	Runtime.getRuntime().addShutdownHook(Thread {
		val skript = pluginManager.plugins.find { it.name == "Skript" } ?: return@Thread
		skript.onDisable()
	})

	startSkript()
	server.start("0.0.0.0", 25565)
}

fun startSkript() {
	Bukkit.getScheduler()
	val pluginManager = Bukkit.getPluginManager()
	val skript = pluginManager.loadPlugin(File(Skript::class.java.protectionDomain.codeSource.location.toURI()))

	Skript.onRegistration {
		println("hello")
		registerClasses()
		registerFunctions()
	}

	skript.isEnabled = true
	skript.onEnable()

	val events = MinecraftServer.getGlobalEventHandler()

	for (event in Skript.getEvents())
		for (eventType in event.events)
			if (EventWrapper::class.java.isAssignableFrom(eventType)) {
				val constructor = eventType.declaredConstructors.find { it.parameterCount == 1 && Event::class.java.isAssignableFrom(it.parameterTypes[0]) }
					?: return
				val type = constructor.parameterTypes[0] as Class<out Event>

				events.addListener(type) {
					val wrapper = constructor.newInstance(it) as EventWrapper<*>
					pluginManager.callEvent(wrapper)
				}
			}
}