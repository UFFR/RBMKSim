package org.uffr.rbmksim.simulation.scolumns;

import java.io.Serial;
import java.util.Arrays;

import org.uffr.rbmksim.main.dialog.ColumnDialogBase;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;

public class RBMKAbsorber extends RBMKSimColumnBase
{
	@Serial
	private static final long serialVersionUID = 1700212015824766625L;

	public RBMKAbsorber(GridLocation location)
	{
		super(location);
	}

	@Override
	public ColumnType getColumnType()
	{
		return ColumnType.ABSORBER;
	}

	@Override
	public ColumnDialogBase<?> getMenu()
	{
		return null;
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
        return obj instanceof RBMKAbsorber;
    }

	@Override
	public String toString()
	{
        String builder = "RBMKAbsorber [heat=" + heat + ", water=" + water + ", steam=" +
                steam + ", heatCache=" + Arrays.toString(heatCache) + ", location=" +
                location + ']';
		return builder;
	}

}
