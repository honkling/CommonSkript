package org.bukkit;

import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Bukkit {
	private static final Thread primaryThread = Thread.currentThread();
	private static final PluginManager pluginManager = new PluginManager();
	private static final BukkitScheduler scheduler = new BukkitScheduler();
	private static final Logger logger = Logger.getLogger("Bukkit");

	public static PluginManager getPluginManager() {
		return pluginManager;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static BukkitScheduler getScheduler() {
		return scheduler;
	}

	public static boolean isPrimaryThread() {
		return Thread.currentThread().equals(primaryThread);
	}
}
