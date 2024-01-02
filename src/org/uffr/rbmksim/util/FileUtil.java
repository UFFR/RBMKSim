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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.checkerframework.checker.units.qual.N;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.main.Main;
import org.uffr.rbmksim.main.RBMKBlueprint;
import org.uffr.rbmksim.main.RBMKFrame;
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
										SIMULATION_FILTER	= new ExtensionFilter("RBMK Simulation File", Main.EXT_RSIM),
										GENERIC_FILTER		= new ExtensionFilter("RBMKSim Save Files", Main.EXT_BPRINT, Main.EXT_RSIM);
	public static final long BLUEPRINT_MAGIC	= 0x52424D4B53696D42L,
							 SIMULATION_MAGIC	= 0x52424D4B53696D53L;
	public static final String BLUEPRINT_MAGIC_STRING = "0x" + StringUtil.longToHex(BLUEPRINT_MAGIC),
								SIMULATION_MAGIC_STRING = "0x" + StringUtil.longToHex(SIMULATION_MAGIC);
	public static final short JAVA_SERIAL_MAGIC = (short) 0xACED;
	public static final byte HEADER_SIZE = 15;

	@Deprecated
	public static void writeFrameToFile(@Nonnull Path path, @Nonnull RBMKFrame frame)
	{
		switch (frame)
		{
			case RBMKBlueprint b -> writeBlueprintToFile(path, b);
			case RBMKSimulation s -> writeSimulationToFile(path, s);
			default -> throw new IllegalArgumentException("Unexpected value: " + frame);
		}
	}

	@Deprecated
	public static boolean writeBlueprintToFile(@Nonnull Path path, @Nonnull RBMKBlueprint blueprint)
	{
		if (Files.exists(path))
			return false;

		LOGGER.info("Exporting as blueprint to " + path);
		try (final OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
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

	@Deprecated
	public static boolean writeSimulationToFile(@Nonnull Path path, @Nonnull RBMKSimulation simulation)
	{
		if (Files.exists(path))
			return false;
		
		LOGGER.debug("Exporting as simulation to: [{}]", path);
		try (final OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
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

	@Deprecated
	@CheckForNull
	public static RBMKBlueprint readBlueprintFromFile(@Nonnull Path path) throws NoSuchFileException
	{
		if (!Files.exists(path))
			throw new NoSuchFileException("Supplied path does not exist!");
		
		LOGGER.info("Importing as blueprint...");
		try (final InputStream inputStream = Files.newInputStream(path);
				final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream))
		{
//			final String header = new String(inputStream.readNBytes(HEADER_SIZE));
			LOGGER.trace("Reading magic number...");
			final long magic = objectInputStream.readLong();
			
			if (magic != BLUEPRINT_MAGIC)
			{
				LOGGER.warn("Read wrong magic number from file, got magic number {} instead of {}", StringUtil.longToHex(magic), StringUtil.longToHex(BLUEPRINT_MAGIC));
				final Optional<ButtonType> selectedButton =
						Main.openDialogAndWait("Warning!", "Header of file " + path + " does not match blueprint file!",
						"Expected 0x" + StringUtil.longToHex(BLUEPRINT_MAGIC) + " but got 0x" + StringUtil.longToHex(magic) + ". This should not be possible and"
								+ "\n may cause unforeseen consequences in runtime behavior."
								+ "\nContinue with reading?",
						AlertType.CONFIRMATION);
				
				if (selectedButton.isEmpty() || selectedButton.get().getButtonData() != ButtonData.OK_DONE)
					return null;
			}
			
			LOGGER.trace("Reading saved version and comparing...");
			final Version savedVersion = (Version) objectInputStream.readObject();
			final int comparison = Main.getVersion().compareTo(savedVersion);
			
			if (comparison != 0)
			{
				LOGGER.info("Read different version in file than what is running, querying user...");
				final String diff = comparison < 0 ? "newer" : "older";
				
				final Optional<ButtonType> selectedButton =
						Main.openDialogAndWait("Warning!",
								  "Version of program that produced blueprint file is " + diff + " than"
								+ " running version.",
								  "File was made by program version " + savedVersion + ", "
								+ "current program's version is v" + Main.getVersion() + ". Continue?",
								AlertType.CONFIRMATION);
				
				if (selectedButton.isEmpty() || selectedButton.get().getButtonData() != ButtonData.OK_DONE)
					return null;

				LOGGER.info("Continuing with deserialization...");
			}
			
			return (RBMKBlueprint) objectInputStream.readObject();

		} catch (IOException | ClassNotFoundException e)
		{
			LOGGER.warn("Unable to read blueprint!", e);
			Main.openErrorDialog(e);
			return null;
		}
	}

	public static void writeSaveFile(@Nonnull Path path, @Nonnull RBMKFrame frame)
	{
		LOGGER.debug("Exporting to: [{}]", path);
		try (final OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
			 final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream))
		{
			LOGGER.trace("Writing magic number...");
			objectOutputStream.writeLong(switch (frame)
			{
				case RBMKBlueprint ignored -> BLUEPRINT_MAGIC;
				case RBMKSimulation ignored -> SIMULATION_MAGIC;
				default -> throw new IllegalStateException("Got unknown type: " + frame.getClass());
			});
			LOGGER.trace("Writing version...");
			objectOutputStream.writeObject(Main.getVersion());
			LOGGER.trace("Writing main data...");
			objectOutputStream.writeObject(frame);
			LOGGER.debug("Done exporting!");
		} catch (IOException e)
		{
			LOGGER.warn("Unable to export save file!", e);
			Main.openErrorDialog(e);
		}
	}

	@CheckForNull
	public static RBMKFrame readSaveFile(@Nonnull Path path) throws NoSuchFileException
	{
		if (!Files.exists(path))
			throw new NoSuchFileException("Supplied path does not exist!");

		LOGGER.info("Reading saved file...");
		try (final InputStream inputStream = Files.newInputStream(path);
				final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream))
		{
			LOGGER.trace("Reading magic number...");
			final long magic = objectInputStream.readLong();

			if (magic != BLUEPRINT_MAGIC && magic != SIMULATION_MAGIC)
			{
				final String hexMagic = StringUtil.longToHex(magic);
				LOGGER.warn("Read wrong magic number from file, got magic number {} instead of {} or {}", hexMagic, BLUEPRINT_MAGIC_STRING, SIMULATION_MAGIC_STRING);
				final Optional<ButtonType> selectedButton =
						Main.openDialogAndWait(I18n.resolve("dialog.warning.title"), I18n.resolve("dialog.warning.badMagic.header", path),
						I18n.resolve("dialog.warning.badMagic.body", BLUEPRINT_MAGIC_STRING, SIMULATION_MAGIC_STRING, hexMagic),
						AlertType.CONFIRMATION);

				if (selectedButton.isEmpty() || selectedButton.get().getButtonData() != ButtonData.OK_DONE)
					return null;
			}

			LOGGER.trace("Reading saved version and comparing...");
			final Version savedVersion = (Version) objectInputStream.readObject();
			final int comparison = Main.getVersion().compareTo(savedVersion);
			LOGGER.trace("Got version v{}", savedVersion);

			if (comparison != 0)
			{
				LOGGER.info("Read different version in file than what is running, querying user...");
				final String diff = comparison < 0 ? "newer" : "older";

				final Optional<ButtonType> selectedButton =
						Main.openDialogAndWait(I18n.resolve("dialog.warning.title"),
								  I18n.resolve("dialog.warning.badVersion.header", diff),
								  I18n.resolve("dialog.warning.badVersion.body", savedVersion, Main.getVersion()),
								AlertType.CONFIRMATION);

				if (selectedButton.isEmpty() || selectedButton.get().getButtonData() != ButtonData.OK_DONE)
					return null;

				LOGGER.info("Continuing with deserialization...");
			}

			return (RBMKFrame) objectInputStream.readObject();

		} catch (IOException | ClassNotFoundException e)
		{
			LOGGER.warn("Unable to read save file!", e);
			Main.openErrorDialog(e);
			return null;
		}
	}
}
