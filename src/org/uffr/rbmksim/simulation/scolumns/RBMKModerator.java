package org.uffr.rbmksim.simulation.scolumns;

import java.util.Arrays;

import org.uffr.rbmksim.main.RBMKSimulation;
import org.uffr.rbmksim.main.dialog.ColumnDialogBase;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;

public class RBMKModerator extends RBMKSimColumnBase
{
	private static final long serialVersionUID = 119616160814359215L;
	public RBMKModerator(GridLocation location, RBMKSimulation simulation)
	{
		super(location, simulation);
	}

	@Override
	public ColumnType getColumnType()
	{
		return ColumnType.MODERATOR;
	}

	@Override
	public ColumnDialogBase<?> getMenu()
	{
		return null;
	}
	
	@Override
	public boolean isModerated()
	{
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RBMKModerator))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("RBMKModerator [heat=").append(heat).append(", water=").append(water).append(", steam=")
				.append(steam).append(", heatCache=").append(Arrays.toString(heatCache)).append(", location=")
				.append(location).append(", simulation=").append(rbmkFrame).append(']');
		return builder.toString();
	}

}