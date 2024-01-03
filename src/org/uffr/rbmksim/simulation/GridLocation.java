package org.uffr.rbmksim.simulation;

import java.io.Serial;
import java.io.Serializable;

import com.google.common.hash.Funnel;
import com.google.errorprone.annotations.Immutable;

@SuppressWarnings("UnstableApiUsage")
@Immutable
public record GridLocation(int x, int y) implements Serializable
{
	@Serial
	private static final long serialVersionUID = 2469677386448724846L;
	public static final Funnel<GridLocation> FUNNEL = (grid, sink) -> sink.putInt(grid.x()).putInt(grid.y());

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof GridLocation other))
			return false;
		return x == other.x && y == other.y;
	}

	@Override
	public String toString()
	{
		return "GridLocation [x=" + x + ", y=" + y + ']';
	}
}
