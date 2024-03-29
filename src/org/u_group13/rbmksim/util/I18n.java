package org.u_group13.rbmksim.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.main.Main;

public class I18n
{
	private static final Logger LOGGER = LoggerFactory.getLogger(I18n.class);
	public static final List<Locale> LOCALES = ImmutableList.copyOf(Locale.getAvailableLocales());
	private static final int LOCALE_COUNT = Locale.getAvailableLocales().length, ENTRIES = 200;// TODO Actually count
	private static final Map<Locale, Map<String, String>> LOCALE_MAP = new HashMap<>(LOCALE_COUNT);
	private static Locale currentLocale;
	private static Map<String, String> currentLangMap;
	
	static
	{
		LOGGER.info("Auto-initialization of class I18n has begun");
		for (Locale locale : LOCALES)
		{
			try (final InputStream stream = getLocaleResource(locale))
			{
				if (stream == null)
				{
					LOGGER.trace("Initialization stream null, locale {} probably just has no localization file", locale);
					continue;
				}

				LOCALE_MAP.put(locale, new HashMap<>(ENTRIES));
				initializeLocale(locale, stream);
			} catch (IOException e)
			{
				LOGGER.warn("Couldn't read locale file!", e);
				Main.openErrorDialog(e);
			}
		}
		currentLocale = Locale.getDefault();
		currentLangMap = LOCALE_MAP.get(currentLocale);
		LOGGER.info("I18n initialization complete");
	}
	
	public static Locale getCurrentLocale()
	{
		return currentLocale;
	}
	
	public static void setCurrentLocale(Locale currentLocale)
	{
		LOGGER.info("Locale (re)set to: " + currentLocale);
		I18n.currentLocale = currentLocale;
		currentLangMap = LOCALE_MAP.get(currentLocale);
	}
	
	private static InputStream getLocaleResource(Locale locale)
	{
		return Main.class.getClassLoader().getResourceAsStream("resources/lang/" + locale + ".lang");
	}
	
	private static void initializeLocale(Locale locale, InputStream stream)
	{
		LOGGER.trace("Initializing " + locale);
		try
		{
			final String fullString = new String(stream.readAllBytes());
			final Map<String, String> map = LOCALE_MAP.get(locale);
			
			MiscUtil.basicKVToMap(fullString, map);
		} catch (IOException e)
		{
			LOGGER.error("Unable to initialize locale [{}], this should not be possible!", locale);
			LOGGER.error("Got error:", e);
			Main.openErrorDialog(e);
		}
	}
	
	public static boolean hasLocale(Locale locale)
	{
		LOGGER.trace("Queried if registry has locale");
		return LOCALE_MAP.containsKey(locale);
	}
	
	public static boolean hasKey(String key)
	{
		LOGGER.trace("Queried if map has key");
		return currentLangMap.containsKey(key);
	}
	
	public static String resolve(String key)
	{
		LOGGER.trace("Resolving [{}] without format", key);
		return currentLangMap.getOrDefault(key, LOCALE_MAP.get(Locale.US).getOrDefault(key, key));
	}
	
	public static String resolve(String key, Object... format)
	{
		LOGGER.trace("Resolving [{}] with format {}", key, format);
		return String.format(currentLocale, currentLangMap.getOrDefault(key, LOCALE_MAP.get(Locale.ENGLISH ).getOrDefault(key, key)), format);
	}

}
