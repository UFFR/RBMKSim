package org.uffr.rbmksim.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.uffr.rbmksim.main.Main.CONFIG_PATH;
import static org.uffr.rbmksim.main.Main.USER_PATH;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.hash.PrimitiveSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.main.Main;
import org.uffr.rbmksim.util.MiscUtil;
import org.uffr.uffrlib.hashing.Hashable;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class ProgramConfig implements Config<ProgramConfig>, Hashable, Serializable, Cloneable
{
	@Serial
	private static final long serialVersionUID = 2581161401492824888L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramConfig.class);
	public static final Path PROGRAM_CONFIG_PATH = CONFIG_PATH.resolve("programConfig.json");
	/// Defaults ///
	public static final Locale LOCALE = Locale.getDefault();
	public static final String USERNAME = System.getProperty("user.name");
	public static final byte TICK_DELAY = 50;
	
	/// Instance ///
	public Locale locale = LOCALE;// I18n usage.
	public Path userPath = USER_PATH;// Default path for blueprints and simulations.
	public Path configPath = CONFIG_PATH;// Path where all configurations, including this one, are held.
	public String username = USERNAME;// Name that is used to credit the creator of blueprints and simulations created.
	public int tickDelay = TICK_DELAY;// Delay between ticks in milliseconds while the program runs, to slow down execution.
	
	static
	{
		try
		{
			Files.createDirectories(CONFIG_PATH);
			Files.createDirectories(USER_PATH);
		} catch (IOException e)
		{
			Main.openErrorDialog(e);
			LOGGER.error("Unable to create configuration directories!", e);
		}
	}
	
	public ProgramConfig()
	{
		LOGGER.info("Creating new ProgramConfig instance...");
		if (Files.notExists(PROGRAM_CONFIG_PATH))
		{
			LOGGER.info("Config file doesn't exist yet, creating...");
			try
			{
				LOGGER.trace("Writing defaults to file...");
				Files.writeString(PROGRAM_CONFIG_PATH, asJsonConfig().toPrettyString());
			} catch (IOException e)
			{
				LOGGER.error("Unable to create initial configuration file!", e);
				Main.openErrorDialog(e);
			}
		} else
		{
			LOGGER.info("Reading saved configuration...");
			try
			{
				final byte[] data = Files.readAllBytes(PROGRAM_CONFIG_PATH);

				LOGGER.trace("Parsing JSON...");
				final ObjectMapper mapper = new ObjectMapper();
				fromJson(mapper.readTree(data));
			} catch (IOException e)
			{
				LOGGER.error("Unable to read/parse configuration file!", e);
				Main.openErrorDialog(e);
			}
		}
	}

	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		sink
		.putString(locale.getCountry(), UTF_8)
		.putString(locale.getVariant(), UTF_8)
		.putString(locale.getScript(), UTF_8)
		.putString(locale.getLanguage(), UTF_8)
		.putString(username, UTF_8)
		.putInt(tickDelay);
		locale.getExtensionKeys().forEach(sink::putChar);
		locale.getUnicodeLocaleAttributes().forEach(sink::putUnencodedChars);
		for (Path path : configPath)
			sink.putString(path.toString(), UTF_8);
		for (Path path : userPath)
			sink.putString(path.toString(), UTF_8);
	}

	@Override
	public ProgramConfig copy(ProgramConfig config)
	{
		this.locale = config.locale;
		this.configPath = config.configPath;
		this.userPath = config.userPath;
		this.username = config.username;
		this.tickDelay = config.tickDelay;
		return this;
	}

	@Override
	public void fromJson(JsonNode config)
	{
		LOGGER.debug("Parsing JSON for configuration options");
		locale = Locale.forLanguageTag(config.get("locale").asText(LOCALE.getLanguage()));
		
		userPath = config.has("userPath") ? Path.of(config.get("userPath").asText(USER_PATH.toString())) : USER_PATH;
		username = config.get("username").asText(USERNAME);
		tickDelay = config.get("tickDelay").asInt(TICK_DELAY);
	}

	@Override
	public void fromBasic(String config)
	{
		final Map<String, String> map = MiscUtil.basicKVToMap(config, new HashMap<>(4));
		locale = Locale.forLanguageTag(map.get("locale"));
		configPath = Path.of(map.get("configPath"));
		userPath = Path.of(map.get("userPath"));
		username = map.get("username");
		tickDelay = MiscUtil.parseInt(map.get("tickDelay"), TICK_DELAY, LOGGER::warn);
	}

	@Override
	public void resetToDefault()
	{
		locale = LOCALE;
		configPath = CONFIG_PATH;
		userPath = USER_PATH;
		username = USERNAME;
		tickDelay = TICK_DELAY;
	}

	@Override
	public JsonNode asJsonConfig()
	{
		LOGGER.debug("Writing config to JSON...");
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode rootNode = mapper.createObjectNode();

		rootNode
		.put("locale", locale.getLanguage())
		.put("username", username)
		.put("tickDelay", tickDelay)
		.put("userPath", userPath.toAbsolutePath().toString());
		
		return rootNode;
	}

	@Override
	public String asBasicConfig()
	{

		return "locale=" + locale.getLanguage() + '\n' +
				"configPath=" + configPath.toAbsolutePath() + '\n' +
				"userPath=" + userPath.toAbsolutePath() + '\n' +
				"username=" + username + '\n' +
				"tickDelay=" + tickDelay + '\n';
	}

	@Override
	public ProgramConfig clone()
	{
		LOGGER.debug("Cloning ProgramConfig instance...");
		try
		{
			return (ProgramConfig) super.clone();
		} catch (CloneNotSupportedException e)
		{
			LOGGER.warn("Could not clone instance! Had to use backup, trace:", e);
			Main.openErrorDialog(e);
			return new ProgramConfig().copy(this);
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(configPath, locale, tickDelay, userPath, username);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof ProgramConfig other))
			return false;
        return Objects.equals(configPath, other.configPath) && Objects.equals(locale, other.locale)
				&& tickDelay == other.tickDelay && Objects.equals(userPath, other.userPath)
				&& Objects.equals(username, other.username);
	}

	@Override
	public String toString()
	{
		return "ProgramConfig [locale=" + locale + ", userPath=" + userPath +
				", configPath=" + configPath + ", username=" + username +
				", tickDelay=" + tickDelay + ']';
	}
	
}
