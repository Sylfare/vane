package org.oddlama.vane.core.lang;

import java.lang.reflect.Field;
import java.util.function.Function;

import org.bukkit.configuration.file.YamlConfiguration;

import org.oddlama.vane.annotation.lang.LangMessage;
import org.oddlama.vane.core.YamlLoadException;
import org.oddlama.vane.util.Message;

public class LangMessageField extends LangField<Message> {
	public LangMessage annotation;

	public LangMessageField(Object owner, Field field, Function<String, String> map_name, LangMessage annotation) {
		super(owner, field, map_name);
		this.annotation = annotation;
	}

	@Override
	public void check_loadable(YamlConfiguration yaml) throws YamlLoadException {
		check_yaml_path(yaml);

		if (!yaml.isString(yaml_path())) {
			throw new YamlLoadException("Invalid type for yaml path '" + yaml_path() + "', expected string");
		}
	}

	@Override
	public String str(final YamlConfiguration yaml) {
		return yaml.getString(yaml_path());
	}

	@Override
	public void load(final String namespace, final YamlConfiguration yaml) {
		try {
			field.set(owner, new Message(str(yaml)));
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Invalid field access on '" + field.getName() + "'. This is a bug.");
		}
	}
}

