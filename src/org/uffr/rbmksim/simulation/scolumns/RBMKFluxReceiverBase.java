package org.uffr.rbmksim.simulation.scolumns;

import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.fuels.NeutronType;

public abstract class RBMKFluxReceiverBase extends RBMKSimColumnBase
{
	private static final long serialVersionUID = 7302505600882938899L;

	public RBMKFluxReceiverBase(GridLocation location)
	{
		super(location);
	}

	public abstract void receiveFlux(NeutronType type, double flux);
}
