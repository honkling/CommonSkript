package org.bukkit.event;

import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class RegisteredListener {
	private Plugin plugin;
	private EventExecutor executor;
	private EventPriority priority;
	private boolean ignoreCancelled;
	private Listener listener;

	public RegisteredListener(Plugin plugin, Listener listener, Method method) {
		this.executor = (_listener, event) -> {
			try {
				method.invoke(event);
			} catch (ReflectiveOperationException exception) {
				exception.printStackTrace();
			}
		};

		EventHandler handler = method.getAnnotation(EventHandler.class);
		ignoreCancelled = handler.ignoreCancelled();
		priority = handler.priority();
		this.plugin = plugin;
		this.listener = listener;
	}

	public RegisteredListener(Plugin plugin, EventExecutor executor, EventPriority priority, boolean ignoreCancelled, Listener listener) {
		this.plugin = plugin;
		this.executor = executor;
		this.priority = priority;
		this.ignoreCancelled = ignoreCancelled;
		this.listener = listener;
	}

	public boolean isIgnoreCancelled() {
		return ignoreCancelled;
	}

	public EventExecutor getExecutor() {
		return executor;
	}

	public EventPriority getPriority() {
		return priority;
	}

	public Listener getListener() {
		return listener;
	}

	public Plugin getPlugin() {
		return plugin;
	}
}
