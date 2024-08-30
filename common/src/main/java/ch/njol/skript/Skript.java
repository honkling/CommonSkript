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

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.classes.data.DefaultConverters;
import ch.njol.skript.classes.data.DefaultFunctions;
import ch.njol.skript.classes.data.DefaultOperations;
import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.classes.data.SkriptClasses;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.BukkitLoggerFilter;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.ErrorDescLogHandler;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.update.ReleaseManifest;
import ch.njol.skript.update.ReleaseStatus;
import ch.njol.skript.update.UpdateManifest;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.EmptyStacktraceException;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Closeable;
import ch.njol.util.Kleenean;
import ch.njol.util.NullableChecker;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.EnumerationIterable;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.junit.After;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.experiment.ExperimentRegistry;
import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.structure.StructureInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

// TODO meaningful error if someone uses an %expression with percent signs% outside of text or a variable

/**
 * <b>Skript</b> - A Bukkit plugin to modify how Minecraft behaves without having to write a single line of code (You'll likely be writing some code though if you're reading this
 * =P)
 * <p>
 * Use this class to extend this plugin's functionality by adding more {@link Condition conditions}, {@link Effect effects}, {@link SimpleExpression expressions}, etc.
 * <p>
 * If your plugin.yml contains <tt>'depend: [Skript]'</tt> then your plugin will not start at all if Skript is not present. Add <tt>'softdepend: [Skript]'</tt> to your plugin.yml
 * if you want your plugin to work even if Skript isn't present, but want to make sure that Skript gets loaded before your plugin.
 * <p>
 * If you use 'softdepend' you can test whether Skript is loaded with <tt>'Bukkit.getPluginManager().getPlugin(&quot;Skript&quot;) != null'</tt>
 * <p>
 * Once you made sure that Skript is loaded you can use <code>Skript.getInstance()</code> whenever you need a reference to the plugin, but you likely won't need it since all API
 * methods are static.
 *
 * @author Peter Güttinger
 * @see #registerAddon(JavaPlugin)
 * @see #registerCondition(Class, String...)
 * @see #registerEffect(Class, String...)
 * @see #registerExpression(Class, Class, ExpressionType, String...)
 * @see #registerEvent(String, Class, Class, String...)
 * @see EventValues#registerEventValue(Class, Class, Getter, int)
 * @see Classes#registerClass(ClassInfo)
 * @see Comparators#registerComparator(Class, Class, Comparator)
 * @see Converters#registerConverter(Class, Class, Converter)
 */
public final class Skript extends JavaPlugin implements Listener {

	// ================ PLUGIN ================

	@Nullable
	private static Skript instance = null;

	private static boolean disabled = false;
	private static boolean partDisabled = false;

	public static Skript getInstance() {
		final Skript i = instance;
		if (i == null)
			throw new IllegalStateException();
		return i;
	}

	/**
	 * Current updater instance used by Skript.
	 */
	@Nullable
	private SkriptUpdater updater;

	public Skript() throws IllegalStateException {
		if (instance != null)
			throw new IllegalStateException("Cannot create multiple instances of Skript!");
		instance = this;
	}

	private static Version minecraftVersion = new Version(666), UNKNOWN_VERSION = new Version(666);
	private static ServerPlatform serverPlatform = ServerPlatform.BUKKIT_UNKNOWN; // Start with unknown... onLoad changes this

	@Nullable
	private static Version version = null;
	@Deprecated(forRemoval = true) // TODO this field will be replaced by a proper registry later
	private static @UnknownNullability ExperimentRegistry experimentRegistry;

	public static Version getVersion() {
		final Version v = version;
		if (v == null)
			throw new IllegalStateException();
		return v;
	}

	public static final Message
		m_invalid_reload = new Message("skript.invalid reload"),
		m_finished_loading = new Message("skript.finished loading"),
		m_no_errors = new Message("skript.no errors"),
		m_no_scripts = new Message("skript.no scripts");
	private static final PluralizingArgsMessage m_scripts_loaded = new PluralizingArgsMessage("skript.scripts loaded");

	public static ServerPlatform getServerPlatform() {
		if (classExists("net.glowstone.GlowServer")) {
			return ServerPlatform.BUKKIT_GLOWSTONE; // Glowstone has timings too, so must check for it first
		} else if (classExists("co.aikar.timings.Timings")) {
			return ServerPlatform.BUKKIT_PAPER; // Could be Sponge, but it doesn't work at all at the moment
		} else if (classExists("org.spigotmc.SpigotConfig")) {
			return ServerPlatform.BUKKIT_SPIGOT;
		} else if (classExists("org.bukkit.craftbukkit.CraftServer") || classExists("org.bukkit.craftbukkit.Main")) {
			// At some point, CraftServer got removed or moved
			return ServerPlatform.BUKKIT_CRAFTBUKKIT;
		} else { // Probably some ancient Bukkit implementation
			return ServerPlatform.BUKKIT_UNKNOWN;
		}
	}

	/**
	 * Returns true if the underlying installed Java/JVM is 32-bit, false otherwise.
	 * Note that this depends on a internal system property and these can always be overridden by user using -D JVM options,
	 * more specifically, this method will return false on non OracleJDK/OpenJDK based JVMs, that don't include bit information in java.vm.name system property.
	 * @return Whether the installed Java/JVM is 32-bit or not.
	 */
	private static boolean using32BitJava() {
		// Property returned should either be "Java HotSpot(TM) 32-Bit Server VM" or "OpenJDK 32-Bit Server VM" if 32-bit and using OracleJDK/OpenJDK
		return System.getProperty("java.vm.name").contains("32");
	}

