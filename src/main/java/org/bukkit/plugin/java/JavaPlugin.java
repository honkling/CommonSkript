package org.bukkit.plugin.java;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginClassLoader;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class JavaPlugin implements Plugin {
	private PluginClassLoader loader;
	private PluginDescriptionFile description;
	private boolean enabled = false;

	public final void init(PluginDescriptionFile description, PluginClassLoader loader) {
		this.description = description;
		this.loader = loader;
	}

	public @Nullable InputStream getResource(@NotNull String filename) {
		return this.getClass().getResourceAsStream("/" + filename);
	}

	public File getDataFolder() {
		return new File("run/" + getName());
	}

	protected File getFile() {
		try {
			URI path = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
			return new File(path);
		} catch (URISyntaxException ignored) {
			return new File(".");
		}
	}

	@Override
	public String getName() {
		return description.getName();
	}

	@Override
	public PluginDescriptionFile getDescription() {
		return description;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public PluginClassLoader getLoader() {
		return loader;
	}

	public static @Nullable JavaPlugin getProvidingPlugin(Class<?> clazz) {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (!(plugin instanceof JavaPlugin))
				continue;

			JavaPlugin javaPlugin = (JavaPlugin) plugin;

			try {
				javaPlugin.getLoader().loadClass(clazz.getName());
				return javaPlugin;
			} catch (ClassNotFoundException ignored) {}
		}

		return null;
	}
}
