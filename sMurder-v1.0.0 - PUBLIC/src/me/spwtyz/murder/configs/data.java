package me.spwtyz.murder.configs;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class data {
	private FileConfiguration config;
	private File file;

	public data(File paramFile) {
		this.file = paramFile;
		this.config = YamlConfiguration.loadConfiguration(paramFile);
	}

	public FileConfiguration getConfig() {
		return this.config;
	}

	public void reload() {
		this.config = YamlConfiguration.loadConfiguration(this.file);
	}

	public void save() {
		try {
			this.config.save(this.file);
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		}
	}
}