	/**
	 * The folder containing all Scripts.
	 * Never reference this field directly. Use {@link #getScriptsFolder()}.
	 */
	private File scriptsFolder;

	/**
	 * The folder containing all addons.
	 */
	private File addonsFolder;

	/**
	 * @return The manager for experimental, optional features.
	 */
	public static ExperimentRegistry experiments() {
		return experimentRegistry;
	}

	/**
	 * @return The folder containing all Scripts.
	 */
	public File getScriptsFolder() {
		if (!scriptsFolder.isDirectory())
			//noinspection ResultOfMethodCallIgnored
			scriptsFolder.mkdirs();
		return scriptsFolder;
	}

	/**
	 * @return The folder containing all Scripts.
	 */
	public @NotNull File getAddonsFolder() {
		if (!addonsFolder.isDirectory())
			//noinspection ResultOfMethodCallIgnored
			addonsFolder.mkdirs();
		return addonsFolder;
	}

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		if (disabled) {
			Skript.error(m_invalid_reload.toString());
			setEnabled(false);
			return;
		}

		handleJvmArguments(); // JVM arguments

		version = new Version("" + getDescription().getVersion()); // Skript version

		// Start the updater
		// Note: if config prohibits update checks, it will NOT do network connections
		try {
			this.updater = new SkriptUpdater();
		} catch (Exception e) {
			Skript.exception(e, "Update checker could not be initialized.");
		}
		experimentRegistry = new ExperimentRegistry(this);
		Feature.registerAll(getAddonInstance(), experimentRegistry);

		if (!getDataFolder().isDirectory())
			getDataFolder().mkdirs();

		scriptsFolder = new File(getDataFolder(), SCRIPTSFOLDER);
		addonsFolder = new File(getDataFolder(), ADDONSFOLDER);
		File config = new File(getDataFolder(), "config.sk");
		File features = new File(getDataFolder(), "features.sk");
		File lang = new File(getDataFolder(), "lang");
		if (!scriptsFolder.isDirectory() || !config.exists() || !features.exists() || !lang.exists()) {
			ZipFile f = null;
			try {
				boolean populateExamples = false;
				if (!scriptsFolder.isDirectory()) {
					if (!scriptsFolder.mkdirs())
						throw new IOException("Could not create the directory " + scriptsFolder);
					populateExamples = true;
				}

				boolean populateLanguageFiles = false;
				if (!lang.isDirectory()) {
					if (!lang.mkdirs())
						throw new IOException("Could not create the directory " + lang);
					populateLanguageFiles = true;
				}

				f = new ZipFile(getFile());
				for (ZipEntry e : new EnumerationIterable<ZipEntry>(f.entries())) {
					if (e.isDirectory())
						continue;
					File saveTo = null;
					if (populateExamples && e.getName().startsWith(SCRIPTSFOLDER + "/")) {
						String fileName = e.getName().substring(e.getName().indexOf("/") + 1);
						// All example scripts must be disabled for jar security.
						if (!fileName.startsWith(ScriptLoader.DISABLED_SCRIPT_PREFIX))
							fileName = ScriptLoader.DISABLED_SCRIPT_PREFIX + fileName;
						saveTo = new File(scriptsFolder, fileName);
					} else if (populateLanguageFiles
							&& e.getName().startsWith("lang/")
							&& !e.getName().endsWith("default.lang")) {
						String fileName = e.getName().substring(e.getName().lastIndexOf("/") + 1);
						saveTo = new File(lang, fileName);
					} else if (e.getName().equals("config.sk")) {
						if (!config.exists())
							saveTo = config;
//					} else if (e.getName().startsWith("aliases-") && e.getName().endsWith(".sk") && !e.getName().contains("/")) {
//						File af = new File(getDataFolder(), e.getName());
//						if (!af.exists())
//							saveTo = af;
					} else if (e.getName().startsWith("features.sk")) {
						if (!features.exists())
							saveTo = features;
					}
					if (saveTo != null) {
						InputStream in = f.getInputStream(e);
						try {
							assert in != null;
							FileUtils.save(in, saveTo);
						} finally {
							in.close();
						}
					}
				}
				info("Successfully generated the config and the example scripts.");
			} catch (ZipException ignored) {} catch (IOException e) {
				error("Error generating the default files: " + ExceptionUtils.toString(e));
			} finally {
				if (f != null) {
					try {
						f.close();
					} catch (IOException ignored) {}
				}
			}
		}

		// initialize the Skript addon instance
		getAddonInstance();

		// Load classes which are always safe to use
		new JavaClasses(); // These may be needed in configuration

		// And then not-so-safe classes
		Throwable classLoadError = null;
		try {
			new SkriptClasses();
		} catch (Throwable e) {
			classLoadError = e;
		}

		// Config must be loaded after Java and Skript classes are parseable
		// ... but also before platform check, because there is a config option to ignore some errors
		SkriptConfig.load();


		// If loading can continue (platform ok), check for potentially thrown error
		if (classLoadError != null) {
			exception(classLoadError);
			setEnabled(false);
			return;
		}

