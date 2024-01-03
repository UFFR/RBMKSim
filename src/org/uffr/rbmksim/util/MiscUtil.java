package org.uffr.rbmksim.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.main.Main;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class MiscUtil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MiscUtil.class);

	/**
	 * Convenience method to convert list of lines with '=' delimited key-value pairs into a {@link Map}.
	 * @param config The string to read from.
	 * @param map The map to store.
	 * @return A map with all the keys and values added, skipping invalid lines. Same as the parameter.
	 */
	public static Map<String, String> basicKVToMap(String config, Map<String, String> map)
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
		LOGGER.trace("Extracting resource [{}]...", innerPath);
		try (final InputStream inputStream = Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream(innerPath), "Tried to read internal resource, not found!"))
		{
			final Path externalPath = Files.createTempFile("rbmksim-", suffix);
			Files.copy(inputStream, externalPath, StandardCopyOption.REPLACE_EXISTING);
			return externalPath;
		}
	}

	// TODO Move to library, possibly useful
	@CheckForNull
	public static <T, U> U convertOrNull(@Nullable T obj , Function<T, U> converter)
	{
		return obj == null ? null : converter.apply(obj);
	}
	
}
