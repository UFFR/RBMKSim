package org.uffr.rbmksim.simulation.fuels;

import static org.uffr.uffrlib.math.MathUtil.sigFigRounding;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.uffr.rbmksim.util.InfoProvider;
import org.uffr.uffrlib.hashing.Hashable;

import com.google.common.hash.HashCode;
import com.google.common.hash.PrimitiveSink;

public class RBMKFuelRod implements InfoProvider, Hashable, Serializable, Cloneable
{
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
		switch (data.depleteFunction())
		{
			default:
			case LINEAR: return enrichment;
			case STATIC: return 1D;
			case BOOSTED_SLOPE: return enrichment + Math.sin((enrichment - 1) * (enrichment - 1) * Math.PI); //x + sin([x - 1]^2 * pi) works
			case RAISING_SLOPE: return enrichment + (Math.sin(enrichment * Math.PI) / 2D); //x + (sin(x * pi) / 2) actually works
			case GENTLE_SLOPE: return enrichment + (Math.sin(enrichment * Math.PI) / 3D); //x + (sin(x * pi) / 3) also works
		}
	}
	
	protected static String getFunctionDesc(RBMKFuelData data, double enrichment)
	{
		if (enrichment < 1)
			return String.format(data.burnFunction().format, data.selfRate() > 0 ? "(x" + data.selfRate() + ')' : 'x', sigFigRounding(data.reactivity() * enrichment, 4, 0)).concat(" (" + sigFigRounding(enrichment * 100, 4, 0) + "%)");
		
		return String.format(data.burnFunction().format, data.selfRate() > 0 ? "(x" + data.selfRate() + ')' : 'x', data.reactivity());
	}
	
	@Override
	public void addInformation(List<String> info)
	{
		info.add(data.name());
		info.add(data.fullName());
		if (data.selfRate() > 0 || data.burnFunction() == EnumBurnFunction.SIGMOID)
			info.add("Self-igniting");
		
		info.add("Depletion: " + sigFigRounding((remainingYield / data.getYield()) * 100, 5, 0) + '%');
		info.add("Xenon poison: " + sigFigRounding(xenon, 4, 0) + '%');
		info.add("Splits with: " + data.receiveType().desc);
		info.add("Splits into: " + data.returnType().desc);
		info.add("Flux function: " + getFunctionDesc(data, getEnrichment()));
		info.add("Function type: " + data.burnFunction().title);
		info.add("Xenon gen function: x * " + data.xenonGen());
		info.add("Xenon burn function: x² * " + data.xenonBurn());
		info.add("Heat per tick at full power: " + data.heatGen() + "°C");
		info.add("Diffusion: " + data.diffusion() + '½');
		info.add("Hull heat: " + sigFigRounding(hullHeat, 2, 0) + "°C");
		info.add("Core heat: " + sigFigRounding(coreHeat, 2, 0) + "°C");
		info.add("Melting point: " + data.meltingPoint() + "°C");
	}
	
	@Override
	public HashCode calculateHashCode()
	{
		return provideHashFunction()
				.putDouble(coreHeat)
				.putDouble(hullHeat)
				.putDouble(remainingYield)
				.putDouble(xenon)
				.hash();
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
		
		switch (data.burnFunction())
		{
			case PASSIVE: return data.selfRate() + getEnrichment();
			case LOG_TEN: return Math.log10(flux + 1) * 0.5 * data.reactivity();
			case PLATEU: return (1 - Math.pow(Math.E, -flux / 25d)) * data.reactivity();
			case ARCH: return Math.max((flux - (flux * flux / 10000d)) / 100d * data.reactivity(), 0);
			case SIGMOID: return data.reactivity() / (1 + Math.pow(Math.E, -(flux - 50) / 10d));
			case SQUARE_ROOT: return Math.sqrt(flux) * data.reactivity() / 10d;
			case LINEAR: return flux / 100d * data.reactivity();
			case QUADRATIC: return flux * flux / 10000d * data.reactivity();
			case EXPERIMENTAL: return flux * (Math.sin(flux) + 1) * data.reactivity();
			default: return 0;
		}
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
		if (!(obj instanceof RBMKFuelRod))
			return false;
		final RBMKFuelRod other = (RBMKFuelRod) obj;
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
