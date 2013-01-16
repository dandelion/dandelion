package com.github.dandelion.thymeleaf.dialect;

import java.util.HashSet;
import java.util.Set;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;

/**
 * Dandelion dialect, in charge of loading assets in the HTML page.
 * 
 * @author Thibault Duchateau
 */
public class DandelionDialect extends AbstractDialect {

	public static final String DIALECT_PREFIX = "ddl";
	public static final String LAYOUT_NAMESPACE = "http://github.com/dandelion";
	public static final int DT_HIGHEST_PRECEDENCE = 3500;

	public String getPrefix() {
		return DIALECT_PREFIX;
	}

	public boolean isLenient() {
		return false;
	}

	/*
	 * The processors.
	 */
	@Override
	public Set<IProcessor> getProcessors() {
		final Set<IProcessor> processors = new HashSet<IProcessor>();

		// ajout des processeurs ici
		// processors.add(new TableInitializerElProcessor(new
		// ElementNameWithoutPrefixProcessorMatcher("table", DIALECT_PREFIX +
		// ":table", "true")));

		return processors;
	}
}