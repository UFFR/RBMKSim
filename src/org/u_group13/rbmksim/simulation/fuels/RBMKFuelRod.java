package org.u_group13.rbmksim.simulation.fuels;

import static org.uffr.uffrlib.math.MathUtil.sigFigRounding;

import com.google.common.hash.PrimitiveSink;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.main.Main;
import org.u_group13.rbmksim.util.I18n;
import org.u_group13.rbmksim.util.InfoProviderNT;
import org.u_group13.rbmksim.util.TextBuilder;
import org.uffr.uffrlib.hashing.Hashable;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public sealed class RBMKFuelRod implements InfoProviderNT, Hashable, Serializable, Cloneable permits RBMKFuelDRX
{
	@Serial
	private static final long serialVersionUID = -9197159590599662941L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RBMKFuelRod.class);
	protected double xenon = 0, hullHeat = 20, coreHeat = 20, remainingYield;
	public final FuelType type;
	public RBMKFuelRod(FuelType type)
	{
		this.type = type;
		remainingYield = type.data.getYield();
	}

	public RBMKFuelRod(RBMKFuelRod other)
	{
		this.xenon = other.xenon;
		this.hullHeat = other.hullHeat;
		this.coreHeat = other.coreHeat;
		this.remainingYield = other.remainingYield;
		this.type = other.type;
	}

	public static double reactivityModByEnrichment(RBMKFuelData data, double enrichment)
	{
        return switch (data.depleteFunction())
        {
            default -> enrichment;
            case STATIC -> 1D;
            case BOOSTED_SLOPE ->
                    enrichment + Math.sin((enrichment - 1) * (enrichment - 1) * Math.PI); //x + sin([x - 1]^2 * pi) works
            case RAISING_SLOPE ->
                    enrichment + (Math.sin(enrichment * Math.PI) / 2D); //x + (sin(x * pi) / 2) actually works
            case GENTLE_SLOPE -> enrichment + (Math.sin(enrichment * Math.PI) / 3D); //x + (sin(x * pi) / 3) also works
        };
	}
	
	protected static String getFunctionDesc(RBMKFuelData data, double enrichment)
	{
		if (enrichment < 1)
			return String.format(data.burnFunction().format, data.selfRate() > 0 ? "(x" + data.selfRate() + ')' : 'x', sigFigRounding(data.reactivity() * enrichment, 4, 0)).concat(" (" + sigFigRounding(enrichment * 100, 4, 0) + "%)");
		
		return String.format(data.burnFunction().format, data.selfRate() > 0 ? "(x" + data.selfRate() + ')' : 'x', data.reactivity());
	}
	
	@Override
	public void addInformation(List<Text> info)
	{
		info.add(new Text(type.data.name()));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(type.data.fullName()));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.reactivity", type.data.reactivity())));
		info.add(InfoProviderNT.getNewline());
		if (type.data.selfRate() > 0 || type.data.burnFunction() == EnumBurnFunction.SIGMOID)
		{
			info.add(new TextBuilder(I18n.resolve("fuel.selfIniting")).setColor(Color.RED).getText());
			info.add(InfoProviderNT.getNewline());
		}
		info.add(new Text(I18n.resolve("fuel.depletion", sigFigRounding((remainingYield / type.data.getYield()) * 100, 5, 0))));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.fluxFunction", getFunctionDesc(type.data, getEnrichment()))));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.functionType", type.data.burnFunction().title)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.depletionFunction", type.data.depleteFunction())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.selfRate", type.data.selfRate())));
		info.add(InfoProviderNT.getNewline());
		info.add(new TextBuilder(I18n.resolve("fuel.xenonGen", type.data.xenonGen())).setColor(Color.PURPLE).getText());
		info.add(InfoProviderNT.getNewline());
		info.add(new TextBuilder(I18n.resolve("fuel.xenonBurn", type.data.xenonBurn())).setColor(Color.PURPLE).getText());
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.xenon", sigFigRounding(xenon, 4, 0))));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.heatGen", type.data.heatGen())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.meltingPoint", type.data.meltingPoint())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.diffusion", type.data.diffusion())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.neutronIn", type.data.receiveType())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.neutronOut", type.data.returnType())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.hullHeat", sigFigRounding(hullHeat, 2, 0))));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.coreHeat", sigFigRounding(coreHeat, 2, 0))));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.meltingPoint", type.data.meltingPoint())));
		info.add(InfoProviderNT.getNewline());
	}
	
	public double getEnrichment()
	{
		return remainingYield / type.data.getYield();
	}
	
	public double burn(double inbound)
	{
		double inboundCopy = inbound;
		inboundCopy += type.data.selfRate();
		inboundCopy *= 1d - (xenon / 100);
		
		double outbound = getReactivity(inboundCopy);
		
		xenon -= xenonBurn(inboundCopy);
		
		if (xenon < 0) xenon = 0;
		if (xenon > 100) xenon = 100;
		
		remainingYield -= inboundCopy;
		if (remainingYield < 0) remainingYield = 0;
		
		coreHeat += outbound * type.data.heatGen();
		
		return outbound;
	}
	
	public void updateHeat(double mod)
	{
		if (coreHeat > hullHeat)
		{
			final double mid = (coreHeat - hullHeat) / 2d;
			coreHeat -= mid * type.data.diffusion() * mod;
			hullHeat += mid * type.data.diffusion() * mod;
		}
	}
	
	public double provideHeat(double heat, double mod)
	{
		if (hullHeat > type.data.meltingPoint())
		{
			final double avg = (heat + coreHeat + hullHeat) / 3d;
			coreHeat = avg;
			hullHeat = avg;
			return avg;
		}
		
		if (hullHeat <= heat)
			return 0;
		
		final double ret = ((hullHeat - heat) / 2) * mod;
		
		hullHeat -= ret;
		return ret;
	}
	
	protected double getReactivity(double inFlux)
	{
		double flux = inFlux * reactivityModByEnrichment(type.data, getEnrichment());

        return switch (type.data.burnFunction())
        {
            case PASSIVE -> type.data.selfRate() + getEnrichment();
            case LOG_TEN -> Math.log10(flux + 1) * 0.5 * type.data.reactivity();
            case PLATEU -> (1 - Math.pow(Math.E, -flux / 25d)) * type.data.reactivity();
            case ARCH -> Math.max((flux - (flux * flux / 10000d)) / 100d * type.data.reactivity(), 0);
            case SIGMOID -> type.data.reactivity() / (1 + Math.pow(Math.E, -(flux - 50) / 10d));
            case SQUARE_ROOT -> Math.sqrt(flux) * type.data.reactivity() / 10d;
            case LINEAR -> flux / 100d * type.data.reactivity();
            case QUADRATIC -> flux * flux / 10000d * type.data.reactivity();
            case EXPERIMENTAL -> flux * (Math.sin(flux) + 1) * type.data.reactivity();
            default -> 0;
        };
	}
	
	protected double xenonBurn(double flux)
	{
		return (flux * flux) / type.data.xenonBurn();
	}
	
	public double getXenon()
	{
		return xenon;
	}

	public double getHullHeat()
	{
		return hullHeat;
	}

	public double getCoreHeat()
	{
		return coreHeat;
	}

	public double getRemainingYield()
	{
		return remainingYield;
	}

	public final void reset()
	{
		hullHeat = 20;
		coreHeat = 20;
		xenon = 0;
		remainingYield = type.data.getYield();
	}

	public final void setYield(double yield)
	{
		remainingYield = yield;
	}

	public final void resetYield()
	{
		remainingYield = type.data.getYield();
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		sink.putDouble(coreHeat).putDouble(hullHeat).putDouble(remainingYield).putDouble(xenon);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(coreHeat, type, hullHeat, remainingYield, xenon);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RBMKFuelRod other))
			return false;
        return Double.doubleToLongBits(coreHeat) == Double.doubleToLongBits(other.coreHeat)
				&& type == other.type
				&& Double.doubleToLongBits(hullHeat) == Double.doubleToLongBits(other.hullHeat)
				&& Double.doubleToLongBits(remainingYield) == Double.doubleToLongBits(other.remainingYield)
				&& Double.doubleToLongBits(xenon) == Double.doubleToLongBits(other.xenon);
	}

	@Override
	public String toString()
	{
		String builder = "RBMKFuelRod [xenon=" + xenon + ", hullHeat=" + hullHeat + ", coreHeat=" +
				coreHeat + ", remainingYield=" + remainingYield + ", type=" + type +
				']';
		return builder;
	}

	@Override
	public RBMKFuelRod clone()
	{
		try
		{
			return (RBMKFuelRod) super.clone();
		} catch (CloneNotSupportedException e)
		{
			LOGGER.error("Could not clone RBMKFuelRod, this should not be possible!", e);
			Main.openErrorDialog(e);
			return new RBMKFuelRod(this);
		}
	}
}
