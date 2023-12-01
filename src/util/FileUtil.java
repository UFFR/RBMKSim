package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser.ExtensionFilter;
import main.Main;
import main.RBMKBlueprint;
import main.RBMKSimulation;
import uffrlib.misc.Version;

public class FileUtil
{
	public static final ExtensionFilter BLUEPRINT_FILTER = new ExtensionFilter("RBMK Blueprint File", Main.EXT_BPRINT),
										SIMULATION_FILTER = new ExtensionFilter("RBMK Simulation File", Main.EXT_RSIM);
	public static final byte HEADER_SIZE = 15;
	
	public static boolean writeBlueprintToFile(Path path, RBMKBlueprint blueprint)
	{
		if (Files.exists(path))
			return false;
		
		try (final OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.APPEND);
				final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream))
		{
			outputStream.write(Main.HEADER_BPRINT.getBytes());
			objectOutputStream.writeObject(Main.getVersion());
			objectOutputStream.writeObject(blueprint);
			return true;
		} catch (IOException e)
		{
			e.printStackTrace();
			Main.openErrorDialog(e);
			return false;
		}
	}
	
	public static boolean writeSimulationToFile(Path path, RBMKSimulation simulation)
	{
		if (Files.exists(path))
			return false;
		
		try (final OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.APPEND);
				final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream))
		{
			outputStream.write(Main.HEADER_SIM.getBytes());
			objectOutputStream.writeObject(Main.getVersion());
			objectOutputStream.writeObject(simulation);
			return true;
		} catch (IOException e)
		{
			e.printStackTrace();
			Main.openErrorDialog(e);
			return false;
		}
	}
	
	public static void readBlueprintFromFile(Path path) throws NoSuchFileException
	{
		if (!Files.exists(path))
			throw new NoSuchFileException("Supplied path does not exist!");
		
		try (final InputStream inputStream = Files.newInputStream(path);
				final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream))
		{
			final String header = new String(inputStream.readNBytes(HEADER_SIZE));
			
			if (!Main.HEADER_BPRINT.equals(header))
			{
				final Optional<ButtonType> selectedButton =
						Main.openDialog("Warning!", "MIME type of path [" + path + "] does not match blueprint file!",
						"Expected [" + Main.HEADER_BPRINT + "] but got [" + header + "]. This should not be possible and"
								+ "\n may cause unforeseen consequences in runtime behavior."
								+ "\nContinue with reading?",
						AlertType.CONFIRMATION);
				
				if (selectedButton.isPresent() && selectedButton.get().getButtonData() != ButtonData.OK_DONE || !selectedButton.isPresent())
					return;
			}
			
			final Version savedVersion = (Version) objectInputStream.readObject();
			final int comparison = Main.getVersion().compareTo(savedVersion);
			
			if (comparison != 0)
			{
				final String diff = comparison < 0 ? "newer" : "older";
				
				final Optional<ButtonType> selectedButton =
						Main.openDialog("Warning!",
								  "Version of program that produced blueprint file is " + diff + " than"
								+ " running version.",
								  "File was made by program version " + savedVersion + ", "
								+ "current program's version is " + Main.getVersion() + ". Continue?",
								AlertType.CONFIRMATION);
				
				if (selectedButton.isPresent() && selectedButton.get().getButtonData() != ButtonData.OK_DONE || !selectedButton.isPresent())
					return;
			}
			
			// TODO
		} catch (IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
			Main.openErrorDialog(e);
		}
	}
	
}
