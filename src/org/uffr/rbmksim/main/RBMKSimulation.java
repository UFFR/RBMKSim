package org.uffr.rbmksim.main;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.RBMKColumnBase;
import org.uffr.rbmksim.simulation.bcolumns.RBMKBlueprintColumn;
import org.uffr.rbmksim.simulation.fuels.FuelRegistry;
import org.uffr.rbmksim.simulation.fuels.RBMKFuelData;
import org.uffr.rbmksim.simulation.fuels.RBMKFuelRod;
import org.uffr.rbmksim.simulation.scolumns.RBMKAbsorber;
import org.uffr.rbmksim.simulation.scolumns.RBMKBlank;
import org.uffr.rbmksim.simulation.scolumns.RBMKBoiler;
import org.uffr.rbmksim.simulation.scolumns.RBMKControl;
import org.uffr.rbmksim.simulation.scolumns.RBMKFuel;
import org.uffr.rbmksim.simulation.scolumns.RBMKSimColumnBase;

import javafx.scene.canvas.Canvas;

public class RBMKSimulation extends RBMKFrame
{
	private static final long serialVersionUID = 5530885107200613696L;
	public static final byte TICK = 50;
	private boolean meltedDown = false, running = false;
	private final transient Timer timer = new Timer("RBMK_SIMULATION");
	private static final TimerTask TICK_TASK = new TimerTask()
		{
			@Override public void run() {Main.getSimulation().tick();}
		};
		
	public RBMKSimulation(Canvas canvas)
	{
		super(canvas);
		timer.schedule(TICK_TASK, TICK, TICK);
	}
	
	@Override
	public void tick()
	{
//		for (RBMKColumnBase c : grid)
//			((RBMKSimColumnBase) c).tick();
		for (GridLocation loc : registeredLocations)
			getColumnAtCoords(loc).tick();
		super.tick();
		if (!running)
			timer.cancel();
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
		for (RBMKColumnBase col : grid)
		{
			final RBMKSimColumnBase simCol = (RBMKSimColumnBase) col;
			if (simCol.getHeat() > simCol.maxHeat())
			{
				meltedDown = true;
				break;
			}
			
			if (simCol instanceof RBMKFuel)
			{
				final RBMKFuel fuel = (RBMKFuel) simCol;
				if (fuel.getFuelRod().isPresent() && fuel.getFuelRod().get().getHullHeat() > fuel.getFuelRod().get().data.meltingPoint())
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
		for (int i = 0, matrixSize = grid.totalCells(); i < matrixSize; i++)
		{
			final GridLocation loc = new GridLocation(i / grid.getCols(), i % grid.getCols());
			final RBMKSimColumnBase column = (RBMKSimColumnBase) grid.get(loc.getX(), loc.getX());
			if (!loc.equals(column.getLocation()))
			{
				discrepancies.add("Column at " + loc + " has an inconsistent with grid location, internally claims to be at " + column.getLocation() + ", possible corruption?");
				if (repair)
				{
					grid.remove(loc.getX(), loc.getY());
					continue;
				}
			}
			if (!validCoords(loc))
			{
				discrepancies.add("Column at " + loc + " is not within the bounds of the simulation.");
				if (repair)
				{
					grid.remove(loc.getX(), loc.getY());
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
					final RBMKFuelData data = fColumn.getFuelRod().get().data;
					if (!FuelRegistry.nameExists(data.name()))
					{
						discrepancies.add("Column at " + loc + " is fuel column with rod of unknown type. Possible removed fuel? " + (repair ? "Repair will reset data." : "Not repairing, skipping column."));
						if (repair)
							column.reset();
						else
							continue;
					}
					if (!FuelRegistry.valueExists(data) || !FuelRegistry.getByName(data.name()).equals(data))
					{
						discrepancies.add("Column at " + loc + " is fuel column and has registered fuel type but type data does not correlate with any registered fuel. Possible outdated blueprint/simulation? " + (repair ? "Repair will reset fuel data." : "Not repairing, skipping column."));
						if (repair)
							fColumn.setFuelRod(new RBMKFuelRod(FuelRegistry.getByName(data.name())));
						else
							continue;
					}
					if (fColumn.getFuelRod().get().getRemainingYield() > data.getYield())
					{
						discrepancies.add("Column at " + loc + " is fuel column and has a fuel rod with a yield higher than its designated maximum. Possible corruption? " + (repair ? "Repair will set yield to maximum." : "Not repairing, skipping column."));
						if (repair)
							fColumn.getFuelRod().get().resetYield();
						else
							continue;
					}
				}
			}
			if (column instanceof RBMKControl)
			{
				final RBMKControl control = (RBMKControl) column;
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
	public void convertColumns()
	{
		// TODO Auto-generated method stub
		for (int i = 0, matrixSize = grid.totalCells(); i < matrixSize; i++)
		{
			final int row = i / columns;
			final int col = i % columns;
			final RBMKColumnBase column = grid.get(col, row);
			if (column == null)
				continue;
			// Should not be any other kind, but check anyway.
			if (column instanceof RBMKBlueprintColumn)
			{
				final GridLocation location = column.getLocation();
				final RBMKSimColumnBase newColumn;
				switch (column.getColumnType())
				{
					case ABSORBER: newColumn = new RBMKAbsorber(location, this); break;
					case BLANK: newColumn = new RBMKBlank(location, this); break;
					case BOILER: newColumn = new RBMKBoiler(location, this); break;
					case BREEDER:
					case OUTGASSER: newColumn = new RBMKBlank(location, this); break; // TODO Setup breeder column type
					default: throw new IllegalStateException("Encountered unknown column type while converting!");
				}
				grid.set(col, row, newColumn);
			} else
				throw new IllegalStateException("Encounted column of illegal type when converting!");
		}
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public void setRunning(boolean running)
	{
		if (running && !isRunning())
			timer.schedule(TICK_TASK, 50, 50);
		
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
		return (loc.getX() <= columns && loc.getY() <= rows) && (loc.getX() >= 0 && loc.getY() >= 0);
	}

	@Override
	public int getRows()
	{
		return rows;
	}
	
	@Override
	public int getColumns()
	{
		return columns;
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
		return grid.contains(column);
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
		if (!(obj instanceof RBMKSimulation))
			return false;
		final RBMKSimulation other = (RBMKSimulation) obj;
		return columns == other.columns && Objects.equals(config, other.config) && Objects.equals(grid, other.grid)
				&& meltedDown == other.meltedDown && rows == other.rows;
	}
	
	@Override
	public RBMKSimulation clone()
	{
		return (RBMKSimulation) super.clone();
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		timer.cancel();
		running = false;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("RBMKSimulation [config=").append(config).append(", grid=").append(grid)
				.append(", registeredLocations=").append(registeredLocations).append(", canvas=").append(canvas)
				.append(", name=").append(name).append(", rows=").append(rows).append(", columns=").append(columns)
				.append(", meltedDown=").append(meltedDown).append(", running=").append(running).append(']');
		return builder.toString();
	}
}
