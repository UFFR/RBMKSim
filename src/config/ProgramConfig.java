package config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uffrlib.misc.NumberUtil.parseInt;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.hash.PrimitiveSink;

import main.Main;
import util.Hashable;
import util.MiscUtil;

public class ProgramConfig implements Config<ProgramConfig>, Hashable, Serializable, Cloneable
{
	private static final long serialVersionUID = 2581161401492824888L;
	/// Defaults ///
	public static final Locale LOCALE = Locale.getDefault();
	public static final Path
					USER_PATH = Path.of(System.getProperty("user.home")),// TODO Change if it causes the program to explode
					CONFIG_PATH = System.getProperty("os.name").contains("Linux") ? Path.of(System.getProperty("user.home"), ".config", "rbmksim") : Path.of("%APPDATA%", "rbmksim");
	public static final String USERNAME = System.getProperty("user.name");
	public static final byte TICK_DELAY = 0;
	
	/// Instance ///
	public Locale locale = LOCALE;// I18n usage.
	public Path
			userPath = USER_PATH,// Default path for blueprints and simulations.
			configPath = CONFIG_PATH;// Path where all configurations, including this one, are held.
	public String username = USERNAME;// Name that is used to credit the creator of blueprints and simulations created.
	public int tickDelay = TICK_DELAY;// Delay between MC ticks while the program runs, to slow down execution.
	
	static
	{
		try
		{
			Files.createDirectories(CONFIG_PATH);
			Files.createDirectories(USER_PATH);
		} catch (IOException e)
		{
			Main.openErrorDialog(e);
		}
	}
	
	public ProgramConfig()
	{}

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
		locale = Locale.forLanguageTag(config.get("locale").asText(LOCALE.getLanguage()));
		
		final JsonNode cPathNode = config.get("configPath");
		final String[] cPaths = new String[cPathNode.size()];
		for (int i = 0; i < cPaths.length; i++)
			cPaths[i] = cPathNode.get(i).asText();
		
		final JsonNode uPathNode = config.get("userPath");
		final String[] uPaths = new String[uPathNode.size()];
		for (int i = 0; i < uPaths.length; i++)
			uPaths[i] = uPathNode.get(i).asText();
		
		configPath = Path.of("", cPaths);
		userPath = Path.of("", uPaths);
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
		tickDelay = parseInt(map.get("tickDelay"), TICK_DELAY);
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
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode rootNode = mapper.createObjectNode();
		
		rootNode
		.put("locale", locale.getLanguage())
		.put("username", username)
		.put("tickDelay", tickDelay);
		
		final Path cPath = configPath.toAbsolutePath(), uPath = userPath.toAbsolutePath();
		
		final ArrayNode cPathNode = rootNode.arrayNode(cPath.getNameCount());
		final ArrayNode uPathNode = rootNode.arrayNode(uPath.getNameCount());
		
		for (Path p : cPath)
			cPathNode.add(p.toString());
		for (Path p : uPath)
			uPathNode.add(p.toString());
		
		return rootNode;
	}

	@Override
	public String asBasicConfig()
	{
		final StringBuilder builder = new StringBuilder(100);
		
		builder
		.append("locale=").append(locale.getLanguage()).append('\n')
		.append("configPath=").append(configPath.toAbsolutePath()).append('\n')
		.append("userPath=").append(userPath.toAbsolutePath()).append('\n')
		.append("username=").append(username).append('\n')
		.append("tickDelay=").append(tickDelay).append('\n');
		
		return builder.toString();
	}

	@Override
	public ProgramConfig clone()
	{
		try
		{
			return (ProgramConfig) super.clone();
		} catch (CloneNotSupportedException e)
		{
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
		if (!(obj instanceof ProgramConfig))
			return false;
		final ProgramConfig other = (ProgramConfig) obj;
		return Objects.equals(configPath, other.configPath) && Objects.equals(locale, other.locale)
				&& tickDelay == other.tickDelay && Objects.equals(userPath, other.userPath)
				&& Objects.equals(username, other.username);
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("ProgramConfig [locale=").append(locale).append(", userPath=").append(userPath)
				.append(", configPath=").append(configPath).append(", username=").append(username)
				.append(", tickDelay=").append(tickDelay).append(']');
		return builder.toString();
	}
	
}
