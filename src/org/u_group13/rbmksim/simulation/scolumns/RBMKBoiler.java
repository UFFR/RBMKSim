package org.u_group13.rbmksim.simulation.scolumns;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.u_group13.rbmksim.main.dialog.ColumnDialogBase;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.FluidType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.util.I18n;
import org.u_group13.rbmksim.util.InfoProviderNT;
import org.u_group13.rbmksim.util.TextBuilder;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.u_group13.rbmksim.simulation.bcolumns.RBMKBlueprintBoiler;

@SuppressWarnings("UnstableApiUsage")
public class RBMKBoiler extends RBMKSimColumnBase
{
	@Serial
	private static final long serialVersionUID = -7777582161264166919L;
	protected final FluidTank waterTank = new FluidTank(FluidType.WATER, 10000);
	protected final FluidTank steamTank = new FluidTank(FluidType.STEAM, 1000000);
	public RBMKBoiler(GridLocation location)
	{
		super(location);
	}

	public RBMKBoiler(RBMKBlueprintBoiler boiler)
	{
		this(boiler.getLocation());
		waterTank.setFluidType(boiler.getSteamType());
	}
	
	public FluidType getSteamType()
	{
		return steamTank.getFluidType();
	}
	
	public int getSteamFill()
	{
		return steamTank.getFill();
	}
	
	@SuppressWarnings({"IntegerDivisionInFloatingPointContext" , "MathRoundingWithIntArgument"})
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
        return switch (type)
        {
            case STEAM              -> 1;
            case DENSE_STEAM        -> 10;
            case SUPER_DENSE_STEAM  -> 100;
            case ULTRA_DENSE_STEAM  -> 1000;
            default                 -> 0;
        };
	}
	
	@Override
	public void addInformation(List<Text> info)
	{
		super.addInformation(info);
		info.add(new TextBuilder(
				I18n.resolve("fluid.water") + " (" + waterTank.getFill() + '/' + waterTank.maxFill + "mB)").setColor(Color.YELLOW).getText());
		info.add(InfoProviderNT.getNewline());
		info.add(new TextBuilder(steamTank.getFluidType().toString() + " (" + steamTank.getFill() + '/' + steamTank.maxFill + "mB)").setColor(Color.YELLOW).getText());
		info.add(InfoProviderNT.getNewline());
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
		context.fillRect(location.x() + 1, location.y() + 1, 5, (int) (12 * ((float) waterTank.getFill() / (float) waterTank.maxFill)));
		context.setFill(Color.WHITE);
		context.fillRect(location.x() + 12, location.y() + 1, 5, (int) (12 * ((float) steamTank.getFill() / (float) steamTank.maxFill)));
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
		if (!(obj instanceof RBMKBoiler other))
			return false;
        return Objects.equals(steamTank, other.steamTank) && Objects.equals(waterTank, other.waterTank);
	}
	
	@SuppressWarnings("UnstableApiUsage")
	public static class FluidTank implements Serializable
	{
		@Serial
		private static final long serialVersionUID = 599853533652632531L;
		public static final Funnel<FluidTank> FUNNEL = (tank, sink) ->
				sink.putInt(tank.getFill()).putInt(tank.maxFill).putInt(tank.getFluidType().ordinal());
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
			if (!(obj instanceof FluidTank other))
				return false;
            return fill == other.fill && fluidType == other.fluidType && maxFill == other.maxFill;
		}
		@Override
		public String toString()
		{
            return "FluidTank [fill=" + fill + ", maxFill=" + maxFill + ", fluidType=" +
                    fluidType + ']';
		}

	}

	@Override
	public String toString()
	{
        return "RBMKBoiler [heat=" + heat + ", water=" + water + ", steam=" +
                steam + ", heatCache=" + Arrays.toString(heatCache) + ", location=" +
                location + ", waterTank=" + waterTank + ", steamTank=" +
                steamTank + ']';
	}
}
