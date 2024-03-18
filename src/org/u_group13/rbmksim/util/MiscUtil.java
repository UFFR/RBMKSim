package org.u_group13.rbmksim.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.main.Main;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Manifest;

public class MiscUtil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MiscUtil.class);
	public static final String VERSION_STRING;

	static
	{
		final String fallback = "0.6.0-SNAPSHOT";
		String v;
		try
		{
			LOGGER.info("Trying to get version from manifest...");
			final URL resource = Main.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
			assert resource != null;
			final Manifest manifest = new Manifest(resource.openStream());

			v = manifest.getMainAttributes().getValue("Build-Version");
			if (v == null)
				v = fallback;
		} catch (IOException e)
		{
			LOGGER.error("Death and hatred to mankind");
			v = fallback;
		}
		VERSION_STRING = v;
	}

	/**
	 * Convenience method to convert list of lines with '=' delimited key-value pairs into a {@link Map}.
	 * @param config The string to read from.
	 * @param map The map to store.
	 * @return A map with all the keys and values added, skipping invalid lines. Same as the parameter.
	 */
	public static Map<String, String> basicKVToMap(@NotNull String config, Map<String, String> map)
	{
		config.lines().forEach(line ->
		{
			final String key, value;
			final int delimIndex = line.indexOf('=');
			if (delimIndex > 1 || delimIndex < line.length() - 1)
			{
				key = line.substring(0, delimIndex);
				value = line.substring(delimIndex + 1);
				
				map.put(key, value);
			}
		});
		return map;
	}
	
	public static int parseInt(String string, int fallback, Consumer<String> logger)
	{
		return parseInt(string, 10, fallback, logger);
	}

	public static int parseInt(String string, int radix, int fallback, Consumer<String> logger)
	{
		if (string == null || string.isEmpty())
		{
			logger.accept("Input null / empty!");
			logger.accept("Using fallback value...");
			return fallback;
		}
		
		try
		{
			return Integer.parseInt(string, radix);
		} catch (NumberFormatException e)
		{
			logger.accept(e.toString());
			logger.accept("Using fallback value...");
			return fallback;
		}
	}
	
	public static boolean parseBoolean(String string, boolean fallback, Consumer<String> logger)
	{
		if (string == null || string.isEmpty())
		{
			logger.accept("Input null / empty!");
			logger.accept("Using fallback value...");
			return fallback;
		}
		
		try
		{
			return Boolean.parseBoolean(string);
		} catch (Exception e)
		{
			logger.accept(e.toString());
			logger.accept("Using fallback value...");
			return fallback;
		}
	}
	
	public static double parseDouble(String string, double fallback, Consumer<String> logger)
	{
		if (string == null || string.isEmpty())
		{
			logger.accept("Input null / empty!");
			logger.accept("Using fallback value...");
			return fallback;
		}
		
		try
		{
			return Double.parseDouble(string);
		} catch (NumberFormatException e)
		{
			logger.accept(e.toString());
			logger.accept("Using fallback value...");
			return fallback;
		}
	}
	
	public static Path extractResource(String innerPath, String suffix) throws IOException
	{
		LOGGER.trace("Extracting resource [{}] using the suffix \"{}\"...", innerPath, suffix);
		try (final InputStream inputStream = Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream(innerPath), "Tried to read internal resource, not found!"))
		{
			LOGGER.trace("Creating temporary file to copy to...");
			final Path externalPath = Files.createTempFile("rbmksim-", suffix);
			LOGGER.trace("Got {} from call", externalPath);
			Files.copy(inputStream, externalPath, StandardCopyOption.REPLACE_EXISTING);
			return externalPath;
		}
	}

	// TODO Move to library, possibly useful
	@CheckForNull
	public static <T, U> U convertOrNull(@Nullable T obj , Function<T, U> converter)
	{
		LOGGER.trace("Attempting to convert {} with function {}", obj, converter);
		return obj == null ? null : converter.apply(obj);
	}

	public static double clampDouble(double val, double min, double max)
	{
		LOGGER.trace("Clamped double value {} to be between {} and {}", val, min, max);
		return val > max ? max : Math.max(val, min);
	}
}
