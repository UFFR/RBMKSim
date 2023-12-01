package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import simulation.fuels.RBMKFuelData;

public class MiscUtil
{
	public static StringExtractor<RBMKFuelData> fuelDataExtractor(RBMKFuelData data)
	{
		return new StringExtractor<RBMKFuelData>(data, RBMKFuelData::name);
	}
	
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
}
