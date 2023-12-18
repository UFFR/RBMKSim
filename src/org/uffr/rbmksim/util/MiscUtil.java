package org.uffr.rbmksim.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import org.uffr.rbmksim.simulation.fuels.RBMKFuelData;

public class MiscUtil
{
	@Deprecated
	public static StringExtractor<RBMKFuelData> fuelDataExtractor(RBMKFuelData data)
	{
		return new StringExtractor<RBMKFuelData>(data, RBMKFuelData::name);
	}
	
	@Deprecated
	public static List<StringExtractor<RBMKFuelData>> wrapDataList(List<RBMKFuelData> fuelDatas)
	{
		final List<StringExtractor<RBMKFuelData>> extractors = new ArrayList<StringExtractor<RBMKFuelData>>(fuelDatas.size());
		for (RBMKFuelData data : fuelDatas)
			extractors.add(fuelDataExtractor(data));
		return ImmutableList.copyOf(extractors);
	}
	
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
				value = line.substring(delimIndex + 1, line.length());
				
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
	
}
