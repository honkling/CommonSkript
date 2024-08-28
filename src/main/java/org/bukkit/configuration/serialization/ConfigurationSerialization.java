package org.bukkit.configuration.serialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationSerialization {
	private static final Map<String, Class<? extends ConfigurationSerializable>> aliases = new HashMap<>();

	public static void registerClass(@NotNull Class<? extends ConfigurationSerializable> clazz, String alias) {
		aliases.put(alias, clazz);
	}

	@Nullable
	public static Class<? extends ConfigurationSerializable> getClassByAlias(String alias) {
		return aliases.get(alias);
	}

	public static String getAlias(@NotNull Class<? extends ConfigurationSerializable> clazz) {
		return clazz.getName();
	}
}
