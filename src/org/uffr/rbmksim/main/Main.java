package org.uffr.rbmksim.main;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.config.ProgramConfig;
import org.uffr.rbmksim.util.I18n;
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

public class Main extends Application
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	// Useful for tracking discrepancies between saved files and the currently running program. Doesn't track metadata due to how I calculate it though.
	private static final Version VERSION = new Version(0, 5, 4, "SNAPSHOT");
	// Basic strings reused in various places.
	public static final String EXT_BPRINT = "*.rbmk",
							   EXT_RSIM   = "*.rsim";
	public static final boolean LINUX = System.getProperty("os.name").contains("Linux");
	public static final Path
							USER_PATH = Path.of(System.getProperty("user.home")),// TODO Change if it causes the program to explode
							CONFIG_PATH = LINUX ? Path.of(System.getProperty("user.home"), ".config", "rbmksim") : Path.of("%APPDATA%", "rbmksim");// TODO Ditto
	// Icon of the program.
	public static final Image ICON_IMAGE = new Image(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("resources/rad.png")));
	
	protected static final FrameRunner RUNNER = new FrameRunner();
	public static ProgramConfig config = new ProgramConfig();
	// JavaFX stage
	private static Stage stage;

	public static void main(String[] args)
	{
		LOGGER.debug("Entry point begun, launching...");
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception
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
	
	public static void openErrorDialog(Throwable e)
	{
		LOGGER.debug("Main.openErrorDialog() triggered");
		openDialog(I18n.resolve("dialog.error.title"), I18n.resolve("dialog.error.header"), e.toString(), AlertType.ERROR);
	}
	
	public static String getAboutString()
	{
		LOGGER.debug("Main.getAboutString() triggered");
		return I18n.resolve("about.author") + '\n' +
				I18n.resolve("about.version" , VERSION) + '\n' +
				I18n.resolve("about.company") + '\n' +
				I18n.resolve("about.license");
	}
	
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
