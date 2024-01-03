package org.uffr.rbmksim.simulation.fuels;

import org.uffr.rbmksim.util.I18n;

public enum FuelCategory
{
	REAL("fuel.category.real"),
	FUTURISTIC("fuel.category.futuristic"),
	SOURCE("fuel.category.source"),
	ZFB("fuel.category.zfb"),
	FICTIONAL("fuel.category.fictional");
	public final String uloc;
	FuelCategory(String uloc)
	{
		this.uloc = uloc;
	}
	
	@Override
	public String toString()
	{
		return I18n.resolve(uloc);
	}
}
