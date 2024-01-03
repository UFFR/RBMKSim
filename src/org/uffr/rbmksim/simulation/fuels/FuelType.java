package org.uffr.rbmksim.simulation.fuels;

import com.google.common.collect.ImmutableSet;
import org.uffr.rbmksim.util.I18n;

import java.util.Set;

public enum FuelType
{
	NU("ueu", new RBMKFuelBuilder("ueu", 15, 2865, 0).construct()),
	DRX("drx", new RBMKFuelBuilder("drx", 10_000, 1_000_000, 0).setBurnFunction(EnumBurnFunction.QUADRATIC).construct());

	public static final Set<FuelType> FUEL_TYPES = ImmutableSet.copyOf(values());

	public final String ulocName, ulocFullName;
	public final RBMKFuelData data;

	FuelType(String uloc , RBMKFuelData data)
	{
		this.ulocName = "fuel.type.name." + uloc;
		this.ulocFullName = "fuel.type.desc." + uloc;
		this.data = data;
	}

	public String getName()
	{
		return I18n.resolve(ulocName);
	}

	public String getFullName()
	{
		return I18n.resolve(ulocFullName);
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