		new DefaultComparators();
		new DefaultConverters();
		new DefaultFunctions();
		new DefaultOperations();

		try {
			getAddonInstance().loadClasses("ch.njol.skript",
				"conditions", "effects", "events", "expressions", "entity", "sections", "structures");
		} catch (final Exception e) {
			exception(e, "Could not load required .class files: " + e.getLocalizedMessage());
			setEnabled(false);
			return;
		}

		if (logNormal())
			info(" " + Language.get("skript.copyright"));

		PluginManager pluginManager = Bukkit.getPluginManager();

		for (File addonFile : getAddonsFolder().listFiles()) {
			if (!addonFile.isFile() || !addonFile.getName().endsWith(".jar") || addonFile.equals(getFile()))
				continue;

			JavaPlugin plugin = pluginManager.loadPlugin(addonFile);
			plugin.setEnabled(true);
			plugin.onEnable();
		}

		stopAcceptingRegistrations();
		Documentation.generate(); // TODO move to test classes?

		// Variable loading
		if (logNormal())
			info("Loading variables...");
		long vls = System.currentTimeMillis();

		LogHandler h = SkriptLogger.startLogHandler(new ErrorDescLogHandler() {
			@Override
			public LogResult log(final LogEntry entry) {
				super.log(entry);
				if (entry.level.intValue() >= Level.SEVERE.intValue()) {
					logEx(entry.message); // no [Skript] prefix
					return LogResult.DO_NOT_LOG;
				} else {
					return LogResult.LOG;
				}
			}

			@Override
			protected void beforeErrors() {
				logEx();
				logEx("===!!!=== Skript variable load error ===!!!===");
				logEx("Unable to load (all) variables:");
			}

			@Override
			protected void afterErrors() {
				logEx();
				logEx("Skript will work properly, but old variables might not be available at all and new ones may or may not be saved until Skript is able to create a backup of the old file and/or is able to connect to the database (which requires a restart of Skript)!");
				logEx();
			}
		});

		try (CountingLogHandler c = new CountingLogHandler(SkriptLogger.SEVERE).start()) {
			if (!Variables.load())
				if (c.getCount() == 0)
					error("(no information available)");
		} finally {
			h.stop();
		}

		long vld = System.currentTimeMillis() - vls;
		if (logNormal())
			info("Loaded " + Variables.numVariables() + " variables in " + ((vld / 100) / 10.) + " seconds");

		// Skript initialization done
		debug("Early init done");

		/*
		 * Start loading scripts
		 */
		Date start = new Date();
		CountingLogHandler logHandler = new CountingLogHandler(Level.SEVERE);

