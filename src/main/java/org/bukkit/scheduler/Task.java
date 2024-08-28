package org.bukkit.scheduler;

import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class Task {
	private static int counter = 0;
	public boolean async;
	public Runnable runnable;
	public long delay;
	public @Nullable Long duration;
	public long ticksLeft;
	public int id;

	public Task(Runnable runnable, boolean async, long delay, @Nullable Long duration) {
		this.delay = delay;
		this.duration = duration;
		this.ticksLeft = delay;
		this.id = counter;
		this.runnable = runnable;
		this.async = async;
		counter++;
	}

	public boolean isRepeating() {
		return duration != null;
	}
}
