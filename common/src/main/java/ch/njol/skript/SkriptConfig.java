/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.EnumParser;
import ch.njol.skript.config.Option;
import ch.njol.skript.config.OptionSection;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.update.ReleaseChannel;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Version;
import ch.njol.skript.variables.Variables;
import org.bukkit.event.EventPriority;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * Important: don't save values from the config, a '/skript reload config/configs/all' won't work correctly otherwise!
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("unused")
public class SkriptConfig {

	@Nullable
	static Config mainConfig;
	static Collection<Config> configs = new ArrayList<>();
	
	static final Option<String> version = new Option<>("version", Skript.getVersion().toString())
			.optional(true);
	
	public static final Option<String> language = new Option<>("language", "english")
			.optional(true)
			.setter(s -> {
				if (!Language.load(s)) {
					Skript.error("No language file found for '" + s + "'!");
				}
			});
	
	static final Option<Boolean> checkForNewVersion = new Option<>("check for new version", false)
			.setter(t -> {
				SkriptUpdater updater = Skript.getInstance().getUpdater();
				if (updater != null)
					updater.setEnabled(t);
			});
	static final Option<Timespan> updateCheckInterval = new Option<>("update check interval", new Timespan(12 * 60 * 60 * 1000))
			.setter(t -> {
				SkriptUpdater updater = Skript.getInstance().getUpdater();
				if (updater != null)
					updater.setCheckFrequency(t.getTicks());
			});
	static final Option<Integer> updaterDownloadTries = new Option<>("updater download tries", 7)
			.optional(true);
	static final Option<String> releaseChannel = new Option<>("release channel", "none")
			.setter(t -> {
				ReleaseChannel channel;
				switch (t) {
					case "alpha":
					case "beta":
						Skript.warning("'alpha' and 'beta' are no longer valid release channels. Use 'prerelease' instead.");
					case "prerelease": // All development builds are valid
						channel = new ReleaseChannel((name) -> true, t);
						break;
					case "stable":
						// TODO a better option would be to check that it is not a pre-release through GH API
						channel = new ReleaseChannel((name) -> !(name.contains("-")), t);
						break;
					case "none":
						channel = new ReleaseChannel((name) -> false, t);
						break;
					default:
						channel = new ReleaseChannel((name) -> false, t);
						Skript.error("Unknown release channel '" + t + "'.");
						break;
				}
				SkriptUpdater updater = Skript.getInstance().getUpdater();
				if (updater != null) {
					updater.setReleaseChannel(channel);
				}
			});

	public static final Option<Boolean> enableEffectCommands = new Option<>("enable effect commands", false);
	public static final Option<String> effectCommandToken = new Option<>("effect command token", "!");
	public static final Option<Boolean> allowOpsToUseEffectCommands = new Option<>("allow ops to use effect commands", false);

	/*
	 * @deprecated Will be removed in 2.8.0. Use {@link #logEffectCommands} instead.
	 */
	@Deprecated
	public static final Option<Boolean> logPlayerCommands = new Option<>("log player commands", false).optional(true);
	public static final Option<Boolean> logEffectCommands = new Option<>("log effect commands", false);

	// everything handled by Variables
	public static final OptionSection databases = new OptionSection("databases");
	
	public static final Option<Boolean> usePlayerUUIDsInVariableNames = new Option<>("use player UUIDs in variable names", false); // TODO change to true later (as well as in the default config)
	public static final Option<Boolean> enablePlayerVariableFix = new Option<>("player variable fix", true);
	
	@SuppressWarnings("null")
	private static final DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	private static final Option<DateFormat> dateFormat = new Option<>("date format", shortDateFormat, s -> {
		try {
			if (s.equalsIgnoreCase("default"))
				return null;
			return new SimpleDateFormat(s);
		} catch (final IllegalArgumentException e) {
			Skript.error("'" + s + "' is not a valid date format. Please refer to https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html for instructions on the format.");
		}
		return null;
	});
	
