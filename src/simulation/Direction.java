package simulation;

public enum Direction
{
	NORTH,
	SOUTH,
	EAST,
	WEST;
	
	public GridLocation getNeighbor(GridLocation start)
	{
		switch (this)
		{
			case NORTH: return new GridLocation(start.getX(), start.getY() - 1);
			case SOUTH: return new GridLocation(start.getX(), start.getY() + 1);
			case EAST: return new GridLocation(start.getX() + 1, start.getY());
			case WEST: return new GridLocation(start.getX() - 1, start.getY());
			default: return start.clone();
		}
	}
}
