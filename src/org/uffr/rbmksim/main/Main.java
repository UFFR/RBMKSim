package org.uffr.rbmksim.main;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.config.ProgramConfig;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.util.I18n;
import org.uffr.uffrlib.misc.Version;
import org.uffr.uffrlib.misc.Version.VersionSuffix;

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
	// Useful for tracking discrepancies between saved files and the currently running program.
	private static final Version VERSION = new Version(0, 5, 0, VersionSuffix.SNAPSHOT);
	// Basic strings reused in various places.
	public static final String EXT_BPRINT = "rbmk",
							   EXT_RSIM = "rsim",
							   KEY;
	// Icon of the program.
	public static final Image ICON_IMAGE = new Image(Main.class.getClassLoader().getResourceAsStream("resources/rad.png"));
	
	// The current frame.
//	protected static Optional<RBMKFrame> frame = Optional.empty();
	protected static FrameRunner runner = new FrameRunner();
	public static ProgramConfig config = new ProgramConfig();
	// JavaFX stage
	private static Stage stage;
	// If the simulation is running, paused otherwise.
//	private static boolean running = false;
	
	static
	{
		// TODO Probably deprecate this
		final StringBuilder builder = new StringBuilder(150);
		for (ColumnType type : ColumnType.values())
			builder.append(type.symbol).append(": ").append(type.fullName).append(" Column\n");
		KEY = builder.toString();
	}
	
	public static void main(String[] args)
	{
		LOGGER.debug("Entry point begun, launching...");
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		LOGGER.info("Starting application...");
		stage = primaryStage;
		final Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources/main_window.fxml"));
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
		runner.setActive(running);
		// TODO Some kind of trigger to initiate the simulation again.
	}
	
	public static boolean isRunning()
	{
		return runner.isActive();
	}
	
	public static Optional<RBMKFrame> getFrame()
	{
		return runner.getFrame();
	}
	
	public static void setFrame(RBMKFrame frame)
	{
		runner.setFrame(frame);
	}
	
	public static void closeFrame()
	{
		runner.close();
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
	
	public static void openErrorDialog(Exception e)
	{
		LOGGER.debug("Main.openErrorDialog() triggered");
		openDialog(I18n.resolve("dialog.error.title"), I18n.resolve("dialog.error.header"), e.toString(), AlertType.ERROR);
	}
	
	public static String getAboutString()
	{
		LOGGER.debug("Main.getAboutString() triggered");
		final StringBuilder builder = new StringBuilder(100);
		builder
		.append(I18n.resolve("about.author")).append('\n')
		.append(I18n.resolve("about.version", VERSION)).append('\n')
		.append(I18n.resolve("about.company")).append('\n')
		.append(I18n.resolve("about.license"));
		return builder.toString();
	}
	
	public static String getCreditsString()
	{
		// TODO Finish credits
		LOGGER.debug("Main.getCreditsString() triggered");
		final StringBuilder builder = new StringBuilder(200);
		builder
		.append('\u2022').append(I18n.resolve("credits.java"));
		return builder.toString();
	}
}
