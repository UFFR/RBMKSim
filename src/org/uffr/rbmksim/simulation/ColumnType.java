package org.uffr.rbmksim.simulation;

import org.uffr.rbmksim.util.I18n;

public enum ColumnType
{
	BLANK(false, "blank"),
	FUEL(true, "fuel"),
	FUEL_SIM(true, "fuelSim"),
	CONTROL(true, "controlManual"),
	CONTROL_AUTO(true, "controlAuto"),
	BOILER(true, "boiler"),
	MODERATOR(false, "moderator"),
	ABSORBER(false, "absorber"),
	REFLECTOR(false, "reflector"),
	OUTGASSER(true, "irradiator"),
	BREEDER(true, "breeder"),
	STORAGE(true, "storage"),
	COOLER(true, "cooler"),
	HEATEX(false, "heatEx");
	public final boolean hasGUI;
	public final String uloc;
	ColumnType(boolean hasGUI, String uloc)
	{
		this.hasGUI = hasGUI;
		this.uloc = "column.type." + uloc;
	}
	
	@Override
	public String toString()
	{
		return I18n.resolve(uloc);
	}
}
