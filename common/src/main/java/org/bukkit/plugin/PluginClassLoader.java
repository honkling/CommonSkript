package org.bukkit.plugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class PluginClassLoader extends URLClassLoader {
	private PluginDescriptionFile description;
	private PluginManager pluginManager;
	private File file;

	public PluginClassLoader(PluginManager pluginManager, File file, ClassLoader parent) throws MalformedURLException {
		super(new URL[] { file.toURI().toURL() }, parent);

		this.pluginManager = pluginManager;
		this.file = file;
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass0(name, resolve, true);
	}

	private Class<?> loadClass0(String name, boolean resolve, boolean checkPlugins) throws ClassNotFoundException {
		try {
			return super.loadClass(name, resolve);
		} catch (ClassNotFoundException ignored) {}

		if (checkPlugins) {
			for (Plugin plugin : pluginManager.getPlugins()) {
				if (!(plugin instanceof JavaPlugin))
					continue;

				JavaPlugin javaPlugin = (JavaPlugin) plugin;

				try {
					Class<?> clazz = javaPlugin.getLoader().loadClass0(name, resolve, false);
					ClassLoader loader = clazz.getClassLoader();

					if (loader instanceof PluginClassLoader) {
						PluginClassLoader pluginLoader = (PluginClassLoader) loader;
						PluginDescriptionFile description = pluginLoader.getDescription();
						String pluginName = description.getName();

						if (description == null)
							return clazz;

						PluginDescriptionFile thisDescription = plugin.getDescription();

						if ((thisDescription.getDepend().contains(pluginName) || thisDescription.getSoftDepend().contains(pluginName)) || thisDescription.getName().equals(pluginName))
							return clazz;

						Bukkit.getLogger().warning(String.format(
							"Plugin '%s' loaded class '%s' from non-dependency plugin '%s'.",
							thisDescription.getName(),
							clazz.getName(),
							description.getName()
						));
					}
				} catch (ClassNotFoundException ignored) {
				}
			}
		}

		throw new ClassNotFoundException(name);
	}

	public @Nullable PluginDescriptionFile getDescription() {
		if (description != null)
			return description;

		InputStream stream = getResourceAsStream("plugin.yml");

		if (stream == null) {
			Bukkit.getLogger().warning(String.format(
				"Found JAR '%s' in the plugins folder without a plugin.yml file.",
				file.getName()
			));
			return null;
		}

		try {
			YamlConfiguration configuration = new YamlConfiguration();
			configuration.loadFromString(new String(stream.readAllBytes(), StandardCharsets.UTF_8));

			return new PluginDescriptionFile(
				(String) configuration.get("name"),
				(String) configuration.get("version"),
				(String) configuration.get("main"),
				Objects.toString(configuration.get("website"), null),
				(List<String>) configuration.get("depend"),
				(List<String>) configuration.get("softdepend")
			);
		} catch (IOException | ClassCastException exception) {
			exception.printStackTrace();
			return null;
		}
	}
}
