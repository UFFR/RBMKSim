package org.uffr.rbmksim.simulation;

import org.uffr.rbmksim.main.RBMKFrame;

public class RBMKRunner implements Runnable
{
	private final RBMKFrame frame;
	private boolean active;
	public RBMKRunner(RBMKFrame frame)
	{
		this.frame = frame;
	}
	
	@Override
	public void run()
	{
		if (active)
			frame.tick();
	}

}
