package org.uffr.rbmksim.simulation;

import java.io.Serializable;
import java.util.Objects;

import com.google.common.hash.Funnel;

public class GridLocation implements Serializable
{
	private static final long serialVersionUID = 2469677386448724846L;
	public static final Funnel<GridLocation> FUNNEL = (grid, sink) -> sink.putInt(grid.getX()).putInt(grid.getY());
	private final int x, y;
	public GridLocation(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	@Override
	public GridLocation clone()
	{
		return new GridLocation(x, y);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof GridLocation))
			return false;
		final GridLocation other = (GridLocation) obj;
		return x == other.x && y == other.y;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("GridLocation [x=").append(x).append(", y=").append(y).append(']');
		return builder.toString();
	}
}