	public static String formatDate(final long timestamp) {
		final DateFormat f = dateFormat.value();
		synchronized (f) {
			return "" + f.format(timestamp);
		}
	}
	
	static final Option<Verbosity> verbosity = new Option<>("verbosity", Verbosity.NORMAL, new EnumParser<>(Verbosity.class, "verbosity"))
			.setter(SkriptLogger::setVerbosity);
	
	public static final Option<EventPriority> defaultEventPriority = new Option<>("plugin priority", EventPriority.NORMAL, s -> {
		try {
			return EventPriority.valueOf(s.toUpperCase(Locale.ENGLISH));
		} catch (final IllegalArgumentException e) {
			Skript.error("The plugin priority has to be one of lowest, low, normal, high, or highest.");
			return null;
		}
	});

	/**
	 * Determines whether `on &lt;event&gt;` will be triggered by cancelled events or not.
	 */
	public static final Option<Boolean> listenCancelledByDefault = new Option<>("listen to cancelled events by default", false)
			.optional(true);

	
	/**
	 * Maximum number of digits to display after the period for floats and doubles
	 */
	public static final Option<Integer> numberAccuracy = new Option<>("number accuracy", 2);
	
	public static final Option<Boolean> caseSensitive = new Option<>("case sensitive", false);
	public static final Option<Boolean> allowFunctionsBeforeDefs = new Option<>("allow function calls before definations", false)
			.optional(true);

	public static final Option<Boolean> disableObjectCannotBeSavedWarnings = new Option<>("disable variable will not be saved warnings", false);
	public static final Option<Boolean> disableMissingAndOrWarnings = new Option<>("disable variable missing and/or warnings", false);
	public static final Option<Boolean> disableVariableStartingWithExpressionWarnings =
		new Option<>("disable starting a variable's name with an expression warnings", false);
	
	@Deprecated
	public static final Option<Boolean> enableScriptCaching = new Option<>("enable script caching", false)
			.optional(true);
	
	public static final Option<Boolean> keepConfigsLoaded = new Option<>("keep configs loaded", false)
			.optional(true);
	
	public static final Option<Boolean> addonSafetyChecks = new Option<>("addon safety checks", false)
			.optional(true);
	
	public static final Option<Boolean> apiSoftExceptions = new Option<>("soft api exceptions", false);

	public static final Option<Boolean> caseInsensitiveVariables = new Option<>("case-insensitive variables", true)
			.setter(t -> Variables.caseInsensitiveVariables = t);

	public static final Option<String> scriptLoaderThreadSize = new Option<>("script loader thread size", "0")
			.setter(s -> {
				int asyncLoaderSize;
				
				if (s.equalsIgnoreCase("processor count")) {
					asyncLoaderSize = Runtime.getRuntime().availableProcessors();
				} else {
					try {
						asyncLoaderSize = Integer.parseInt(s);
					} catch (NumberFormatException e) {
						Skript.error("Invalid option: " + s);
						return;
					}
				}
				
				ScriptLoader.setAsyncLoaderSize(asyncLoaderSize);
			})
			.optional(true);
	
	public static final Option<Boolean> allowUnsafePlatforms = new Option<>("allow unsafe platforms", false)
			.optional(true);
	
	public static final Option<Boolean> loadDefaultAliases = new Option<>("load default aliases", true)
			.optional(true);

	public static final Option<Boolean> executeFunctionsWithMissingParams = new Option<>("execute functions with missing parameters", true)
			.optional(true)
			.setter(t -> Function.executeWithNulls = t);

	public static final Option<Timespan> longParseTimeWarningThreshold = new Option<>("long parse time warning threshold", new Timespan(0));

	/**
	 * This should only be used in special cases
	 */
	@Nullable
	public static Config getConfig() {
		return mainConfig;
	}
	
