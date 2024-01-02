package org.uffr.rbmksim.simulation.fuels;

import static org.uffr.uffrlib.math.MathUtil.sigFigRounding;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.uffr.rbmksim.util.I18n;
import org.uffr.rbmksim.util.InfoProviderNT;
import org.uffr.rbmksim.util.TextBuilder;
import org.uffr.uffrlib.hashing.Hashable;

import com.google.common.hash.HashCode;
import com.google.common.hash.PrimitiveSink;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class RBMKFuelRod implements InfoProviderNT, Hashable, Serializable, Cloneable
{
	@Serial
	private static final long serialVersionUID = -9197159590599662941L;
	public static final int defaultYield = 100_000_000;
	protected double xenon = 0, hullHeat = 20, coreHeat = 20, remainingYield;
	public transient final RBMKFuelData data;
	public RBMKFuelRod(RBMKFuelData data)
	{
		this.data = data;
		remainingYield = data.getYield();
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
		info.add(new Text(I18n.resolve(data.name())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve(data.fullName())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.reactivity", data.reactivity())));
		info.add(InfoProviderNT.getNewline());
		if (data.selfRate() > 0 || data.burnFunction() == EnumBurnFunction.SIGMOID)
		{
			info.add(new TextBuilder(I18n.resolve("fuel.selfIniting")).setStroke(Color.RED).getText());
			info.add(InfoProviderNT.getNewline());
		}
		info.add(new Text(I18n.resolve("fuel.depletion", sigFigRounding((remainingYield / data.getYield()) * 100, 5, 0))));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.fluxFunction", getFunctionDesc(data, getEnrichment()))));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.functionType", data.burnFunction().title)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.depletionFunction", data.depleteFunction())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.selfRate", data.selfRate())));
		info.add(InfoProviderNT.getNewline());
		info.add(new TextBuilder(I18n.resolve("fuel.xenonGen", data.xenonGen())).setStroke(Color.PURPLE).getText());
		info.add(InfoProviderNT.getNewline());
		info.add(new TextBuilder(I18n.resolve("fuel.xenonBurn", data.xenonBurn())).setStroke(Color.PURPLE).getText());
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.xenon", sigFigRounding(xenon, 4, 0))));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.heatGen", data.heatGen())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.meltingPoint", data.meltingPoint())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.diffusion", data.diffusion())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.neutronIn", data.receiveType())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.neutronOut", data.returnType())));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.hullHeat", sigFigRounding(hullHeat, 2, 0))));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.coreHeat", sigFigRounding(coreHeat, 2, 0))));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.meltingPoint", data.meltingPoint())));
		info.add(InfoProviderNT.getNewline());
	}
	
	public double getEnrichment()
	{
		return remainingYield / data.getYield();
	}
	
	public double burn(double inbound)
	{
		double inboundCopy = inbound;
		inboundCopy += data.selfRate();
		inboundCopy *= 1d - (xenon / 100);
		
		double outbound = getReactivity(inboundCopy);
		
		xenon -= xenonBurn(inboundCopy);
		
		if (xenon < 0) xenon = 0;
		if (xenon > 100) xenon = 100;
		
		remainingYield -= inboundCopy;
		if (remainingYield < 0) remainingYield = 0;
		
		coreHeat += outbound * data.heatGen();
		
		return outbound;
	}
	
	public void updateHeat(double mod)
	{
		if (coreHeat > hullHeat)
		{
			final double mid = (coreHeat - hullHeat) / 2d;
			coreHeat -= mid * data.diffusion() * mod;
			hullHeat += mid * data.diffusion() * mod;
		}
	}
	
	public double provideHeat(double heat, double mod)
	{
		if (hullHeat > data.meltingPoint())
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
		double flux = inFlux * reactivityModByEnrichment(data, getEnrichment());

        return switch (data.burnFunction())
        {
            case PASSIVE -> data.selfRate() + getEnrichment();
            case LOG_TEN -> Math.log10(flux + 1) * 0.5 * data.reactivity();
            case PLATEU -> (1 - Math.pow(Math.E, -flux / 25d)) * data.reactivity();
            case ARCH -> Math.max((flux - (flux * flux / 10000d)) / 100d * data.reactivity(), 0);
            case SIGMOID -> data.reactivity() / (1 + Math.pow(Math.E, -(flux - 50) / 10d));
            case SQUARE_ROOT -> Math.sqrt(flux) * data.reactivity() / 10d;
            case LINEAR -> flux / 100d * data.reactivity();
            case QUADRATIC -> flux * flux / 10000d * data.reactivity();
            case EXPERIMENTAL -> flux * (Math.sin(flux) + 1) * data.reactivity();
            default -> 0;
        };
	}
	
	protected double xenonBurn(double flux)
	{
		return (flux * flux) / data.xenonBurn();
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
		remainingYield = data.getYield();
	}
	
	public final void resetYield()
	{
		remainingYield = data.getYield();
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		sink.putDouble(coreHeat).putDouble(hullHeat).putDouble(remainingYield).putDouble(xenon);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(coreHeat, data, hullHeat, remainingYield, xenon);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RBMKFuelRod other))
			return false;
        return Double.doubleToLongBits(coreHeat) == Double.doubleToLongBits(other.coreHeat)
				&& Objects.equals(data, other.data)
				&& Double.doubleToLongBits(hullHeat) == Double.doubleToLongBits(other.hullHeat)
				&& Double.doubleToLongBits(remainingYield) == Double.doubleToLongBits(other.remainingYield)
				&& Double.doubleToLongBits(xenon) == Double.doubleToLongBits(other.xenon);
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("RBMKFuelRod [xenon=").append(xenon).append(", hullHeat=").append(hullHeat).append(", coreHeat=")
				.append(coreHeat).append(", remainingYield=").append(remainingYield).append(", data=").append(data)
				.append(']');
		return builder.toString();
	}
}
