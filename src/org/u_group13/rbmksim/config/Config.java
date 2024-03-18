package org.u_group13.rbmksim.config;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * @author Basic interface for configurations.
 *
 * @param <T> The configuration class, for {@link #copy(T)}.
 */
// TODO Rework to use library
@Deprecated
public interface Config<T>
{
	/**
	 * Copy all fields from {@code config} into self.
	 * @param config The configuration to copy.
	 * @return Itself, for convenience
	 */
	T copy(T config);
	/**
	 * Load fields from saved JSON.
	 * @param config The JSON to read from. Generated from {@link #asJsonConfig()}.
	 */
	void fromJson(JsonNode config);
	/**
	 * Loads fields from a basic '=' delimited key-value list.
	 * @param config The string to read from. Generated from {@link #asBasicConfig()}.
	 */
	void fromBasic(String config);
	/**
	 * Reset all fields to their default values.
	 */
	void resetToDefault();
	/**
	 * Save current state as JSON. May be used in {@link #fromJson(JsonNode)}.
	 * @return The JSON representing the current state.
	 */
	JsonNode asJsonConfig();
	/**
	 * Save current state as string. May be used in {@link #fromBasic(String)}.
	 * @return The string representing the current state.
	 */
	String asBasicConfig();
}
