package ch.njol.skript.events.wrapper

import ch.njol.skript.registrations.EventValues
import ch.njol.skript.util.Getter
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.event.trait.PlayerEvent

class PlayerSpawnWrapper(event: PlayerSpawnEvent) : PlayerEventWrapper<PlayerSpawnEvent>(event)
class PlayerQuitWrapper(event: PlayerDisconnectEvent) : PlayerEventWrapper<PlayerDisconnectEvent>(event)

abstract class PlayerEventWrapper<E : PlayerEvent>(event: E) : EventWrapper<E>(event) {
	companion object {
		init {
			EventValues.registerEventValue(
				PlayerEventWrapper::class.java,
				Player::class.java,
				object : Getter<Player, PlayerEventWrapper<*>>() {
					override fun get(event: PlayerEventWrapper<*>): Player {
						return event.event.player
					}

				},
				EventValues.TIME_NOW
			)
		}
	}
}