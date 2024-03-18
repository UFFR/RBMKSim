package org.u_group13.rbmksim.main;

import javafx.scene.canvas.Canvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.simulation.RBMKColumnBase;
import org.u_group13.rbmksim.simulation.bcolumns.RBMKBlueprintBoiler;
import org.u_group13.rbmksim.simulation.bcolumns.RBMKBlueprintColumn;
import org.u_group13.rbmksim.simulation.bcolumns.RBMKBlueprintFuel;
import org.u_group13.rbmksim.simulation.fuels.FuelType;
import org.u_group13.rbmksim.simulation.fuels.RBMKFuelRod;
import org.u_group13.rbmksim.simulation.scolumns.*;

import java.io.Serial;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RBMKSimulation extends RBMKFrame
{
	@Serial
	private static final long serialVersionUID = 5530885107200613696L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RBMKSimulation.class);
	// Delay between each tick in milliseconds, so there are 20 ticks per second by default
	@Deprecated
	public static final byte TICK = 50;
	private boolean meltedDown = false, running = false;

	public RBMKSimulation(Canvas canvas)
	{
		super(canvas);
	}

	public RBMKSimulation(RBMKBlueprint blueprint)
	{
		super(blueprint);
		// TODO Copy constructor
	}

	public RBMKSimulation(RBMKSimulation simulation)
	{
		super(simulation);
		this.meltedDown = simulation.meltedDown;
		this.running = simulation.running;
		// TODO Copy constructor
	}
	
	@Override
	protected RBMKColumnBase newOfType(GridLocation location, ColumnType type)
	{
		// TODO FuelSim, control rods, outgasser, breeder, storage, cooler, heat exchanger
		return switch (type)
		{
			case BLANK			-> new RBMKBlank(location);
			case FUEL, FUEL_SIM -> new RBMKFuel(location, false);
			case BOILER			-> new RBMKBoiler(location);
			case MODERATOR		-> new RBMKModerator(location);
			case ABSORBER		-> new RBMKAbsorber(location);
			case REFLECTOR		-> new RBMKReflector(location);
			default				-> throw new IllegalArgumentException("Unimplemented type: " + type);
		};
	}

	@Override
	protected RBMKColumnBase convertType(RBMKColumnBase column)
	{
		return switch (column)
		{
			case RBMKSimColumnBase sCol     -> sCol;
			case RBMKBlueprintBoiler boiler -> new RBMKBoiler(boiler);
			case RBMKBlueprintFuel fuel     -> new RBMKFuel(fuel);
			case RBMKBlueprintColumn bCol   -> newOfType(bCol.getLocation(), bCol.getColumnType());
			default                         -> throw new IllegalArgumentException("Unimplemented type: " + column.getClass());
		};
	}

	@Override
	public void tick()
	{
		super.tick();
		grid.values().forEach(value -> ((RBMKSimColumnBase) value).tick());

	}
	
	public void triggerMeltdown()
	{
		meltedDown = true;
	}
	
	public boolean isMeltedDown()
	{
		return meltedDown;
	}
	
	public boolean recheckIfMeltedDown()
	{
		meltedDown = false;
		for (RBMKColumnBase col : grid.values())
		{
			final RBMKSimColumnBase simCol = (RBMKSimColumnBase) col;
			if (simCol.getHeat() > simCol.maxHeat())
			{
				meltedDown = true;
				break;
			}
			
			if (simCol instanceof RBMKFuel fuel)
			{
				if (fuel.getFuelRod().isPresent() && fuel.getFuelRod().get().getHullHeat() > fuel.getFuelRod().get().type.data.meltingPoint())
				{
					meltedDown = true;
					break;
				}
			}
		}
		return meltedDown;
	}
	
	@Override
	public void checkForDiscrepancies(List<String> discrepancies, boolean repair)
	{
		final Iterator<Map.Entry<GridLocation, RBMKColumnBase>> iterator = grid.entrySet().iterator();
		while (iterator.hasNext())
		{
			final Map.Entry<GridLocation, RBMKColumnBase> entry = iterator.next();
			final GridLocation loc = entry.getKey();
			final RBMKSimColumnBase column = (RBMKSimColumnBase) entry.getValue();
			if (!loc.equals(column.getLocation()))
			{
				discrepancies.add("Column at " + loc + " has an inconsistent with grid location, internally claims to be at " + column.getLocation() + ", possible corruption?");
				if (repair)
				{
					iterator.remove();
					continue;
				}
			}
			if (invalidCoords(loc))
			{
				discrepancies.add("Column at " + loc + " is not within the bounds of the simulation.");
				if (repair)
				{
					removeColumn(loc);
					continue;
				}
			}
			if (column.getHeat() < 0)
			{
				discrepancies.add("Column at " + loc + " has negative heat value, possible corruption or simulation error?");
				if (repair)
					column.reset();
			}
			if (column instanceof RBMKFuel)
			{
				if (((RBMKFuel) column).getFuelRod().isPresent())
				{
					final RBMKFuel fColumn = (RBMKFuel) column;
					final RBMKFuelRod rod = fColumn.getFuelRod().get();
					final FuelType type = rod.type;
					if (FuelType.FUEL_TYPES.contains(type))
					{
						discrepancies.add("Column at " + loc + " is fuel column with rod of unknown type. Possible removed fuel? " + (repair ? "Repair will reset column." : "Not repairing, skipping column."));
						if (repair)
							column.reset();
						else
							continue;
					}
					if (rod.getRemainingYield() > type.data.getYield())
					{
						discrepancies.add("Column at " + loc + " is fuel column and has a fuel rod with a yield higher than its designated maximum. Possible corruption? " + (repair ? "Repair will set yield to maximum." : "Not repairing, skipping column."));
						if (repair)
							rod.resetYield();
						else
							continue;
					} else if (rod.getRemainingYield() < 0)
					{
						discrepancies.add("Column at " + loc + " is fuel column and has a fuel rod with a negative yield. Possible corruption? " + (repair ? "Repair will set yield to 0." : "Not repairing, skipping column."));
						if (repair)
							rod.setYield(0);
						else
							continue;
					}
				}
			}
			if (column instanceof RBMKControl control)
			{
                if (control.getLevel() < 0 || control.getLevel() > 1)
				{
					discrepancies.add("Column at " + loc + " is control column and its level is out of bounds (" + control.getLevel() + "). " + (repair ? "Repair will set to the closest bound.": "Not repairing, skipping column."));
					if (repair)
					{
						if (control.getLevel() > 1) control.setLevel(1);
						if (control.getLevel() < 0) control.setLevel(0);
					} else
						continue;
				}
				if (control.getTargetLevel() < 0 || control.getTargetLevel() > 1)
				{
					discrepancies.add("Column at " + loc + " is control column and its target level is out of bounds. " + (repair ? "Repair will set to current level.": "Not repairing, skipping column."));
					if (repair)
						control.setTargetLevel(control.getLevel());
					else
						continue;
				}
			}
		}
	}
	
	@Override
	public RBMKFrame convertType()
	{
		return new RBMKBlueprint(this);
	}
	
	@Override
	protected void convertColumns()
	{
		// TODO Auto-generated method stub
		for (Map.Entry<GridLocation,RBMKColumnBase> entry : grid.entrySet())
		{
			final RBMKColumnBase column = entry.getValue();
			if (column == null)
				continue;
			// Should not be any other kind, but check anyway.
			if (column instanceof RBMKBlueprintColumn)
				entry.setValue(getConvertedColumn(column));
			else
				throw new IllegalStateException("Encountered column of illegal type when converting!");
		}
	}

	private static RBMKSimColumnBase getConvertedColumn(RBMKColumnBase column)
	{
		final GridLocation location = column.getLocation();
		return switch (column.getColumnType())
		{
			case ABSORBER           -> new RBMKAbsorber(location);
			case BLANK              -> new RBMKBlank(location);
			case BOILER             -> new RBMKBoiler(location);
			case BREEDER, OUTGASSER -> new RBMKBlank(location); // TODO Setup breeder column type
			default                 -> throw new IllegalStateException("Encountered unknown column type while converting!");
		};
	}

	public boolean isRunning()
	{
		return running;
	}
	
	public void setRunning(boolean running)
	{
		this.running = running;
	}
	
	@Override
	public RBMKSimColumnBase getColumnAtCoords(int x, int y)
	{
		return (RBMKSimColumnBase) super.getColumnAtCoords(x, y);
	}
	
	@Override
	public RBMKSimColumnBase getColumnAtCoords(GridLocation location)
	{
		return (RBMKSimColumnBase) super.getColumnAtCoords(location);
	}
	
	@Override
	public boolean validCoords(int x, int y)
	{
		if (x < 0 || y < 0)
			return false;
		return validCoords(new GridLocation(x, y));
	}
	
	@Override
	public boolean validCoords(GridLocation loc)
	{
		return (loc.x() <= columns && loc.y() <= rows) && (loc.x() >= 0 && loc.y() >= 0);
	}

    @Override
	public void setRows(int rows)
	{
		final boolean shrank = getRows() > rows;
		this.rows = rows;
		if (shrank)
			removeInvalid();
	}
	
	@Override
	public void setColumns(int columns)
	{
		final boolean shrank = getColumns() > columns;
		this.columns = columns;
		if (shrank)
			removeInvalid();
	}
	
	@Override
	public void incrementRows()
	{
		rows++;
	}
	
	@Override
	public void incrementColumns()
	{
		columns++;
	}
	
	@Override
	public void decrementRows()
	{
		rows--;
		removeInvalid();
	}
	
	@Override
	public void decrementColumns()
	{
		columns--;
		removeInvalid();
	}

	public boolean columnInSim(RBMKSimColumnBase column)
	{
		return grid.containsValue(column);
	}
	
	@Override
	public void reset()
	{
		super.reset();
		meltedDown = false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(columns, config, grid, meltedDown, rows);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RBMKSimulation other))
			return false;
        return columns == other.columns && Objects.equals(config, other.config) && Objects.equals(grid, other.grid)
				&& meltedDown == other.meltedDown && rows == other.rows;
	}

	@Override
	public String toString()
	{
		return "RBMKSimulation [" + "meltedDown=" + meltedDown +
				", running=" + running +
				", config=" + config +
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
