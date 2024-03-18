package org.u_group13.rbmksim.main;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameRunner implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FrameRunner.class);
	private RBMKFrame frame = null;
	private boolean active;
	
	public FrameRunner()
	{
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run()
	{
		if (active)
		{
			if (frame == null)
			{
				LOGGER.warn("Tried to tick while frame is null!");
				return;
			}
			frame.tick();
			try
			{
				Thread.sleep(50 + Main.config.tickDelay);
			} catch (InterruptedException e)
			{
				Main.openErrorDialog(e);
				LOGGER.warn("Frame runner thread interrupted!", e);
			}
		}
	}

	public boolean canRun()
	{
		return frame != null;
	}

	public boolean isActive()
	{
		return active;
	}
	
	public void setActive(boolean active)
	{
		this.active = active;
	}
	
	public Optional<RBMKFrame> getFrame()
	{
		return Optional.ofNullable(frame);
	}
	
	public void setFrame(RBMKFrame frame)
	{
		this.frame = frame;
	}
	
	public void close()
	{
		frame = null;
	}
	
}
