package org.uffr.rbmksim.simulation.bcolumns;

import com.google.common.hash.PrimitiveSink;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.FluidType;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.scolumns.RBMKBoiler;
import org.uffr.rbmksim.simulation.scolumns.RBMKBoiler.FluidTank;
import org.uffr.rbmksim.util.I18n;
import org.uffr.rbmksim.util.InfoProviderNT;
import org.uffr.rbmksim.util.TextBuilder;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class RBMKBlueprintBoiler extends RBMKBlueprintColumn
{
	@Serial
	private static final long serialVersionUID = -4867177641210935981L;
	private final FluidTank steamTank, waterTank;
	public RBMKBlueprintBoiler(RBMKBoiler boiler)
	{
		super(boiler);
		steamTank = new FluidTank(boiler.getSteamType(), 1000000);
		waterTank = new FluidTank(FluidType.WATER, 10000);
	}
	
	public RBMKBlueprintBoiler(GridLocation location, ColumnType columnType)
	{
		super(location, columnType, false);
		steamTank = new FluidTank(FluidType.STEAM, 1000000);
		waterTank = new FluidTank(FluidType.WATER, 10000);
	}

	@Override
	public void addInformation(List<Text> info)
	{
		super.addInformation(info);
		info.add(new TextBuilder(I18n.resolve("fluid.water") + " (" + waterTank.getFill() + '/' + waterTank.maxFill + "mB)").setColor(Color.YELLOW).getText());
		info.add(InfoProviderNT.getNewline());
		info.add(new TextBuilder(steamTank.getFluidType().toString() + " (" + steamTank.getFill() + '/' + steamTank.maxFill + "mB)").setColor(Color.YELLOW).getText());
		info.add(InfoProviderNT.getNewline());
	}

	@Override
	public void reset()
	{
		steamTank.setFill(0);
		steamTank.setFluidType(FluidType.STEAM);
		waterTank.setFill(0);
	}

	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		FluidTank.FUNNEL.funnel(steamTank, sink);
		FluidTank.FUNNEL.funnel(waterTank, sink);
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
		if (!(obj instanceof RBMKBlueprintBoiler other))
			return false;
        return Objects.equals(steamTank, other.steamTank) && Objects.equals(waterTank, other.waterTank);
	}

	@Override
	public String toString()
	{
		String builder = "BlueprintBoiler [steamTank=" + steamTank + ", waterTank=" + waterTank +
				", getLocation()=" + getLocation() + ", getColumnType()=" +
				getColumnType() + ", isModerated()=" + isModerated() + ']';
		return builder;
	}
	
}
