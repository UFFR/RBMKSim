package org.uffr.rbmksim.simulation.fuels;

public enum FuelCategory
{
	REAL("Real/Realistic Fuels"),
	FUTURISTIC("Futuristic Fuels"),
	SOURCE("Neutron Sources"),
	ZFB("Zirconium Fast Breeders"),
	FICTIONAL("Ficticious Fuels");
	public final String name;
	private FuelCategory(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
