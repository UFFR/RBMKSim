package org.u_group13.rbmksim.main;

import java.io.Serial;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.simulation.RBMKColumnBase;
import org.u_group13.rbmksim.simulation.bcolumns.RBMKBlueprintControl;
import org.u_group13.rbmksim.simulation.scolumns.RBMKBoiler;
import org.u_group13.rbmksim.simulation.scolumns.RBMKControl;
import org.u_group13.rbmksim.simulation.scolumns.RBMKFuel;
import org.u_group13.rbmksim.simulation.scolumns.RBMKSimColumnBase;
import org.u_group13.rbmksim.simulation.bcolumns.RBMKBlueprintBoiler;
import org.u_group13.rbmksim.simulation.bcolumns.RBMKBlueprintColumn;
import org.u_group13.rbmksim.simulation.bcolumns.RBMKBlueprintFuel;

import javafx.scene.canvas.Canvas;

import javax.annotation.Nonnull;

/**
 * RBMK frame only for designing. It only contains the bare minimum of information for that role.
 * <br>
 * Only ticks when changed and only to update the rendering.
 * @author UFFR
 *
 */
public final class RBMKBlueprint extends RBMKFrame
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
	protected RBMKColumnBase newOfType(@NotNull GridLocation location, @NotNull ColumnType type)
	{
		// Experimenting with new switches
		return switch (type)
		{
			case FUEL, FUEL_SIM -> new RBMKBlueprintFuel(location, type, false, null);
			case BOILER			-> new RBMKBlueprintBoiler(location, type);
			case CONTROL        -> new RBMKBlueprintControl(location, type, false);
			default				-> new RBMKBlueprintColumn(location, type, false);
		};
	}

	@Override
	protected RBMKColumnBase convertType(RBMKColumnBase column)
	{
		return switch (column)
		{
			case RBMKBlueprintColumn bCol   -> bCol;
			case RBMKFuel fuel              -> new RBMKBlueprintFuel(fuel);
			case RBMKBoiler boiler          -> new RBMKBlueprintBoiler(boiler);
			case RBMKControl control        -> new RBMKBlueprintControl(control);
			default                         -> new RBMKBlueprintColumn((RBMKSimColumnBase) column);
		};
	}

	@Override
	public void checkForDiscrepancies(List<String> discrepancies, boolean repair)
	{
		LOGGER.info("Checking RBMKBlueprint for errors...");
		String message;
		for (Map.Entry<GridLocation,RBMKColumnBase> entry : grid.entrySet())
		{
			final GridLocation loc = entry.getKey();
			final RBMKColumnBase column = entry.getValue();
			if (column == null)
			{
				message = "Location " + loc + " is registered, but has no column! Possible corruption?";
				LOGGER.trace(message);
				discrepancies.add(message);
				if (repair)
				{
					LOGGER.trace("Fixing...");
					grid.entrySet().remove(entry);
				}
				continue;
			}
			if (!loc.equals(column.getLocation()))
			{
				message = "Column at " + loc + " has an inconsistent with grid location, internally claims to be at " + column.getLocation() + ", possible corruption?";
				LOGGER.trace(message);
				discrepancies.add(message);
				if (repair)
				{
					LOGGER.trace("Fixing...");
					grid.remove(loc);
					continue;
				}
			}
			if (invalidCoords(loc))
			{
				message = "Column at " + loc + " is not within the bounds of the simulation.";
				LOGGER.trace(message);
				discrepancies.add(message);
				if (repair)
				{
					LOGGER.trace("Fixing...");
					grid.remove(loc);
					continue;
				}
			}
		}
	}
	
	@Override
	public RBMKSimulation convertType()
	{
		// TODO Auto-generated method stub
		return new RBMKSimulation(this);
	}

	@Override
	protected void convertColumns()
	{
		LOGGER.debug("Converting columns to correct type...");
		for (Map.Entry<GridLocation,RBMKColumnBase> entry : grid.entrySet())
		{
			final RBMKColumnBase column = entry.getValue();
			// Should not be any other kind, but check anyway.
			if (column instanceof RBMKSimColumnBase simColumn)
				entry.setValue(convertType(simColumn));
			else
				throw new IllegalStateException("Encountered column of illegal type when converting!");
		}
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
		return "RBMKBlueprint [" + "config=" + config +
				", grid=" + grid +
				", canvas=" + canvas +
				", renderer=" + renderer +
				", name='" + name + '\'' +
				", creatorName='" + creatorName + '\'' +
				", version=" + version +
				", date=" + date +
				", rows=" + rows +
				", columns=" + columns +
				", ticks=" + ticks +
				']';
	}
}
