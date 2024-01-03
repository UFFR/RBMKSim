package org.uffr.rbmksim.simulation.fuels;

public enum NeutronType
{
	ANY("Any Neutron"),
	FAST("Fast Neutrons"),
	SLOW("Slow Neutrons");
	public final String desc;
	NeutronType(String desc)
	{
		this.desc = desc;
	}
}
