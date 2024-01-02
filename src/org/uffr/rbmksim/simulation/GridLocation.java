package org.uffr.rbmksim.simulation;

import java.io.Serial;
import java.io.Serializable;

import com.google.common.hash.Funnel;
import com.google.errorprone.annotations.Immutable;

@Immutable
public record GridLocation(int x, int y) implements Serializable
{
	@Serial
	private static final long serialVersionUID = 2469677386448724846L;
	public static final Funnel<GridLocation> FUNNEL = (grid, sink) -> sink.putInt(grid.x()).putInt(grid.y());


	@Override
	public GridLocation clone()
	{
		return new GridLocation(x, y);
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
