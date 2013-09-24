package com.github.dandelion.core.config;

import com.github.dandelion.core.DandelionError;

/**
 * All errors related to the configuration loading.
 * 
 * @since 0.0.3
 */
public enum ConfigurationError implements DandelionError {

	DEFAULT_CONFIGURATION_LOADING(0), 
	LOCALE_RESOLVER_CLASS_NOT_FOUND(1), 
	LOCALE_RESOLVER_CLASS_INSTANCIATION(2);

	private final int number;

	private ConfigurationError(int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}
}
