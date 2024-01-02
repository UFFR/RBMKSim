package org.uffr.rbmksim.simulation.bcolumns;

import java.io.Serial;
import java.util.Objects;
import java.util.Optional;

import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.fuels.RBMKFuelData;
import org.uffr.rbmksim.simulation.scolumns.RBMKFuel;

import com.google.common.hash.PrimitiveSink;

import javax.annotation.Nullable;

public class RBMKBlueprintFuel extends RBMKBlueprintColumn
{
	@Serial
	private static final long serialVersionUID = -3585125936742359209L;
	private RBMKFuelData fuelData;
	public RBMKBlueprintFuel(RBMKFuel column)
	{
		super(column);
		fuelData = (column.getFuelRod().isPresent() ? column.getFuelRod().get().data : null);
	}
	
	public RBMKBlueprintFuel(GridLocation location, ColumnType columnType, boolean moderated, RBMKFuelData fuelData)
	{
		super(location, columnType, moderated);
		this.fuelData = fuelData;
	}
	
	public void setFuelData(@Nullable RBMKFuelData fuelData)
	{
		this.fuelData = fuelData;
	}
	
	public Optional<RBMKFuelData> getFuelData()
	{
		return Optional.ofNullable(fuelData);
	}
	
	@Override
	public void reset()
	{
		fuelData = null;
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		getFuelData().ifPresent(data -> data.funnelInto(sink));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(fuelData);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RBMKBlueprintFuel other))
			return false;
        return Objects.equals(fuelData, other.fuelData);
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("BlueprintFuel [fuelData=").append(fuelData).append(", getLocation()=").append(getLocation())
				.append(", getColumnType()=").append(getColumnType()).append(", isModerated()=").append(isModerated())
				.append(']');
		return builder.toString();
	}

}
