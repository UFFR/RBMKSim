package org.u_group13.rbmksim.simulation.bcolumns;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javafx.scene.text.Text;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.simulation.fuels.FuelType;
import org.u_group13.rbmksim.simulation.scolumns.RBMKFuel;
import org.u_group13.rbmksim.util.I18n;

import com.google.common.hash.PrimitiveSink;

import javax.annotation.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class RBMKBlueprintFuel extends RBMKBlueprintColumn
{
	@Serial
	private static final long serialVersionUID = -3585125936742359209L;
	private FuelType fuelType;
	public RBMKBlueprintFuel(RBMKFuel column)
	{
		super(column);
		fuelType = (column.getFuelRod().isPresent() ? column.getFuelRod().get().type : null);
	}
	
	public RBMKBlueprintFuel(GridLocation location, ColumnType columnType, boolean moderated, FuelType fuelType)
	{
		super(location, columnType, moderated);
		this.fuelType = fuelType;
	}

	@Override
	public void addInformation(List<Text> info)
	{
		super.addInformation(info);
		if (fuelType != null)
			fuelType.data.addInformation(info);
		else
			info.add(new Text(I18n.resolve("column.type.fuel.empty")));
	}

	public void setFuelType(@Nullable FuelType fuelType)
	{
		this.fuelType = fuelType;
	}
	
	public Optional<FuelType> getFuelType()
	{
		return Optional.ofNullable(fuelType);
	}
	
	@Override
	public void reset()
	{
		fuelType = null;
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		if (fuelType != null)
			sink.putInt(fuelType.ordinal());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(fuelType);
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
        return Objects.equals(fuelType , other.fuelType);
	}

	@Override
	public String toString()
	{
		String builder = "BlueprintFuel [fuelData=" + fuelType + ", getLocation()=" + getLocation() +
				", getColumnType()=" + getColumnType() + ", isModerated()=" + isModerated() +
				']';
		return builder;
	}

}
