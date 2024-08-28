package ch.njol.skript;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class Main {
	public static void main(String[] args) throws URISyntaxException {
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.loadPlugin(getFile());

		for (Plugin plugin : pluginManager.getPlugins()) {
			plugin.setEnabled(true);
			plugin.onEnable();
		}
	}

	private static File getFile() throws URISyntaxException {
		URI uri = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		return new File(uri);
	}
}
