package org.oddlama.vane.core;

import static org.oddlama.vane.util.ResourceList.get_resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import org.oddlama.vane.annotation.config.ConfigBoolean;
import org.oddlama.vane.annotation.config.ConfigString;
import org.oddlama.vane.core.config.ConfigManager;
import org.oddlama.vane.core.lang.LangManager;

public abstract class Module extends JavaPlugin {
	private Core core;
	public Logger log;

	public ConfigManager config_manager = new ConfigManager(this);
	public LangManager lang_manager = new LangManager(this);

	@ConfigString(def = "inherit", desc = "The language for this module. The corresponding language file must be named lang-{lang}.yml. Specifying 'inherit' will load the value set for vane-core.")
	public String config_lang;

	@ConfigBoolean(def = true, desc = "The module will only add functionality if this is set to true.")
	public boolean config_enabled = false;

	protected void on_load() {}
	protected abstract void on_enable();
	protected abstract void on_disable();
	protected abstract void on_config_change();

	@Override
	public final void onLoad() {
		// Create data directory
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		on_load();
	}

	@Override
	public final void onEnable() {
		// Get core plugin reference, important for inherited configuration
		if (this.getName().equals("vane-core")) {
			core = (Core)this;
		} else {
			core = (Core)getServer().getPluginManager().getPlugin("vane-core");
		}

		// Compile config and lang variables
		config_manager.compile(this);
		lang_manager.compile(this);

		log = getLogger();
		if (!reload_configuration()) {
			// Force stop server, we encountered an invalid config file version
			log.severe("Invalid plugin configuration. Shutting down.");
			getServer().shutdown();
		}
	}

	public boolean reload_configuration() {
		boolean was_enabled = config_enabled;

		// Generate new file if not existing
		final var file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			final var builder = new StringBuilder();
			config_manager.generate_yaml(builder);
			final var contents = builder.toString();

			// Save contents to file
			try {
				Files.write(file.toPath(), contents.getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Reload automatic variables
		if (!config_manager.reload(file)) {
			return false;
		}

		// Reload localization
		if (!reload_localization()) {
			return false;
		}

		// Disable plugin if needed
		if (was_enabled && !config_enabled) {
			on_disable();
		} else if (!was_enabled && config_enabled) {
			on_enable();
		}

		on_config_change();
		return true;
	}

	private void update_lang_file(String lang_file) {
		final var file = new File(getDataFolder(), lang_file);
		final var file_version = YamlConfiguration.loadConfiguration(file)
		                             .getLong("version", -1);
		long resource_version = -1;

		final var res = getResource(lang_file);
		try (final var reader = new InputStreamReader(res)) {
			resource_version = YamlConfiguration.loadConfiguration(reader)
			                       .getLong("version", -1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (resource_version > file_version) {
			try {
				Files.copy(getResource(lang_file), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean reload_localization() {
		// Copy all embedded lang files, if their version is newer.
		get_resources(getClass(), Pattern.compile("lang-.*\\.yml")).stream().forEach(this::update_lang_file);

		// Get configured language code
		var lang_code = config_lang;
		if ("inherit".equals(lang_code)) {
			lang_code = core.config_lang;

			// Fallback to en in case 'inherit' is used in vane-core.
			if ("inherit".equals(lang_code)) {
				lang_code = "en";
			}
		}

		// Generate new file if not existing
		final var file = new File(getDataFolder(), "lang-" + lang_code + ".yml");
		if (!file.exists()) {
			log.severe("Missing language file '" + file.getName() + "'");
			return false;
		}

		// Reload automatic variables
		if (!lang_manager.reload(file)) {
			return false;
		}

		return true;
	}

	public void register_listener(Listener listener) {
		getServer().getPluginManager().registerEvents(listener, this);
	}

	public void unregister_listener(Listener listener) {
		HandlerList.unregisterAll(listener);
	}

	public void schedule_task(Runnable task, long delay_ticks) {
		getServer().getScheduler().runTaskLater(this, task, delay_ticks);
	}

	public void schedule_next_tick(Runnable task) {
		getServer().getScheduler().runTask(this, task);
	}
}