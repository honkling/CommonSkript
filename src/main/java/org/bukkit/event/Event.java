package org.bukkit.event;

public abstract class Event {
	public enum Result {
		ALLOW,
		DEFAULT,
		DENY
	}

	public abstract HandlerList getHandlers();
}
