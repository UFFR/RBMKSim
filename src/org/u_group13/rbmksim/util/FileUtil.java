package org.u_group13.rbmksim.util;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser.ExtensionFilter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.main.Main;
import org.u_group13.rbmksim.main.RBMKBlueprint;
import org.u_group13.rbmksim.main.RBMKFrame;
import org.u_group13.rbmksim.main.RBMKSimulation;
import org.uffr.uffrlib.misc.StringUtil;
import org.uffr.uffrlib.misc.Version;

import javax.annotation.CheckForNull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class FileUtil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
	// Basic strings reused in various places.
	public static final String EXT_BPRINT = "*.rbmk", EXT_RSIM   = "*.rsim", EXT_JSON = "*.json";
	public static final ExtensionFilter BLUEPRINT_FILTER	= new ExtensionFilter("RBMK Blueprint File", EXT_BPRINT),
										SIMULATION_FILTER	= new ExtensionFilter("RBMK Simulation File", EXT_RSIM),
										GENERIC_FILTER		= new ExtensionFilter("RBMKSim Save Files", EXT_BPRINT, EXT_RSIM),
										JSON_FILTER = new ExtensionFilter("JSON documents", EXT_JSON);
	public static final long BLUEPRINT_MAGIC	= 0x52424D4B53696D42L,
							 SIMULATION_MAGIC	= 0x52424D4B53696D53L;
	public static final String BLUEPRINT_MAGIC_STRING = "0x" + StringUtil.longToHex(BLUEPRINT_MAGIC).toUpperCase(),
								SIMULATION_MAGIC_STRING = "0x" + StringUtil.longToHex(SIMULATION_MAGIC).toUpperCase();

	public static void writeSaveFile(@NotNull Path path, @NotNull RBMKFrame frame)
	{
		LOGGER.debug("Exporting to: [{}] as {}", path, Main.config.binarySaveFiles ? "binary" : "JSON");
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
	public static RBMKFrame readSaveFile(@NotNull Path path) throws NoSuchFileException
	{
		if (Files.notExists(path))
			throw new NoSuchFileException("Supplied path does not exist!");

		LOGGER.info("Reading saved file...");
		try (final InputStream inputStream = Files.newInputStream(path);
		     final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream))
		{
			LOGGER.trace("Reading magic number...");
			final long magic = objectInputStream.readLong();

			if (magic != BLUEPRINT_MAGIC && magic != SIMULATION_MAGIC)
			{
				final String hexMagic = StringUtil.longToHex(magic).toUpperCase();
				LOGGER.warn("Read wrong magic number from file, got magic number 0x{} instead of {} or {}", hexMagic, BLUEPRINT_MAGIC_STRING, SIMULATION_MAGIC_STRING);
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
				LOGGER.warn("Read different version in file than what is running, querying user...");
				final String diff = I18n.resolve(comparison < 0 ? "dialog.warning.badVersion.diffNew" : "dialog.warning.badVersion.diffOld");

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
