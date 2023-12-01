package simulation.scolumns;

import java.util.Arrays;

import main.RBMKSimulation;
import main.dialog.ColumnDialogBase;
import simulation.ColumnType;
import simulation.GridLocation;

public class RBMKBlank extends RBMKSimColumnBase
{
	private static final long serialVersionUID = -9157288903978474500L;

	public RBMKBlank(GridLocation location, RBMKSimulation simulation)
	{
		super(location, simulation);
	}
	
	@Override
	public ColumnType getColumnType()
	{
		return ColumnType.BLANK;
	}
	
	@Override
	public ColumnDialogBase<?> getMenu()
	{
		return null;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RBMKBlank))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("RBMKBlank [heat=").append(heat).append(", water=").append(water).append(", steam=")
				.append(steam).append(", heatCache=").append(Arrays.toString(heatCache)).append(", location=")
				.append(location).append(", simulation=").append(rbmkFrame).append(']');
		return builder.toString();
	}

}
