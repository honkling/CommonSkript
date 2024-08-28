package org.bukkit.event;

import java.util.ArrayList;
import java.util.List;

public class HandlerList {
	private final List<RegisteredListener> listeners = new ArrayList<>();

	public void register(RegisteredListener handler) {
		listeners.add(handler);
	}

	public void unregister(Listener listener) {
		listeners.removeIf((registeredListener) -> registeredListener.getListener() == listener);
	}

	public List<RegisteredListener> getRegisteredListeners() {
		return listeners;
	}
}
