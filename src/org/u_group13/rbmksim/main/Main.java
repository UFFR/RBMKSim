package org.u_group13.rbmksim.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.config.ProgramConfig;
import org.u_group13.rbmksim.util.I18n;
import org.u_group13.rbmksim.util.MiscUtil;
import org.u_group13.rbmksim.util.VersionProvider;
import org.uffr.uffrlib.misc.Version;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import picocli.CommandLine;

@CommandLine.Command(name = "RBMKSim", description = "Simulator for NTM RBMKs", versionProvider = VersionProvider.class, mixinStandardHelpOptions = true)
public class Main extends Application implements Callable<Integer>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	// Useful for tracking discrepancies between saved files and the currently running program. Doesn't track metadata due to how I calculate it though.
	private static final Version VERSION = new Version(MiscUtil.VERSION_STRING);
	public static final boolean LINUX = System.getProperty("os.name").contains("Linux");
	public static final Path
							USER_PATH = Path.of(System.getProperty("user.home")),// TODO Change if it causes the program to explode
							CONFIG_PATH = LINUX ? Path.of(System.getProperty("user.home"), ".config", "rbmksim") : Path.of("%APPDATA%", "rbmksim");// TODO Ditto
	// Icon of the program.
	public static final Image ICON_IMAGE = new Image(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("resources/rad.png")));
	
	protected static final FrameRunner RUNNER = new FrameRunner();
	public static ProgramConfig config = null;
	// JavaFX stage
	private static Stage stage;

	public static Main instance;

	@CommandLine.Option(names = {"-u", "--user-path"}, description = "Override user path from the default.")
	public Path overrideUserPath = USER_PATH;
	@CommandLine.Option(names = {"-p", "--config-path"}, description = "Override configuration path from the default.")
	public Path overrideConfigPath = CONFIG_PATH;

	public Main()
	{
	}

	@Override
	public Integer call() throws Exception
	{
		final ObjectMapper mapper = new ObjectMapper();
		Files.createDirectories(overrideConfigPath);
		if (Files.notExists(ProgramConfig.PROGRAM_CONFIG_PATH))
		{
			LOGGER.debug("Program config file doesn't exist yet, creating...");

			try
			{
				LOGGER.trace("Writing defaults to file...");
				Files.writeString(ProgramConfig.PROGRAM_CONFIG_PATH, mapper.writeValueAsString(new ProgramConfig()));
			} catch (IOException e)
			{
				LOGGER.error("Unable to create initial configuration file!", e);
				openErrorDialog(e);
			}
		} else
		{

			LOGGER.info("Reading saved configuration...");
			try (final InputStream stream = Files.newInputStream(ProgramConfig.PROGRAM_CONFIG_PATH))
			{
				config = mapper.readValue(stream, ProgramConfig.class);
			} catch (IOException e)
			{
				LOGGER.error("Unable to read/parse configuration file!", e);
				openErrorDialog(e);
			}
		}
		LOGGER.info("Done reading configuration");
		return 0;
	}

	public static void main(String[] args)
	{
		LOGGER.info("Entry point begun, launching...");
		instance = new Main();
		new CommandLine(instance).execute(args);
		LOGGER.info("Launching JavaFX...");
		launch(args);
	}

	@Override
	public void start(@NotNull Stage primaryStage) throws Exception
	{
		LOGGER.info("Starting application...");
		Thread.setDefaultUncaughtExceptionHandler((t, e) ->
		{
			LOGGER.error("Uncaught exception in program in {}", t);
			LOGGER.error("Stack trace:", e);
			openErrorDialog(e);
		});
		stage = primaryStage;
		final Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("resources/main_window.fxml")));
		primaryStage.setTitle(I18n.resolve("app.title"));
        primaryStage.setScene(new Scene(root, 720, 480));
        primaryStage.getIcons().add(ICON_IMAGE);
        primaryStage.show();
        LOGGER.info("Startup complete");
	}

	public void trySaveConfig()
	{
		final ObjectMapper mapper = new ObjectMapper();
		try
		{
			Files.createDirectories(overrideConfigPath);
			LOGGER.debug("Trying to save configuration file...");

			try (final OutputStream stream = Files.newOutputStream(ProgramConfig.PROGRAM_CONFIG_PATH))
			{
				mapper.writeValue(stream, config);
			}

		} catch (IOException e)
		{
			LOGGER.error("Could not save configuration!", e);
			openErrorDialog(e);
		}
	}
	
	public static Stage getStage()
	{
		return stage;
	}
	
	public static void setRunning(boolean running)
	{
//		Main.running = running;
		RUNNER.setActive(running);
		// TODO Some kind of trigger to initiate the simulation again.
	}

	public static Path getUserPath()
	{
		return instance.overrideUserPath == null ? USER_PATH : instance.overrideUserPath;
	}

	public static void toggleRunning()
	{
		setRunning(!RUNNER.isActive());
	}
	
	public static boolean isRunning()
	{
		return RUNNER.isActive();
	}
	
	public static Optional<RBMKFrame> getFrame()
	{
		return RUNNER.getFrame();
	}
	
	public static void setFrame(@Nullable RBMKFrame frame)
	{
		RUNNER.setFrame(frame);
	}
	
	public static void closeFrame()
	{
		RUNNER.close();
		setFrame(null);
	}
	
	public static Version getVersion()
	{
		return VERSION;
	}
	
	public static Optional<ButtonType> openDialogAndWait(String title, String headerMessage, String contentMessage, AlertType type)
	{
		LOGGER.debug("Main.openDialogAndWait() triggered");
		final Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(headerMessage);
		alert.setContentText(contentMessage);
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(ICON_IMAGE);
		return alert.showAndWait();
	}
	
	public static void openDialog(String title, String headerMessage, String contentMessage, AlertType type)
	{
		LOGGER.debug("Main.openDialog() triggered");
		final Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(headerMessage);
		alert.setContentText(contentMessage);
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(ICON_IMAGE);
		alert.show();
	}
	
	public static void openErrorDialog(@NotNull Throwable e)
	{
		LOGGER.debug("Main.openErrorDialog() triggered");
		openDialog(I18n.resolve("dialog.error.title"), I18n.resolve("dialog.error.header"), e.toString(), AlertType.ERROR);
	}
	
	public static @NotNull String getAboutString()
	{
		LOGGER.debug("Main.getAboutString() triggered");
		return I18n.resolve("about.author") + '\n' +
				I18n.resolve("about.version" , VERSION) + '\n' +
				I18n.resolve("about.company") + '\n' +
				I18n.resolve("about.license");
	}

	@NotNull
	public static String getCreditsString()
	{
		// TODO Finish credits
		LOGGER.debug("Main.getCreditsString() triggered");
		return '•' + I18n.resolve("credits.javafx") + '\n' +
				'•' + I18n.resolve("credits.ntm") + '\n' +
				'•' + I18n.resolve("credits.slf4j") + '\n' +
				'•' + I18n.resolve("credits.logback") + '\n' +
				'•' + I18n.resolve("credits.guava") + '\n' +
				'•' + I18n.resolve("credits.java");
	}
}
