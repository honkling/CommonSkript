package org.bukkit.plugin;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public interface EventExecutor {
	void execute(@NotNull Listener listener, @NotNull Event event);
}
