package org.uffr.rbmksim.main;

import java.io.Serial;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.RBMKColumnBase;
import org.uffr.rbmksim.simulation.bcolumns.RBMKBlueprintBoiler;
import org.uffr.rbmksim.simulation.bcolumns.RBMKBlueprintColumn;
import org.uffr.rbmksim.simulation.bcolumns.RBMKBlueprintFuel;
import org.uffr.rbmksim.simulation.scolumns.RBMKBoiler;
import org.uffr.rbmksim.simulation.scolumns.RBMKFuel;
import org.uffr.rbmksim.simulation.scolumns.RBMKSimColumnBase;

import javafx.scene.canvas.Canvas;

import javax.annotation.Nonnull;

/**
 * RBMK frame only for designing. It only contains the bare minimum of information for that role.
 * <br>
 * Only ticks when changed and only to update the rendering.
 * @author UFFR
 *
 */
public class RBMKBlueprint extends RBMKFrame
{
	@Serial
	private static final long serialVersionUID = 2357399660826941002L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RBMKBlueprint.class);

	public RBMKBlueprint(Canvas canvas)
	{
		super(canvas);
		LOGGER.debug("Generic RBMKBlueprint constructed");
	}
	
	public RBMKBlueprint(@Nonnull RBMKFrame frame)
	{
		super(frame);
		LOGGER.debug("RBMKBlueprint copy constructor called");
		
		if (frame instanceof RBMKSimulation)
			convertColumns();
		else if (!(frame instanceof RBMKBlueprint))
			throw new IllegalArgumentException("Frame is of unknown type: " + frame.getClass());
	}
	
	@Override
	protected RBMKColumnBase newOfType(GridLocation location, ColumnType type)
	{
		// Experimenting with new switches
		switch (type)
		{
			default				-> {return new RBMKBlueprintColumn(location, type, false);}
			case FUEL, FUEL_SIM -> {return new RBMKBlueprintFuel(location, type, false, null);}
			case BOILER			-> {return new RBMKBlueprintBoiler(location, type);}
		}
	}
	
	@Override
	public void checkForDiscrepancies(List<String> discrepancies, boolean repair)
	{
		LOGGER.debug("Checking for errors...");
		String message;
//		for (RBMKColumnBase column : grid)
		for (GridLocation loc : registeredLocations)
		{
//			final GridLocation loc = column.getLocation();
			final RBMKColumnBase column = getColumnAtCoords(loc);
			if (!loc.equals(column.getLocation()))
			{
				message = "Column at " + loc + " has an inconsistent with grid location, internally claims to be at " + column.getLocation() + ", possible corruption?";
				LOGGER.trace(message);
				discrepancies.add(message);
				if (repair)
				{
					LOGGER.trace("Fixing...");
					grid.remove(loc.x(), loc.x());
					registeredLocations.remove(loc);
					continue;
				}
			}
			if (!validCoords(loc))
			{
				message = "Column at " + loc + " is not within the bounds of the simulation.";
				LOGGER.trace(message);
				discrepancies.add(message);
				if (repair)
				{
					LOGGER.trace("Fixing...");
					grid.remove(loc.x(), loc.x());
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
	protected void convertColumns()
	{
		LOGGER.debug("Converting columns to correct type...");
		for (int i = 0, matrixSize = grid.totalCells(); i < matrixSize; i++)
		{
			final int row = i / columns;
			final int col = i % columns;
			final RBMKColumnBase column = grid.get(col, row);
			if (column == null)
				continue;
			// Should not be any other kind, but check anyway.
			if (column instanceof RBMKSimColumnBase simColumn)
			{
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
				throw new IllegalStateException("Encountered column of illegal type when converting!");
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
        return obj instanceof RBMKBlueprint;
    }

	@Override
	public String toString()
	{
		final int maxLen = 10;
		final StringBuilder builder = new StringBuilder();
		builder.append("RBMKBlueprint [config=").append(config).append(", grid=").append(grid)
				.append(", registeredLocations=")
				.append(registeredLocations != null ? toString(registeredLocations) : null).append(", rows=")
				.append(rows).append(", columns=").append(columns).append(']');
		return builder.toString();
	}
	
	private static String toString(Collection<?> collection)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append('[');
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < 10; i++)
		{
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append(']');
		return builder.toString();
	}

}
