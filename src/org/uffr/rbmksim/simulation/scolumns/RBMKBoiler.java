package org.uffr.rbmksim.simulation.scolumns;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.uffr.rbmksim.main.RBMKSimulation;
import org.uffr.rbmksim.main.dialog.ColumnDialogBase;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.FluidType;
import org.uffr.rbmksim.simulation.GridLocation;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class RBMKBoiler extends RBMKSimColumnBase
{
	private static final long serialVersionUID = -7777582161264166919L;
	protected final FluidTank waterTank = new FluidTank(FluidType.WATER, 10000);
	protected final FluidTank steamTank = new FluidTank(FluidType.STEAM, 1000000);
	public RBMKBoiler(GridLocation location, RBMKSimulation simulation)
	{
		super(location, simulation);
	}
	
	public FluidType getSteamType()
	{
		return steamTank.getFluidType();
	}
	
	public int getSteamFill()
	{
		return steamTank.getFill();
	}
	
	@Override
	public void tick()
	{
		final double heatProvided = heat - steamTank.getFluidType().temperature;
		if (heatProvided > 0)
		{
			final int waterUsed = Math.min((int) Math.floor(heatProvided / getCurrentFrame().getConfig().boilerHeatConsumption), steamTank.getFill());
			waterTank.decrementFill(waterUsed);
			final int steamProduced = (int) Math.floor(waterUsed * 100 / getFactorFromSteam(steamTank.getFluidType()));
			steamTank.incrementFill(steamProduced);
			
			heat -= waterUsed * getCurrentFrame().getConfig().boilerHeatConsumption;
		}
		super.tick();
	}

	private static short getFactorFromSteam(FluidType type)
	{
		switch (type)
		{
			case STEAM: return 1;
			case DENSE_STEAM: return 10;
			case SUPER_DENSE_STEAM: return 100;
			case ULTRA_DENSE_STEAM: return 1000;
			default: return 0;
		}
	}
	
	@Override
	public void addInformation(List<Text> info)
	{
		super.addInformation(info);
		info.add(new Text("Water: " + waterTank.getFill() + '/' + waterTank.maxFill + "mB"));
		info.add(new Text(steamTank.getFluidType().name + ": " + steamTank.getFill() + '/' + steamTank.maxFill + "mB"));
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		FluidTank.FUNNEL.funnel(waterTank, sink);
		FluidTank.FUNNEL.funnel(steamTank, sink);
	}
	
	@Override
	public ColumnType getColumnType()
	{
		return ColumnType.BOILER;
	}

	@Override
	public ColumnDialogBase<RBMKBoiler> getMenu()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void render(GraphicsContext context)
	{
		super.render(context);
		// TODO Confirm if renders properly
		context.setFill(Color.BLUE);
		context.fillRect(location.getX() + 1, location.getY() + 1, 5, (int) (12 * ((float) waterTank.getFill() / (float) waterTank.maxFill)));
		context.setFill(Color.WHITE);
		context.fillRect(location.getX() + 12, location.getY() + 1, 5, (int) (12 * ((float) steamTank.getFill() / (float) steamTank.maxFill)));
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(steamTank, waterTank);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RBMKBoiler))
			return false;
		final RBMKBoiler other = (RBMKBoiler) obj;
		return Objects.equals(steamTank, other.steamTank) && Objects.equals(waterTank, other.waterTank);
	}
	
	public static class FluidTank implements Serializable
	{
		private static final long serialVersionUID = 599853533652632531L;
		public static final Funnel<FluidTank> FUNNEL = (tank, sink) ->
		{
			sink.putInt(tank.getFill()).putInt(tank.maxFill).putInt(tank.getFluidType().ordinal());
		};
		private int fill;
		public final int maxFill;
		protected FluidType fluidType;
		public FluidTank(FluidType fluidType, int maxFill)
		{
			this.fluidType = fluidType;
			this.maxFill = maxFill;
		}
		public FluidType getFluidType()
		{
			return fluidType;
		}
		public void setFluidType(FluidType fluidType)
		{
			this.fluidType = fluidType;
			fill = 0;
		}
		public int getFill()
		{
			return fill;
		}
		public void setFill(int fill)
		{
			this.fill = fill;
			checkAndClamp();
		}
		public void incrementFill(int inc)
		{
			fill += inc;
			checkAndClamp();
		}
		public void decrementFill(int dec)
		{
			fill -= dec;
			checkAndClamp();
		}
		private void checkAndClamp()
		{
			if (fill < 0) fill = 0;
			if (fill > maxFill) fill = maxFill;
		}
		@Override
		public int hashCode()
		{
			return Objects.hash(fill, fluidType, maxFill);
		}
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!(obj instanceof FluidTank))
				return false;
			final FluidTank other = (FluidTank) obj;
			return fill == other.fill && fluidType == other.fluidType && maxFill == other.maxFill;
		}
		@Override
		public String toString()
		{
			final StringBuilder builder = new StringBuilder();
			builder.append("FluidTank [fill=").append(fill).append(", maxFill=").append(maxFill).append(", fluidType=")
					.append(fluidType).append(']');
			return builder.toString();
		}

	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("RBMKBoiler [heat=").append(heat).append(", water=").append(water).append(", steam=")
				.append(steam).append(", heatCache=").append(Arrays.toString(heatCache)).append(", location=")
				.append(location).append(", waterTank=").append(waterTank).append(", steamTank=")
				.append(steamTank).append(']');
		return builder.toString();
	}
}
