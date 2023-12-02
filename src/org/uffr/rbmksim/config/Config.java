package org.uffr.rbmksim.config;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * @author Basic interface for configurations.
 *
 * @param <T> The configuration class, for {@link #copy(T)}.
 */
public interface Config<T>
{
	/**
	 * Copy all fields from {@code config} into self.
	 * @param config The configuration to copy.
	 * @return Itself, for convenience
	 */
	public T copy(T config);
	/**
	 * Load fields from saved JSON.
	 * @param config The JSON to read from. Generated from {@link #asJsonConfig()}.
	 */
	public void fromJson(JsonNode config);
	/**
	 * Loads fields from a basic '=' delimited key-value list.
	 * @param config The string to read from. Generated from {@link #asBasicConfig()}.
	 */
	public void fromBasic(String config);
	/**
	 * Reset all fields to their default values.
	 */
	public void resetToDefault();
	/**
	 * Save current state as JSON. May be used in {@link #fromJson(JsonNode)}.
	 * @return The JSON representing the current state.
	 */
	public JsonNode asJsonConfig();
	/**
	 * Save current state as string. May be used in {@link #fromBasic(String)}.
	 * @return The string representing the current state.
	 */
	public String asBasicConfig();
}