		File scriptsFolder = getScriptsFolder();
		ScriptLoader.updateDisabledScripts(scriptsFolder.toPath());
		ScriptLoader.loadScripts(scriptsFolder, logHandler)
			.thenAccept(scriptInfo -> {
				try {
					if (logHandler.getCount() == 0)
						Skript.info(m_no_errors.toString());
					if (scriptInfo.files == 0)
						Skript.warning(m_no_scripts.toString());
					if (Skript.logNormal() && scriptInfo.files > 0)
						Skript.info(m_scripts_loaded.toString(
							scriptInfo.files,
							scriptInfo.structures,
							start.difference(new Date())
						));

					Skript.info(m_finished_loading.toString());
				} catch (Exception e) {
					// Something went wrong, we need to make sure the exception is printed
					throw Skript.exception(e);
				}
			});
	}

	/**
	 * Handles -Dskript.stuff command line arguments.
	 */
	private void handleJvmArguments() {
		Path folder = getDataFolder().toPath();

		/*
		 * Burger is a Python application that extracts data from Minecraft.
		 * Datasets for most common versions are available for download.
		 * Skript uses them to provide minecraft:material to Bukkit
		 * Material mappings on Minecraft 1.12 and older.
		 */
		String burgerEnabled = System.getProperty("skript.burger.enable");
		if (burgerEnabled != null) {
			tainted = true;
			String version = System.getProperty("skript.burger.version");
			String burgerInput;
			if (version == null) { // User should have provided JSON file path
				String inputFile = System.getProperty("skript.burger.file");
				if (inputFile == null) {
					Skript.exception("burger enabled but skript.burger.file not provided");
					return;
				}
				try {
					burgerInput = new String(Files.readAllBytes(Paths.get(inputFile)), StandardCharsets.UTF_8);
				} catch (IOException e) {
					Skript.exception(e);
					return;
				}
			} else { // Try to download Burger dataset for this version
				try {
					Path data = folder.resolve("burger-" + version + ".json");
					if (!Files.exists(data)) {
						URL url = new URL("https://pokechu22.github.io/Burger/" + version + ".json");
						try (InputStream is = url.openStream()) {
							Files.copy(is, data);
						}
					}
					burgerInput = new String(Files.readAllBytes(data), StandardCharsets.UTF_8);
				} catch (IOException e) {
					Skript.exception(e);
					return;
				}
			}

		}
	}

	public static Version getMinecraftVersion() {
		return minecraftVersion;
	}

	/**
	 * @return Whether this server is running CraftBukkit
	 */
	public static boolean isRunningCraftBukkit() {
		return serverPlatform == ServerPlatform.BUKKIT_CRAFTBUKKIT;
	}

	/**
	 * Used to test whether certain Bukkit features are supported.
	 *
	 * @param className
	 * @return Whether the given class exists.
	 * @deprecated use {@link #classExists(String)}
	 */
	@Deprecated
	public static boolean supports(final String className) {
		return classExists(className);
	}

	/**
	 * Tests whether a given class exists in the classpath.
	 *
	 * @param className The {@link Class#getCanonicalName() canonical name} of the class
	 * @return Whether the given class exists.
	 */
	public static boolean classExists(final String className) {
		try {
			Class.forName(className);
			return true;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Tests whether a method exists in the given class.
	 *
	 * @param c The class
	 * @param methodName The name of the method
	 * @param parameterTypes The parameter types of the method
	 * @return Whether the given method exists.
	 */
	public static boolean methodExists(final Class<?> c, final String methodName, final Class<?>... parameterTypes) {
		try {
			c.getDeclaredMethod(methodName, parameterTypes);
			return true;
		} catch (final NoSuchMethodException e) {
			return false;
		} catch (final SecurityException e) {
			return false;
		}
	}

	/**
	 * Tests whether a method exists in the given class, and whether the return type matches the expected one.
	 * <p>
	 * Note that this method doesn't work properly if multiple methods with the same name and parameters exist but have different return types.
	 *
	 * @param c The class
	 * @param methodName The name of the method
	 * @param parameterTypes The parameter types of the method
	 * @param returnType The expected return type
	 * @return Whether the given method exists.
	 */
	public static boolean methodExists(final Class<?> c, final String methodName, final Class<?>[] parameterTypes, final Class<?> returnType) {
		try {
			final Method m = c.getDeclaredMethod(methodName, parameterTypes);
			return m.getReturnType() == returnType;
		} catch (final NoSuchMethodException e) {
			return false;
		} catch (final SecurityException e) {
			return false;
		}
	}

	/**
	 * Tests whether a field exists in the given class.
	 *
	 * @param c The class
	 * @param fieldName The name of the field
	 * @return Whether the given field exists.
	 */
	public static boolean fieldExists(final Class<?> c, final String fieldName) {
		try {
			c.getDeclaredField(fieldName);
			return true;
		} catch (final NoSuchFieldException e) {
			return false;
		} catch (final SecurityException e) {
			return false;
		}
	}

	@Nullable
	static Metrics metrics;

	@Nullable
	public static Metrics getMetrics() {
		return metrics;
	}

	@SuppressWarnings("null")
	private final static Collection<Closeable> closeOnDisable = Collections.synchronizedCollection(new ArrayList<Closeable>());

	/**
	 * Registers a Closeable that should be closed when this plugin is disabled.
	 * <p>
	 * All registered Closeables will be closed after all scripts have been stopped.
	 *
	 * @param closeable
	 */
	public static void closeOnDisable(final Closeable closeable) {
		closeOnDisable.add(closeable);
	}

	@Nullable
	private static Method IS_RUNNING;
	@Nullable
	private static Object MC_SERVER;

	@SuppressWarnings("ConstantConditions")
	private boolean isServerRunning() {
		return false;
	}

	private void beforeDisable() {
		partDisabled = true;

		ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
	}

	@Override
	public void onDisable() {
		if (disabled)
			return;
		disabled = true;
		this.experimentRegistry = null;

		if (!partDisabled) {
			beforeDisable();
		}

		for (Closeable c : closeOnDisable) {
			try {
				c.close();
			} catch (final Exception e) {
				Skript.exception(e, "An error occurred while shutting down.", "This might or might not cause any issues.");
			}
		}
	}

	// ================ CONSTANTS, OPTIONS & OTHER ================

	public final static String SCRIPTSFOLDER = "scripts";
	public final static String ADDONSFOLDER = "addons";

	/**
	 * A small value, useful for comparing doubles or floats.
	 * <p>
	 * E.g. to test whether two floating-point numbers are equal:
	 *
	 * <pre>
	 * Math.abs(a - b) &lt; Skript.EPSILON
	 * </pre>
	 *
	 * or whether a location is within a specific radius of another location:
	 *
	 * <pre>
	 * location.distanceSquared(center) - radius * radius &lt; Skript.EPSILON
	 * </pre>
	 *
	 * @see #EPSILON_MULT
	 */
	public final static double EPSILON = 1e-10;
	/**
	 * A value a bit larger than 1
	 *
	 * @see #EPSILON
	 */
	public final static double EPSILON_MULT = 1.00001;

	/**
	 * The maximum ID a block can have in Minecraft.
	 */
	public final static int MAXBLOCKID = 255;
	/**
	 * The maximum data value of Minecraft, i.e. Short.MAX_VALUE - Short.MIN_VALUE.
	 */
	public final static int MAXDATAVALUE = Short.MAX_VALUE - Short.MIN_VALUE;

	// TODO localise Infinity, -Infinity, NaN (and decimal point?)
	public static String toString(final double n) {
		return StringUtils.toString(n, SkriptConfig.numberAccuracy.value());
	}

	public final static UncaughtExceptionHandler UEH = new UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(final @Nullable Thread t, final @Nullable Throwable e) {
			Skript.exception(e, "Exception in thread " + (t == null ? null : t.getName()));
		}
	};

	/**
	 * Creates a new Thread and sets its UncaughtExceptionHandler. The Thread is not started automatically.
	 */
	public static Thread newThread(final Runnable r, final String name) {
		final Thread t = new Thread(r, name);
		t.setUncaughtExceptionHandler(UEH);
		return t;
	}

	// ================ REGISTRATIONS ================

	private static boolean acceptRegistrations = true;

	public static boolean isAcceptRegistrations() {
		if (instance == null)
			throw new IllegalStateException("Skript was never loaded");
		return acceptRegistrations && instance.isEnabled();
	}

	public static void checkAcceptRegistrations() {
		if (!isAcceptRegistrations() && !Skript.testing())
			throw new SkriptAPIException("Registration can only be done during plugin initialization");
	}

	private static void stopAcceptingRegistrations() {
		Converters.createChainedConverters();

		acceptRegistrations = false;

		Classes.onRegistrationsStop();
	}

	// ================ ADDONS ================

	private final static HashMap<String, SkriptAddon> addons = new HashMap<>();

	/**
	 * Registers an addon to Skript. This is currently not required for addons to work, but the returned {@link SkriptAddon} provides useful methods for registering syntax elements
	 * and adding new strings to Skript's localization system (e.g. the required "types.[type]" strings for registered classes).
	 *
	 * @param p The plugin
	 */
	public static SkriptAddon registerAddon(final JavaPlugin p) {
		checkAcceptRegistrations();
		if (addons.containsKey(p.getName()))
			throw new IllegalArgumentException("The plugin " + p.getName() + " is already registered");
		final SkriptAddon addon = new SkriptAddon(p);
		addons.put(p.getName(), addon);
		return addon;
	}

	@Nullable
	public static SkriptAddon getAddon(final JavaPlugin p) {
		return addons.get(p.getName());
	}

	@Nullable
	public static SkriptAddon getAddon(final String name) {
		return addons.get(name);
	}

	@SuppressWarnings("null")
	public static Collection<SkriptAddon> getAddons() {
		return Collections.unmodifiableCollection(addons.values());
	}

	@Nullable
	private static SkriptAddon addon;

	/**
	 * @return A {@link SkriptAddon} representing Skript.
	 */
	public static SkriptAddon getAddonInstance() {
		if (addon == null) {
			addon = new SkriptAddon(Skript.getInstance());
			addon.setLanguageFileDirectory("lang");
		}
		return addon;
	}

	// ================ CONDITIONS & EFFECTS & SECTIONS ================

	private static final Collection<SyntaxElementInfo<? extends Condition>> conditions = new ArrayList<>(50);
	private static final Collection<SyntaxElementInfo<? extends Effect>> effects = new ArrayList<>(50);
	private static final Collection<SyntaxElementInfo<? extends Statement>> statements = new ArrayList<>(100);
	private static final Collection<SyntaxElementInfo<? extends Section>> sections = new ArrayList<>(50);

	/**
	 * registers a {@link Condition}.
	 *
	 * @param condition The condition's class
	 * @param patterns Skript patterns to match this condition
	 */
	public static <E extends Condition> void registerCondition(final Class<E> condition, final String... patterns) throws IllegalArgumentException {
		checkAcceptRegistrations();
		String originClassPath = Thread.currentThread().getStackTrace()[2].getClassName();
		final SyntaxElementInfo<E> info = new SyntaxElementInfo<>(patterns, condition, originClassPath);
		conditions.add(info);
		statements.add(info);
	}

	/**
	 * Registers an {@link Effect}.
	 *
	 * @param effect The effect's class
	 * @param patterns Skript patterns to match this effect
	 */
	public static <E extends Effect> void registerEffect(final Class<E> effect, final String... patterns) throws IllegalArgumentException {
		checkAcceptRegistrations();
		String originClassPath = Thread.currentThread().getStackTrace()[2].getClassName();
		final SyntaxElementInfo<E> info = new SyntaxElementInfo<>(patterns, effect, originClassPath);
		effects.add(info);
		statements.add(info);
	}

	/**
	 * Registers a {@link Section}.
	 *
	 * @param section The section's class
	 * @param patterns Skript patterns to match this section
	 * @see Section
	 */
	public static <E extends Section> void registerSection(Class<E> section, String... patterns) throws IllegalArgumentException {
		checkAcceptRegistrations();
		String originClassPath = Thread.currentThread().getStackTrace()[2].getClassName();
		SyntaxElementInfo<E> info = new SyntaxElementInfo<>(patterns, section, originClassPath);
		sections.add(info);
	}

	public static Collection<SyntaxElementInfo<? extends Statement>> getStatements() {
		return statements;
	}

	public static Collection<SyntaxElementInfo<? extends Condition>> getConditions() {
		return conditions;
	}

	public static Collection<SyntaxElementInfo<? extends Effect>> getEffects() {
		return effects;
	}

	public static Collection<SyntaxElementInfo<? extends Section>> getSections() {
		return sections;
	}

	// ================ EXPRESSIONS ================

	private final static List<ExpressionInfo<?, ?>> expressions = new ArrayList<>(100);

	private final static int[] expressionTypesStartIndices = new int[ExpressionType.values().length];

	/**
	 * Registers an expression.
	 *
	 * @param c The expression's class
	 * @param returnType The superclass of all values returned by the expression
	 * @param type The expression's {@link ExpressionType type}. This is used to determine in which order to try to parse expressions.
	 * @param patterns Skript patterns that match this expression
	 * @throws IllegalArgumentException if returnType is not a normal class
	 */
	public static <E extends Expression<T>, T> void registerExpression(final Class<E> c, final Class<T> returnType, final ExpressionType type, final String... patterns) throws IllegalArgumentException {
		checkAcceptRegistrations();
		if (returnType.isAnnotation() || returnType.isArray() || returnType.isPrimitive())
			throw new IllegalArgumentException("returnType must be a normal type");
		String originClassPath = Thread.currentThread().getStackTrace()[2].getClassName();
		final ExpressionInfo<E, T> info = new ExpressionInfo<>(patterns, returnType, c, originClassPath, type);
		expressions.add(expressionTypesStartIndices[type.ordinal()], info);
		for (int i = type.ordinal(); i < ExpressionType.values().length; i++) {
			expressionTypesStartIndices[i]++;
		}
	}

	@SuppressWarnings("null")
	public static Iterator<ExpressionInfo<?, ?>> getExpressions() {
		return expressions.iterator();
	}

	public static Iterator<ExpressionInfo<?, ?>> getExpressions(final Class<?>... returnTypes) {
		return new CheckedIterator<>(getExpressions(), new NullableChecker<ExpressionInfo<?, ?>>() {
			@Override
			public boolean check(final @Nullable ExpressionInfo<?, ?> i) {
				if (i == null || i.returnType == Object.class)
					return true;
				for (final Class<?> returnType : returnTypes) {
					assert returnType != null;
					if (Converters.converterExists(i.returnType, returnType))
						return true;
				}
				return false;
			}
		});
	}

	// ================ EVENTS ================

	private static final List<SkriptEventInfo<?>> events = new ArrayList<>(50);
	private static final List<StructureInfo<? extends Structure>> structures = new ArrayList<>(10);

	/**
	 * Registers an event.
	 *
	 * @param name Capitalised name of the event without leading "On" which is added automatically (Start the name with an asterisk to prevent this). Used for error messages and
	 *            the documentation.
	 * @param c The event's class
	 * @param event The Bukkit event this event applies to
	 * @param patterns Skript patterns to match this event
	 * @return A SkriptEventInfo representing the registered event. Used to generate Skript's documentation.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends SkriptEvent> SkriptEventInfo<E> registerEvent(String name, Class<E> c, Class<? extends Event> event, String... patterns) {
		return registerEvent(name, c, new Class[] {event}, patterns);
	}

	/**
	 * Registers an event.
	 *
	 * @param name The name of the event, used for error messages
	 * @param c The event's class
	 * @param events The Bukkit events this event applies to
	 * @param patterns Skript patterns to match this event
	 * @return A SkriptEventInfo representing the registered event. Used to generate Skript's documentation.
	 */
	public static <E extends SkriptEvent> SkriptEventInfo<E> registerEvent(String name, Class<E> c, Class<? extends Event>[] events, String... patterns) {
		checkAcceptRegistrations();
		String originClassPath = Thread.currentThread().getStackTrace()[2].getClassName();

		String[] transformedPatterns = new String[patterns.length];
		for (int i = 0; i < patterns.length; i++)
			transformedPatterns[i] = SkriptEvent.fixPattern(patterns[i]);

		SkriptEventInfo<E> r = new SkriptEventInfo<>(name, transformedPatterns, c, originClassPath, events);
		Skript.events.add(r);
		return r;
	}

	public static <E extends Structure> void registerStructure(Class<E> c, String... patterns) {
		checkAcceptRegistrations();
		String originClassPath = Thread.currentThread().getStackTrace()[2].getClassName();
		StructureInfo<E> structureInfo = new StructureInfo<>(patterns, c, originClassPath);
		structures.add(structureInfo);
	}

	public static <E extends Structure> void registerSimpleStructure(Class<E> c, String... patterns) {
		checkAcceptRegistrations();
		String originClassPath = Thread.currentThread().getStackTrace()[2].getClassName();
		StructureInfo<E> structureInfo = new StructureInfo<>(patterns, c, originClassPath, true);
		structures.add(structureInfo);
	}

	public static <E extends Structure> void registerStructure(Class<E> c, EntryValidator entryValidator, String... patterns) {
		checkAcceptRegistrations();
		String originClassPath = Thread.currentThread().getStackTrace()[2].getClassName();
		StructureInfo<E> structureInfo = new StructureInfo<>(patterns, c, originClassPath, entryValidator);
		structures.add(structureInfo);
	}

	public static Collection<SkriptEventInfo<?>> getEvents() {
		return events;
	}

	public static List<StructureInfo<? extends Structure>> getStructures() {
		return structures;
	}

	// ================ COMMANDS ================

	// ================ LOGGING ================

	public static boolean logNormal() {
		return SkriptLogger.log(Verbosity.NORMAL);
	}

	public static boolean logHigh() {
		return SkriptLogger.log(Verbosity.HIGH);
	}

	public static boolean logVeryHigh() {
		return SkriptLogger.log(Verbosity.VERY_HIGH);
	}

	public static boolean debug() {
		return SkriptLogger.debug();
	}

	public static boolean testing() {
		return debug() || Skript.class.desiredAssertionStatus();
	}

	public static boolean log(final Verbosity minVerb) {
		return SkriptLogger.log(minVerb);
	}

	public static void debug(final String info) {
		if (!debug())
			return;
		SkriptLogger.log(SkriptLogger.DEBUG, info);
	}

	/**
	 * @see SkriptLogger#log(Level, String)
	 */
	@SuppressWarnings("null")
	public static void info(final String info) {
		SkriptLogger.log(Level.INFO, info);
	}

	/**
	 * @see SkriptLogger#log(Level, String)
	 */
	@SuppressWarnings("null")
	public static void warning(final String warning) {
		SkriptLogger.log(Level.WARNING, warning);
	}

	/**
	 * @see SkriptLogger#log(Level, String)
	 */
	@SuppressWarnings("null")
	public static void error(final @Nullable String error) {
		if (error != null)
			SkriptLogger.log(Level.SEVERE, error);
	}

	/**
	 * Use this in {@link Expression#init(Expression[], int, Kleenean, ch.njol.skript.lang.SkriptParser.ParseResult)} (and other methods that are called during the parsing) to log
	 * errors with a specific {@link ErrorQuality}.
	 *
	 * @param error
	 * @param quality
	 */
	public static void error(final String error, final ErrorQuality quality) {
		SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, quality, error));
	}

	private final static String EXCEPTION_PREFIX = "#!#! ";

	/**
	 * Used if something happens that shouldn't happen
	 *
	 * @param info Description of the error and additional information
	 * @return an EmptyStacktraceException to throw if code execution should terminate.
	 */
	public static EmptyStacktraceException exception(final String... info) {
		return exception(null, info);
	}

	public static EmptyStacktraceException exception(final @Nullable Throwable cause, final String... info) {
		return exception(cause, null, null, info);
	}

	public static EmptyStacktraceException exception(final @Nullable Throwable cause, final @Nullable Thread thread, final String... info) {
		return exception(cause, thread, null, info);
	}

	public static EmptyStacktraceException exception(final @Nullable Throwable cause, final @Nullable TriggerItem item, final String... info) {
		return exception(cause, null, item, info);
	}

	/**
	 * Maps Java packages of plugins to descriptions of said plugins.
	 * This is only done for plugins that depend or soft-depend on Skript.
	 */
	private static Map<String, PluginDescriptionFile> pluginPackages = new HashMap<>();
	private static boolean checkedPlugins = false;

	/**
	 * Set by Skript when doing something that users shouldn't do.
	 */
	private static boolean tainted = false;

	/**
	 * Set to true when an exception is thrown.
	 */
	private static boolean errored = false;

	/**
	 * Mark that an exception has occurred at some point during runtime.
	 * Only used for Skript's testing system.
	 */
	public static void markErrored() {
		errored = true;
	}

	/**
	 * Used if something happens that shouldn't happen
	 *
	 * @param cause exception that shouldn't occur
	 * @param info Description of the error and additional information
	 * @return an EmptyStacktraceException to throw if code execution should terminate.
	 */
	public static EmptyStacktraceException exception(@Nullable Throwable cause, final @Nullable Thread thread, final @Nullable TriggerItem item, final String... info) {
		errored = true;

		// Don't send full exception message again, when caught exception (likely) comes from this method
		if (cause instanceof EmptyStacktraceException) {
			return new EmptyStacktraceException();
		}

		// First error: gather plugin package information
		if (!checkedPlugins) {
			for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
				if (plugin.getName().equals("Skript")) // Don't track myself!
					continue;

				PluginDescriptionFile desc = plugin.getDescription();
				if (desc.getDepend().contains("Skript") || desc.getSoftDepend().contains("Skript")) {
					// Take actual main class out from the qualified name
					String[] parts = desc.getMain().split("\\."); // . is special in regexes...
					StringBuilder name = new StringBuilder(desc.getMain().length());
					for (int i = 0; i < parts.length - 1; i++) {
						name.append(parts[i]).append('.');
					}

					// Put this to map
					pluginPackages.put(name.toString(), desc);
					if (Skript.debug())
						Skript.info("Identified potential addon: " + desc.getFullName() + " (" + name.toString() + ")");
				}
			}

			checkedPlugins = true; // No need to do this next time
		}

		String issuesUrl = "https://github.com/SkriptLang/Skript/issues";

		logEx();
		logEx("[Skript] Severe Error:");
		logEx(info);
		logEx();

		// Parse something useful out of the stack trace
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		Set<PluginDescriptionFile> stackPlugins = new HashSet<>();
		for (StackTraceElement s : stackTrace) { // Look through stack trace
			for (Entry<String,PluginDescriptionFile> e : pluginPackages.entrySet()) { // Look through plugins
				if (s.getClassName().contains(e.getKey())) // Hey, is this plugin in that stack trace?
					stackPlugins.add(e.getValue()); // Yes? Add it to list
			}
		}

		SkriptUpdater updater = Skript.getInstance().getUpdater();

		// Check if server platform is supported
		if (tainted) {
			logEx("Skript is running with developer command-line options.");
			logEx("If you are not a developer, consider disabling them.");
		} else if (getInstance().getDescription().getVersion().contains("nightly")) {
			logEx("You're running a (buggy) nightly version of Skript.");
			logEx("If this is not a test server, switch to a more stable release NOW!");
			logEx("Your players are unlikely to appreciate crashes and/or data loss due to Skript bugs.");
			logEx("");
			logEx("Just testing things? Good. Please report this bug, so that we can fix it before a stable release.");
			logEx("Issue tracker: " + issuesUrl);
		} else if (!serverPlatform.supported){
			logEx("Your server platform appears to be unsupported by Skript. It might not work reliably.");
			logEx("You can report this at " + issuesUrl + ". However, we may be unable to fix the issue.");
			logEx("It is recommended that you switch to Paper or Spigot, should you encounter more problems.");
		} else if (updater != null && updater.getReleaseStatus() == ReleaseStatus.OUTDATED) {
			logEx("You're running outdated version of Skript! Please try updating it NOW; it might fix this.");
			logEx("Run /sk update check to get a download link to latest Skript!");
			logEx("You will be given instructions how to report this error if it persists after update.");
		} else {
			logEx("Something went horribly wrong with Skript.");
			logEx("This issue is NOT your fault! You probably can't fix it yourself, either.");
			if (pluginPackages.isEmpty()) {
				logEx("You should report it at " + issuesUrl + ". Please copy paste this report there (or use paste service).");
				logEx("This ensures that your issue is noticed and will be fixed as soon as possible.");
			} else {
				logEx("It looks like you are using some plugin(s) that alter how Skript works (addons).");
				if (stackPlugins.isEmpty()) {
					logEx("Here is full list of them:");
					StringBuilder pluginsMessage = new StringBuilder();
					for (PluginDescriptionFile desc : pluginPackages.values()) {
						pluginsMessage.append(desc.getFullName());
						String website = desc.getWebsite();
						if (website != null && !website.isEmpty()) // Add website if found
							pluginsMessage.append(" (").append(desc.getWebsite()).append(")");

						pluginsMessage.append(" ");
					}
					logEx(pluginsMessage.toString());
					logEx("We could not identify which of those are specially related, so this might also be Skript issue.");
				} else {
					logEx("Following plugins are probably related to this error in some way:");
					StringBuilder pluginsMessage = new StringBuilder();
					for (PluginDescriptionFile desc : stackPlugins) {
						pluginsMessage.append(desc.getName());
						String website = desc.getWebsite();
						if (website != null && !website.isEmpty()) // Add website if found
							pluginsMessage.append(" (").append(desc.getWebsite()).append(")");

						pluginsMessage.append(" ");
					}
					logEx(pluginsMessage.toString());
				}

				logEx("You should try disabling those plugins one by one, trying to find which one causes it.");
				logEx("If the error doesn't disappear even after disabling all listed plugins, it is probably Skript issue.");
				logEx("In that case, you will be given instruction on how should you report it.");
				logEx("On the other hand, if the error disappears when disabling some plugin, report it to author of that plugin.");
				logEx("Only if the author tells you to do so, report it to Skript's issue tracker.");
			}
		}

		logEx();
		logEx("Stack trace:");
		if (cause == null || cause.getStackTrace().length == 0) {
			logEx("  warning: no/empty exception given, dumping current stack trace instead");
			cause = new Exception(cause);
		}
		boolean first = true;
		while (cause != null) {
			logEx((first ? "" : "Caused by: ") + cause.toString());
			for (final StackTraceElement e : cause.getStackTrace())
				logEx("    at " + e.toString());
			cause = cause.getCause();
			first = false;
		}

		logEx();
		logEx("Version Information:");
		if (updater != null) {
			ReleaseStatus status = updater.getReleaseStatus();
			logEx("  Skript: " + getVersion() + (status == ReleaseStatus.LATEST ? " (latest)"
					: status == ReleaseStatus.OUTDATED ? " (OUTDATED)"
					: status == ReleaseStatus.CUSTOM ? " (custom version)" : ""));
			ReleaseManifest current = updater.getCurrentRelease();
			logEx("    Flavor: " + current.flavor);
			logEx("    Date: " + current.date);
		} else {
			logEx("  Skript: " + getVersion() + " (unknown; likely custom)");
		}
		logEx("  Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + ")");
		logEx("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"));
		logEx();
		logEx("Server platform: " + serverPlatform.name + (serverPlatform.supported ? "" : " (unsupported)"));
		logEx();
		logEx("Current node: " + SkriptLogger.getNode());
		logEx("Current item: " + (item == null ? "null" : item.toString(null, true)));
		if (item != null && item.getTrigger() != null) {
			Trigger trigger = item.getTrigger();
			Script script = trigger.getScript();
			logEx("Current trigger: " + trigger.toString(null, true) + " (" + (script == null ? "null" : script.getConfig().getFileName()) + ", line " + trigger.getLineNumber() + ")");
		}
		logEx();
		logEx("Thread: " + (thread == null ? Thread.currentThread() : thread).getName());
		logEx();
		logEx("Language: " + Language.getName());
		logEx();
		logEx("End of Error.");
		logEx();

		return new EmptyStacktraceException();
	}

	static void logEx() {
		SkriptLogger.LOGGER.severe(EXCEPTION_PREFIX);
	}

	static void logEx(final String... lines) {
		for (final String line : lines)
			SkriptLogger.LOGGER.severe(EXCEPTION_PREFIX + line);
	}

	private static final Message SKRIPT_PREFIX_MESSAGE = new Message("skript.prefix");

	public static String getSkriptPrefix() {
		return SKRIPT_PREFIX_MESSAGE.getValueOrDefault("<grey>[<gold>Skript<grey>] <reset>");
	}

	/**
	 * Gets the updater instance currently used by Skript.
	 * @return SkriptUpdater instance.
	 */
	@Nullable
	public SkriptUpdater getUpdater() {
		return updater;
	}

}
