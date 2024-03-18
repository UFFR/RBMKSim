package org.u_group13.rbmksim.util;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.main.Main;

public class FileOpener implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FileOpener.class);

	private final Path path;
	
	public FileOpener(Path path)
	{
		LOGGER.debug("Created FileOpener instance for path [{}]...", path);
		this.path = path;
	}

	@Override
	public void run()
	{
		try
		{
			Desktop.getDesktop().open(path.toFile());
		} catch (IOException e)
		{
			LOGGER.warn("Unable to open file!", e);
			Main.openErrorDialog(e);
		}
	}

}