	// also used for reloading
	static boolean load() {
		try {
			final File oldConfigFile = new File(Skript.getInstance().getDataFolder(), "config.cfg");
			final File configFile = new File(Skript.getInstance().getDataFolder(), "config.sk");
			if (oldConfigFile.exists()) {
				if (!configFile.exists()) {
					oldConfigFile.renameTo(configFile);
					Skript.info("[1.3] Renamed your 'config.cfg' to 'config.sk' to match the new format");
				} else {
					Skript.error("Found both a new and an old config, ignoring the old one");
				}
			}
			if (!configFile.exists()) {
				Skript.error("Config file 'config.sk' does not exist!");
				return false;
			}
			if (!configFile.canRead()) {
				Skript.error("Config file 'config.sk' cannot be read!");
				return false;
			}
			
			Config mc;
			try {
				mc = new Config(configFile, false, false, ":");
			} catch (final IOException e) {
				Skript.error("Could not load the main config: " + e.getLocalizedMessage());
				return false;
			}
			mainConfig = mc;

			String configVersion = mc.get(version.key);
			if (configVersion == null || Skript.getVersion().compareTo(new Version(configVersion)) != 0) {
				try {
					final InputStream in = Skript.getInstance().getResource("config.sk");
					if (in == null) {
						Skript.error("Your config is outdated, but Skript couldn't find the newest config in its jar.");
						return false;
					}
					final Config newConfig = new Config(in, "Skript.jar/config.sk", false, false, ":");
					in.close();
					
					boolean forceUpdate = false;
					
					if (mc.getMainNode().get("database") != null) { // old database layout
						forceUpdate = true;
						try {
							final SectionNode oldDB = (SectionNode) mc.getMainNode().get("database");
							assert oldDB != null;
							final SectionNode newDBs = (SectionNode) newConfig.getMainNode().get(databases.key);
							assert newDBs != null;
							final SectionNode newDB = (SectionNode) newDBs.get("database 1");
							assert newDB != null;
							
							newDB.setValues(oldDB);
							
							// '.db' was dynamically added before
							final String file = newDB.getValue("file");
							assert file != null;
							if (!file.endsWith(".db"))
								newDB.set("file", file + ".db");
							
							final SectionNode def = (SectionNode) newDBs.get("default");
							assert def != null;
							def.set("backup interval", "" + mc.get("variables backup interval"));
						} catch (final Exception e) {
							Skript.error("An error occurred while trying to update the config's database section.");
							Skript.error("You'll have to update the config yourself:");
							Skript.error("Open the new config.sk as well as the created backup, and move the 'database' section from the backup to the start of the 'databases' section");
							Skript.error("of the new config (i.e. the line 'databases:' should be directly above 'database:'), and add a tab in front of every line that you just copied.");
							return false;
						}
					}
					
					if (newConfig.setValues(mc, version.key, databases.key) || forceUpdate) { // new config is different
						final File bu = FileUtils.backup(configFile);
						newConfig.getMainNode().set(version.key, Skript.getVersion().toString());
						if (mc.getMainNode().get(databases.key) != null)
							newConfig.getMainNode().set(databases.key, mc.getMainNode().get(databases.key));
						mc = mainConfig = newConfig;
						mc.save(configFile);
						Skript.info("Your configuration has been updated to the latest version. A backup of your old config file has been created as " + bu.getName());
					} else { // only the version changed
						mc.getMainNode().set(version.key, Skript.getVersion().toString());
						mc.save(configFile);
					}
				} catch (final IOException e) {
					Skript.error("Could not load the new config from the jar file: " + e.getLocalizedMessage());
				}
			}
			
			mc.load(SkriptConfig.class);
			
//			if (!keepConfigsLoaded.value())
//				mainConfig = null;
		} catch (final RuntimeException e) {
			Skript.exception(e, "An error occurred while loading the config");
			return false;
		}
		return true;
	}

}
