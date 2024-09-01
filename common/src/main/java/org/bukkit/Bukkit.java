package org.bukkit;

import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.DefaultTicker;
import org.bukkit.scheduler.Ticker;
import org.jetbrains.annotations.NotNull;

import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Bukkit {
	private static final Thread primaryThread = Thread.currentThread();
	private static final PluginManager pluginManager = new PluginManager();
	private static final Logger logger = Logger.getLogger("Bukkit");
	private static BukkitScheduler scheduler = null;
	private static Ticker ticker = new DefaultTicker();

	public static PluginManager getPluginManager() {
		return pluginManager;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static @NotNull BukkitScheduler getScheduler() {
		if (scheduler == null)
			scheduler = new BukkitScheduler();

		return scheduler;
	}

	public static boolean isPrimaryThread() {
		return Thread.currentThread().equals(primaryThread);
	}

	public static Thread getPrimaryThread() {
		return primaryThread;
	}

	public static Ticker getTicker() {
		return ticker;
	}

	public static void setTicker(Ticker ticker) {
		Bukkit.ticker = ticker;
	}
}
