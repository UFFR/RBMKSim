package org.uffr.rbmksim.main;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.config.ProgramConfig;
import org.uffr.rbmksim.simulation.ColumnType;
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
	// Useful for tracking discrepancies between saved files and the currently running program.
	private static final Version VERSION = new Version(0, 5, 0, 'a');
	// Basic strings reused in various places.
	public static final String EXT_BPRINT = "rbmk",
							   EXT_RSIM = "rsim",
							   KEY;
	// Icon of the program.
	public static final Image ICON_IMAGE = new Image(Main.class.getClassLoader().getResourceAsStream("resources/rad.png"));
	
	// The current frame.
	protected static Optional<RBMKFrame> frame = Optional.empty();
	public static ProgramConfig config = new ProgramConfig();
	// JavaFX stage
	private static Stage stage;
	// If the simulation is running, paused otherwise.
	private static boolean running = false;
	
	static
	{
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
		LOGGER.debug("Starting application...");
		stage = primaryStage;
		final Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources/main_window.fxml"));
//        primaryStage.setTitle("NTM RBMK Blueprint Designer & Simulator");
		primaryStage.setTitle(I18n.resolve("app.title"));
        primaryStage.setScene(new Scene(root, 720, 480));
        primaryStage.getIcons().add(ICON_IMAGE);
        primaryStage.show();
        LOGGER.debug("Startup complete");
	}
	
	public static Stage getStage()
	{
		return stage;
	}
	
	public static void setRunning(boolean running)
	{
		Main.running = running;
		// TODO Some kind of trigger to initiate the simulation again.
	}
	
	public static boolean isRunning()
	{
		return running;
	}
	
	public static boolean simulationPresent()
	{
		return frame.isPresent();
	}
	
	public static RBMKFrame getSimulation()
	{
		return frame.get();
	}
	
	public static Version getVersion()
	{
		return VERSION.clone();
	}
	
	public static Optional<ButtonType> openDialog(String title, String headerMessage, String contentMessage, AlertType type)
	{
		final Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(headerMessage);
		alert.setContentText(contentMessage);
		((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(ICON_IMAGE);
		return alert.showAndWait();
	}
	
	public static Optional<ButtonType> openErrorDialog(Exception e)
	{
		return openDialog(I18n.resolve("dialog.error.title"), I18n.resolve("dialog.error.header"), e.toString(), AlertType.ERROR);
	}
	
	// TODO I18n
	public static String getAboutString()
	{
		final StringBuilder builder = new StringBuilder(100);
		builder
//		.append("Author: UFFR_87\n")
		.append(I18n.resolve("about.author")).append('\n')
//		.append("Version: ").append(VERSION).append('\n')
		.append(I18n.resolve("about.version", VERSION)).append('\n')
//		.append("Company: Unnamed Group 13\n")
		.append(I18n.resolve("about.company")).append('\n')
//		.append("License: GPL v3.0");
		.append(I18n.resolve("about.license"));
		return builder.toString();
	}
}
