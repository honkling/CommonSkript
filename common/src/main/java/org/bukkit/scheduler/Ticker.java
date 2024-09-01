package org.bukkit.scheduler;

import org.jetbrains.annotations.NotNull;

public interface Ticker {
	void initialize(@NotNull Runnable tick);
}
