package org.bukkit.configuration.file;

import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

public class YamlConfiguration {
	private static final Yaml yaml = new Yaml();
	private Map<String, Object> document = new HashMap<>();

	public void loadFromString(String input) {
		this.document = yaml.load(input);
	}

	public String saveToString() {
		return yaml.dump(document);
	}

	public void set(String key, Object value) {
		document.put(key, value); // expand on this later
	}

	public Object get(String key) {
		return document.get(key);
	}
}
