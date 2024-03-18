package org.u_group13.rbmksim.simulation;

import org.u_group13.rbmksim.util.I18n;

public enum FluidType
{
	WATER("fluid.water", 20),
	STEAM("fluid.steam", 100),
	DENSE_STEAM("fluid.denseSteam", 300),
	SUPER_DENSE_STEAM("fluid.superSteam", 450),
	ULTRA_DENSE_STEAM("fluid.ultraSteam", 600);
	public final int temperature;
	public final String name;
	FluidType(String name, int temperature)
	{
		this.name = name;
		this.temperature = temperature;
	}
	
	@Override
	public String toString()
	{
		return I18n.resolve("fluid.withTemp", I18n.resolve(name), temperature);
	}
}
