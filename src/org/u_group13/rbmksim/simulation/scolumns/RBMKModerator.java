package org.u_group13.rbmksim.simulation.scolumns;

import java.io.Serial;
import java.util.Arrays;

import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.main.dialog.ColumnDialogBase;

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
