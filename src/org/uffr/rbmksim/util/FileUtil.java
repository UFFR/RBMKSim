package org.uffr.rbmksim.util;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.main.Main;
import org.uffr.rbmksim.main.RBMKBlueprint;
import org.uffr.rbmksim.main.RBMKSimulation;
import org.uffr.uffrlib.misc.StringUtil;
import org.uffr.uffrlib.misc.Version;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser.ExtensionFilter;

public class FileUtil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
	public static final ExtensionFilter BLUEPRINT_FILTER	= new ExtensionFilter("RBMK Blueprint File", Main.EXT_BPRINT),
										SIMULATION_FILTER	= new ExtensionFilter("RBMK Simulation File", Main.EXT_RSIM);
//	private static final byte[] BLUEPRINT_MAGIC_B	= {0x52, 0x42, 0x4D, 0x4B, 0x53, 0x69, 0x6D, 0x42},// RBMKSimB
//								SIMULATION_MAGIC_B	= {0x52, 0x42, 0x4D, 0x4B, 0x53, 0x69, 0x6D, 0x53};// RBMKSimS
	public static final long BLUEPRINT_MAGIC	= 0x52424D4B53696D42L,
							 SIMULATION_MAGIC	= 0x52424D4B53696D53L;
	public static final byte HEADER_SIZE = 15;
	
	public static boolean writeBlueprintToFile(Path path, RBMKBlueprint blueprint)
	{
		if (Files.exists(path))
			return false;

		LOGGER.debug("Exporting as blueprint to " + path);
		try (final OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.APPEND);
				final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream))
		{
//			outputStream.write(Main.HEADER_BPRINT.getBytes());
			LOGGER.trace("Writing magic number...");
			objectOutputStream.writeLong(BLUEPRINT_MAGIC);
			LOGGER.trace("Writing version...");
			objectOutputStream.writeObject(Main.getVersion());
			LOGGER.trace("Writing main data...");
			objectOutputStream.writeObject(blueprint);
			LOGGER.debug("Done exporting!");
			return true;
		} catch (IOException e)
		{
			LOGGER.warn("Unable to export blueprint!", e);
			Main.openErrorDialog(e);
			return false;
		}
	}
	
	public static boolean writeSimulationToFile(Path path, RBMKSimulation simulation)
	{
		if (Files.exists(path))
			return false;
		
		LOGGER.debug("Exporting as simulation to: " + path);
		try (final OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.APPEND);
				final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream))
		{
//			outputStream.write(Main.HEADER_SIM.getBytes());
			LOGGER.trace("Writing magic number...");
			objectOutputStream.writeLong(SIMULATION_MAGIC);
			LOGGER.trace("Writing version...");
			objectOutputStream.writeObject(Main.getVersion());
			LOGGER.trace("Writing main data...");
			objectOutputStream.writeObject(simulation);
			LOGGER.debug("Done exporting!");
			return true;
		} catch (IOException e)
		{
			LOGGER.warn("Unable to export simulation!", e);
			Main.openErrorDialog(e);
			return false;
		}
	}
	
	public static void readBlueprintFromFile(Path path) throws NoSuchFileException
	{
		if (!Files.exists(path))
			throw new NoSuchFileException("Supplied path does not exist!");
		
		LOGGER.debug("Importing as blueprint...");
		try (final InputStream inputStream = Files.newInputStream(path);
				final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream))
		{
//			final String header = new String(inputStream.readNBytes(HEADER_SIZE));
			LOGGER.trace("Reading magic number...");
			final long magic = objectInputStream.readLong();
			
			if (magic != BLUEPRINT_MAGIC)
			{
				LOGGER.info("Read wrong magic number from file");
				final Optional<ButtonType> selectedButton =
						Main.openDialog("Warning!", "MIME type of path " + path + " does not match blueprint file!",
						"Expected 0x" + StringUtil.longToHex(BLUEPRINT_MAGIC) + " but got 0x" + StringUtil.longToHex(magic) + ". This should not be possible and"
								+ "\n may cause unforeseen consequences in runtime behavior."
								+ "\nContinue with reading?",
						AlertType.CONFIRMATION);
				
				if (selectedButton.isPresent() && selectedButton.get().getButtonData() != ButtonData.OK_DONE || !selectedButton.isPresent())
					return;
			}
			
			LOGGER.trace("Reading saved version and comparing...");
			final Version savedVersion = (Version) objectInputStream.readObject();
			final int comparison = Main.getVersion().compareTo(savedVersion);
			
			if (comparison != 0)
			{
				LOGGER.info("Read different version in file than what is running");
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
			LOGGER.warn("Unable to read blueprint!", e);
			Main.openErrorDialog(e);
		}
	}
	
}
