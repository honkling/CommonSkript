package org.bukkit.plugin;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PluginDescriptionFile {
	private String name;
	private String version;
	private String main;
	private @Nullable String website;
	private List<String> depend;
	private List<String> softDepend;

	public PluginDescriptionFile(String name, String version, String main, @Nullable String website, List<String> depend, List<String> softDepend) {
		this.name = name;
		this.version = version;
		this.main = main;
		this.website = website;
		this.depend = depend;
		this.softDepend = softDepend;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public List<String> getDepend() {
		return depend;
	}

	public List<String> getSoftDepend() {
		return softDepend;
	}

	public String getMain() {
		return main;
	}

	public String getFullName() {
		return getName() + " v" + getVersion();
	}

	public @Nullable String getWebsite() {
		return website;
	}
}
