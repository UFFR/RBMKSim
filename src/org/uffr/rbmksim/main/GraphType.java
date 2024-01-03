package org.uffr.rbmksim.main;

import org.uffr.rbmksim.util.I18n;

public enum GraphType
{
	HEAT("heat"),
	FLUX("flux"),
	STEAM("steam"),
	POWER("power"),
	COOLANT("coolant");
	
	public final String uloc;
	GraphType(String uloc)
	{
		this.uloc = "app.graphType." + uloc;
	}
	
	@Override
	public String toString()
	{
		return I18n.resolve(uloc);
	}
}