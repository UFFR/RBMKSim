package simulation.scolumns;

import main.RBMKSimulation;
import simulation.GridLocation;
import simulation.fuels.NeutronType;

public abstract class RBMKFluxReceiverBase extends RBMKSimColumnBase
{
	private static final long serialVersionUID = 7302505600882938899L;

	public RBMKFluxReceiverBase(GridLocation location, RBMKSimulation simulation)
	{
		super(location, simulation);
	}

	public abstract void receiveFlux(NeutronType type, double flux);
}
