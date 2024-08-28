package org.bukkit.plugin;

public interface Plugin {
	PluginDescriptionFile getDescription();
	void setEnabled(boolean enabled);
	boolean isEnabled();
	String getName();

	void onEnable();
	void onDisable();
}
