package main;

import java.util.Optional;

import config.ProgramConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import simulation.ColumnType;
import uffrlib.misc.Version;

public class Main extends Application
{
	// Useful for tracking discrepancies between saved files and the currently running program.
	private static final Version VERSION = new Version(0, 4, 0, 'a');
	// Basic strings reused in various places.
	public static final String
					EXT_BPRINT = "rbmk",
					EXT_RSIM = "rsim",
					HEADER_BPRINT = "RBMK-BLUEPRINT_",
					HEADER_SIM =    "RBMK-SIMULATION",
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
		final StringBuilder builder = new StringBuilder(50);
		for (ColumnType type : ColumnType.values())
			builder.append(type.symbol).append(": ").append(type.fullName).append(" Column\n");
		KEY = builder.toString();
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		stage = primaryStage;
		final Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources/main_window.fxml"));
        primaryStage.setTitle("NTM RBMK Blueprint Designer & Simulator");
        primaryStage.setScene(new Scene(root, 720, 480));
        primaryStage.getIcons().add(ICON_IMAGE);
        primaryStage.show();
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
		return openDialog("Error!", "Caught exception while running!", e.toString(), AlertType.ERROR);
	}
	// TODO I18n
	public static String getAboutString()
	{
		final StringBuilder builder = new StringBuilder(100);
		builder
		.append("Author: UFFR_87\n")
		.append("Version: ").append(VERSION).append('\n')
		.append("Company: Unnamed Group 13\n")
		.append("Copyleft: CC BY-SA 4.0 2023\n")
		.append("License: GLP 3.0");
		return builder.toString();
	}
}
