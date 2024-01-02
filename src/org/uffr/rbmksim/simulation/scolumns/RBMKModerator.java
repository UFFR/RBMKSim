package org.uffr.rbmksim.simulation.scolumns;

import java.io.Serial;
import java.util.Arrays;

import org.uffr.rbmksim.main.dialog.ColumnDialogBase;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;

public class RBMKModerator extends RBMKSimColumnBase
{
	@Serial
	private static final long serialVersionUID = 119616160814359215L;
	public RBMKModerator(GridLocation location)
	{
		super(location);
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
        return obj instanceof RBMKModerator;
    }

	@Override
	public String toString()
	{
        return "RBMKModerator [heat=" + heat + ", water=" + water + ", steam=" +
                steam + ", heatCache=" + Arrays.toString(heatCache) + ", location=" +
                location + ']';
	}

}
