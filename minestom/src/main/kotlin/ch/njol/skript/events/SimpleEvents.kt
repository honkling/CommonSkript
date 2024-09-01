package ch.njol.skript.events

import ch.njol.skript.Skript
import ch.njol.skript.events.wrapper.PlayerQuitWrapper
import ch.njol.skript.events.wrapper.PlayerSpawnWrapper
import ch.njol.skript.lang.util.SimpleEvent

object SimpleEvents {
	init {
		Skript.registerEvent("Player Join", SimpleEvent::class.java, PlayerSpawnWrapper::class.java,
			"player (join|spawn)")
		Skript.registerEvent("Player Quit", SimpleEvent::class.java, PlayerQuitWrapper::class.java,
			"player (quit|leave|disconnect)")
	}
}