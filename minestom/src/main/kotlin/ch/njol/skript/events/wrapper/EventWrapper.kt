package ch.njol.skript.events.wrapper

import net.minestom.server.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.Event as BukkitEvent

private val handlerList = HandlerList()

abstract class EventWrapper<E : Event>(val event: E) : BukkitEvent() {
    companion object {
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }
}