package org.oddlama.vane.core.lang;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import java.text.MessageFormat;

// Convenience wrapper for parameter auto-packing
public class TranslatedString extends TranslatableComponent {
	private String key;
	private String default_translation;

	public TranslatedString(final TranslatedString copy_of) {
		super(copy_of);
		this.key = copy_of.key;
		this.default_translation = copy_of.default_translation;
	}

	public TranslatedString(final String key, final String default_translation) {
		super(key);
		this.key = key;
		this.default_translation = default_translation;
	}

	public String key() { return key; }
	public String str() { return default_translation; }

	public TranslatedString clone() {
		return new TranslatedString(this);
	}
}
