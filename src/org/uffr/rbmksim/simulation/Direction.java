package org.uffr.rbmksim.simulation;

public enum Direction
{
	NORTH,
	SOUTH,
	EAST,
	WEST;
	
	public GridLocation getNeighbor(GridLocation start)
	{
        return switch (this)
        {
            case NORTH -> new GridLocation(start.x(), start.y() - 1);
            case SOUTH -> new GridLocation(start.x(), start.y() + 1);
            case EAST -> new GridLocation(start.x() + 1, start.y());
            case WEST -> new GridLocation(start.x() - 1, start.y());
            default -> start.clone();
        };
	}
}
