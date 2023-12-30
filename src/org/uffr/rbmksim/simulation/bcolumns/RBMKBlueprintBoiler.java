package org.uffr.rbmksim.simulation.bcolumns;

import java.util.Objects;

import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.FluidType;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.scolumns.RBMKBoiler;
import org.uffr.rbmksim.simulation.scolumns.RBMKBoiler.FluidTank;

import com.google.common.hash.PrimitiveSink;

public class RBMKBlueprintBoiler extends RBMKBlueprintColumn
{
	private static final long serialVersionUID = -4867177641210935981L;
	private final FluidTank steamTank, waterTank;
	public RBMKBlueprintBoiler(RBMKBoiler boiler)
	{
		super(boiler);
		steamTank = new FluidTank(boiler.getSteamType(), 1000000);
		waterTank = new FluidTank(FluidType.WATER, 10000);
	}
	
	public RBMKBlueprintBoiler(GridLocation location, ColumnType columnType)
	{
		super(location, columnType, false);
		steamTank = new FluidTank(FluidType.STEAM, 1000000);
		waterTank = new FluidTank(FluidType.WATER, 10000);
	}
	
	@Override
	public void reset()
	{
		steamTank.setFill(0);
		steamTank.setFluidType(FluidType.STEAM);
		waterTank.setFill(0);
	}

	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		FluidTank.FUNNEL.funnel(steamTank, sink);
		FluidTank.FUNNEL.funnel(waterTank, sink);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(steamTank, waterTank);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RBMKBlueprintBoiler))
			return false;
		final RBMKBlueprintBoiler other = (RBMKBlueprintBoiler) obj;
		return Objects.equals(steamTank, other.steamTank) && Objects.equals(waterTank, other.waterTank);
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("BlueprintBoiler [steamTank=").append(steamTank).append(", waterTank=").append(waterTank)
				.append(", getLocation()=").append(getLocation()).append(", getColumnType()=")
				.append(getColumnType()).append(", isModerated()=").append(isModerated()).append(']');
		return builder.toString();
	}
	
}
