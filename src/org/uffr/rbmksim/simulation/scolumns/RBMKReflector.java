package org.uffr.rbmksim.simulation.scolumns;

import java.io.Serial;
import java.util.Arrays;

import org.uffr.rbmksim.main.dialog.ColumnDialogBase;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;

public class RBMKReflector extends RBMKSimColumnBase
{
	@Serial
	private static final long serialVersionUID = 7139174484901399687L;

	public RBMKReflector(GridLocation location)
	{
		super(location);
	}

	@Override
	public ColumnType getColumnType()
	{
		return ColumnType.REFLECTOR;
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
        return obj instanceof RBMKReflector;
    }

	@Override
	public String toString()
	{
        return "RBMKReflector [heat=" + heat + ", water=" + water + ", steam=" +
                steam + ", heatCache=" + Arrays.toString(heatCache) + ", location=" +
                location + ']';
	}

}
