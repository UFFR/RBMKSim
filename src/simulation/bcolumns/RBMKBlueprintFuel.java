package simulation.bcolumns;

import java.util.Objects;
import java.util.Optional;

import com.google.common.hash.PrimitiveSink;

import main.RBMKFrame;
import simulation.ColumnType;
import simulation.GridLocation;
import simulation.fuels.RBMKFuelData;
import simulation.scolumns.RBMKFuel;

public class RBMKBlueprintFuel extends RBMKBlueprintColumn
{
	private static final long serialVersionUID = -3585125936742359209L;
	private Optional<RBMKFuelData> fuelData;
	public RBMKBlueprintFuel(RBMKFuel column)
	{
		super(column);
		fuelData = Optional.ofNullable(column.getFuelRod().isPresent() ? column.getFuelRod().get().data : null);
	}
	
	public RBMKBlueprintFuel(GridLocation location, RBMKFrame rbmkFrame, ColumnType columnType, boolean moderated, RBMKFuelData fuelData)
	{
		super(location, rbmkFrame, columnType, moderated);
		this.fuelData = Optional.ofNullable(fuelData);
	}
	
	public void setFuelData(RBMKFuelData fuelData)
	{
		this.fuelData = Optional.ofNullable(fuelData);
	}
	
	public Optional<RBMKFuelData> getFuelData()
	{
		return fuelData;
	}
	
	@Override
	public void reset()
	{
		fuelData = Optional.empty();
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		fuelData.ifPresent(data -> data.funnelInto(sink));
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
		if (!(obj instanceof RBMKBlueprintFuel))
			return false;
		final RBMKBlueprintFuel other = (RBMKBlueprintFuel) obj;
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
