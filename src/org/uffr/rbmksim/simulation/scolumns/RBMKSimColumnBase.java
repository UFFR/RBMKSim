package org.uffr.rbmksim.simulation.scolumns;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.hash.PrimitiveSink;
import org.uffr.rbmksim.main.RBMKSimulation;
import org.uffr.rbmksim.main.dialog.ColumnDialogBase;
import org.uffr.rbmksim.simulation.Direction;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.RBMKColumnBase;
import org.uffr.rbmksim.util.RBMKRenderHelper;

import javafx.scene.canvas.GraphicsContext;

public abstract class RBMKSimColumnBase extends RBMKColumnBase
{
	private static final long serialVersionUID = 3500879413794578688L;
	
	protected transient final RBMKSimColumnBase[] heatCache = new RBMKSimColumnBase[4];
	protected double heat;
	protected short water, steam;// Change to regular int if causes issues
	public RBMKSimColumnBase(GridLocation location, RBMKSimulation simulation)
	{
		super(location, simulation);
	}
	
	public abstract ColumnDialogBase<? extends RBMKSimColumnBase> getMenu();
	
	@SuppressWarnings("static-method")
	public double maxHeat()
	{
		return MAX_HEAT_DEFAULT;
	}
	
	public double passiveCooling()
	{
		return getConfig().passiveCooling;
	}
	
	@Override
	public void render(GraphicsContext graphics)
	{
		RBMKRenderHelper.genericRender(getColumnType(), location, graphics);
	}
	
	@Override
	public boolean isModerated()
	{
		return false;
	}
	
	public void tick()
	{
		if (getConfig().reasimBoilers)
			boilWater();
		
		moveHeat();
		
		coolPassively();
	}
	
	private void boilWater()
	{
		if (heat < 100)
			return;
		
		final double availHeat = (heat - 100) / getConfig().boilerHeatConsumption;
		final short availSpace = (short) (MAX_STEAM - steam);
		final short processed = (short) Math.floor(Math.min(availHeat, Math.min(water, availSpace)) * getConfig().reasimBoilerSpeed);
		water -= processed;
		steam += processed;
		heat -= processed * getConfig().boilerHeatConsumption;
	}
	
	private void moveHeat()
	{
		final List<RBMKSimColumnBase> cacheColumns = new ArrayList<>();
		cacheColumns.add(this);
		int index = 0;
		for (Direction dir : Direction.values())
		{
			if (heatCache[index] == null)
				heatCache[index] = (RBMKSimColumnBase) rbmkFrame.getColumnAtCoords(dir.getNeighbor(location));
			index++;
		}
		
		for (RBMKSimColumnBase column : heatCache)
		{
			if (column != null)
			{
				cacheColumns.add(column);
				heat += column.heat;
				water += column.water;
				steam += column.steam;
			}
		}
		
		final double step = getConfig().columnHeatFlow;
		final int cacheSize = cacheColumns.size();
		if (cacheSize > 1)
		{
			final double target = heat / cacheSize;
			final int
					tWater = water / cacheSize,
					rWater = water % cacheSize,
					tSteam = steam / cacheSize,
					rSteam = steam % cacheSize;
			
			for (RBMKSimColumnBase column : cacheColumns)
			{
				final double delta = target - column.heat;
				column.heat += delta * step;
				column.water = (short) tWater;
				column.steam = (short) tSteam;
			}
			
			water += rWater;
			steam += rSteam;
		}
	}
	
	protected void coolPassively()
	{
		heat -= passiveCooling();
		
		if (heat < 20) heat = 20;
	}
	
	public double getHeat()
	{
		return heat;
	}
	
	public short getWater()
	{
		return water;
	}
	
	public short getSteam()
	{
		return steam;
	}
	
	@Override
	public void reset()
	{
		heat = 20;
		if (getConfig().reasimBoilers)
		{
			water = 0;
			steam = 0;
		}
	}
	
	@Override
	public void addInformation(List<String> info)
	{
		super.addInformation(info);
		info.add("Heat: " + heat);
		if (getConfig().reasimBoilers)
		{
			info.add("Water: " + water);
			info.add("Steam: " + steam);
		}
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		sink
		.putDouble(heat)
		.putShort(steam)
		.putShort(water);
		GridLocation.FUNNEL.funnel(location, sink);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(heat, location, steam, water);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RBMKSimColumnBase))
			return false;
		final RBMKSimColumnBase other = (RBMKSimColumnBase) obj;
		return Double.doubleToLongBits(heat) == Double.doubleToLongBits(other.heat)
				&& Objects.equals(location, other.location) && steam == other.steam && water == other.water;
	}
	
	@Override
	public abstract String toString();
	
}
