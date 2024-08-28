package ch.njol.skript.events.custom;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ScriptLoadEvent extends Event {
	private static HandlerList handlerList = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlerList;
	}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
}
