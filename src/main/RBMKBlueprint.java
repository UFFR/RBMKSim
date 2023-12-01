package main;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javafx.scene.canvas.Canvas;
import simulation.GridLocation;
import simulation.RBMKColumnBase;
import simulation.bcolumns.RBMKBlueprintBoiler;
import simulation.bcolumns.RBMKBlueprintFuel;
import simulation.scolumns.RBMKBoiler;
import simulation.scolumns.RBMKFuel;
import simulation.scolumns.RBMKSimColumnBase;

/**
 * RBMK frame only for designing. It only contains the bare minimum of information for that role.
 * <br>
 * Only ticks when changed and only to update the rendering.
 * @author UFFR
 *
 */
public class RBMKBlueprint extends RBMKFrame
{
	private static final long serialVersionUID = 2357399660826941002L;

	public RBMKBlueprint(Canvas canvas)
	{
		super(canvas);
	}
	
	public RBMKBlueprint(RBMKFrame frame)
	{
		super(frame);
		
		if (frame instanceof RBMKSimulation)
			convertColumns();
		else
			throw new IllegalArgumentException("Frame is of unknown type: " + frame.getClass());
	}
	
	@Override
	public void checkForDiscrepancies(List<String> discrepancies, boolean repair)
	{
		for (RBMKColumnBase column : grid)
		{
			final GridLocation loc = column.getLocation();
//			final RBMKColumnBase column = grid.get(loc.getX(), loc.getX());
			if (!loc.equals(column.getLocation()))
			{
				discrepancies.add("Column at " + loc + " has an inconsistent with grid location, internally claims to be at " + column.getLocation() + ", possible corruption?");
				if (repair)
				{
					grid.remove(loc.getX(), loc.getX());
					registeredLocations.remove(loc);
					continue;
				}
			}
			if (!validCoords(loc))
			{
				discrepancies.add("Column at " + loc + " is not within the bounds of the simulation.");
				if (repair)
				{
					grid.remove(loc.getX(), loc.getX());
					registeredLocations.remove(loc);
					continue;
				}
			}
//			if (column.getHeat() != 20)
//			{
//				discrepancies.add("Column at " + loc + " has a heat level of " + column.getHeat() + ", which should not be possible for a blueprint.");
//				if (repair)
//				{
//					column.reset();
//					continue;
//				}
//			}
//			if (config.reasimBoilers)
//			{
//				if (column.getWater() != 0)
//				{
//					discrepancies.add("Column at " + loc + " has a water level of " + column.getWater() + ", which should not be possible for a blueprint.");
//					if (repair)
//					{
//						column.reset();
//						continue;
//					}
//				}
//				if (column.getSteam() != 0)
//				{
//					discrepancies.add("Column at " + loc + " has a steam level of " + column.getSteam() + ", which should not be possible for a blueprint.");
//					if (repair)
//					{
//						column.reset();
//						continue;
//					}
//				}
//			}
		}
	}
	
	@Override
	public RBMKSimulation convertType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void convertColumns()
	{
		for (int i = 0, matrixSize = grid.totalCells(); i < matrixSize; i++)
		{
			final int row = i / columns;
			final int col = i % columns;
			final RBMKColumnBase column = grid.get(col, row);
			if (column == null)
				continue;
			// Should not be any other kind, but check anyway.
			if (column instanceof RBMKSimColumnBase)
			{
				final RBMKSimColumnBase simColumn = (RBMKSimColumnBase) column;
				switch (simColumn.getColumnType())
				{
					case FUEL:
					case FUEL_SIM:
						grid.set(col, row, new RBMKBlueprintFuel((RBMKFuel) simColumn));
						break;
					case BOILER:
						grid.set(col, row, new RBMKBlueprintBoiler((RBMKBoiler) simColumn));
						break;
					default:
						grid.set(col, row, simColumn);
						break;
				}
			} else
				throw new IllegalStateException("Encounted column of illegal type when converting!");
		}
	}
	
	@Override
	public RBMKBlueprint clone()
	{
		return (RBMKBlueprint) super.clone();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RBMKBlueprint))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		final int maxLen = 10;
		final StringBuilder builder = new StringBuilder();
		builder.append("RBMKBlueprint [config=").append(config).append(", grid=").append(grid)
				.append(", registeredLocations=")
				.append(registeredLocations != null ? toString(registeredLocations, maxLen) : null).append(", rows=")
				.append(rows).append(", columns=").append(columns).append(']');
		return builder.toString();
	}
	
	private static String toString(Collection<?> collection, int maxLen)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append('[');
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++)
		{
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append(']');
		return builder.toString();
	}

}