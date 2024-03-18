package org.u_group13.rbmksim.simulation.scolumns;

import java.io.Serial;
import java.util.Arrays;

import org.u_group13.rbmksim.main.dialog.ColumnDialogBase;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;

public class RBMKBlank extends RBMKSimColumnBase
{
	@Serial
	private static final long serialVersionUID = -9157288903978474500L;

	public RBMKBlank(GridLocation location)
	{
		super(location);
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
        return obj instanceof RBMKBlank;
    }

	@Override
	public String toString()
	{
        String builder = "RBMKBlank [heat=" + heat + ", water=" + water + ", steam=" +
                steam + ", heatCache=" + Arrays.toString(heatCache) + ", location=" +
                location + ']';
		return builder;
	}

}
