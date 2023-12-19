package org.uffr.rbmksim.main;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameRunner implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FrameRunner.class);
	private Optional<RBMKFrame> frame = Optional.empty();
	private boolean active;
	/*public FrameRunner(RBMKFrame frame)
	{
		this.frame = Optional.ofNullable(frame);
	}*/
	
	public FrameRunner()
	{
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run()
	{
		if (active)
		{
			frame.get().tick();
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
		return frame;
	}
	
	public void setFrame(RBMKFrame frame)
	{
		this.frame = Optional.ofNullable(frame);
	}
	
	public void close()
	{
		frame = Optional.empty();
	}
	
}
