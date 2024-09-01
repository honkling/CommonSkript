package org.bukkit.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class BukkitScheduler {
    private final Map<Integer, Task> tasks = new HashMap<>();
	private final List<Task> priority = new ArrayList<>();
	private Task currentTask = null;

	public BukkitScheduler() {
        Ticker ticker = Bukkit.getTicker();
		ticker.initialize(this::tick);
	}

	public void tick() {
		for (int taskID : tasks.keySet()) {
			Task task = tasks.get(taskID);
			task.ticksLeft--;

			if (task.ticksLeft <= 0) {
				currentTask = task;

				if (task.async)
					new Thread(task.runnable).start();
				else task.runnable.run();

				currentTask = null;
				task.ticksLeft = task.isRepeating() ? task.duration : task.delay;

				if (!task.isRepeating())
					tasks.remove(taskID);
			}
		}
	}

	public int runTaskLaterAsynchronously(Plugin _plugin, Runnable runnable, long delay) {
		return scheduleTask(runnable, true, delay, null);
	}

	public int scheduleSyncDelayedTask(Plugin _plugin, Runnable runnable, long delay) {
		return scheduleTask(runnable, false, delay, null);
	}

	public int runTaskTimerAsynchronously(Plugin _plugin, Runnable runnable, long delay, long duration) {
		return scheduleTask(runnable, true, delay, duration);
	}

	public int scheduleSyncRepeatingTask(Plugin _plugin, Runnable runnable, long delay, long duration) {
		return scheduleTask(runnable, false, delay, duration);
	}

	public boolean isQueued(int taskID) {
		return tasks.containsKey(taskID);
	}

	public boolean isCurrentlyRunning(int taskID) {
		if (!tasks.containsKey(taskID))
			return false;

		Task task = tasks.get(taskID);
		return task.isRepeating() || (!task.async && currentTask == task);
	}

	public void cancelTask(int taskID) {
		tasks.remove(taskID);
	}

	public <T> Future<T> callSyncMethod(Plugin _plugin, Callable<T> task) {
		FutureTask<T> future = new FutureTask<>(task);
		scheduleTask(future, false, 0L, null);
		return future;
	}

	private int scheduleTask(Runnable runnable, boolean async, long delay, @Nullable Long duration) {
		Task task = new Task(runnable, async, delay, duration);
		tasks.put(task.id, task);
		return task.id;
	}
}
