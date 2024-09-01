package org.bukkit.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public class DefaultTicker implements Ticker {
	@Override
	public void initialize(@NotNull Runnable tick) {
		Timer tickTimer = new Timer(true);
		tickTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				tick.run();
			}
		}, 0, 50);
	}
}
