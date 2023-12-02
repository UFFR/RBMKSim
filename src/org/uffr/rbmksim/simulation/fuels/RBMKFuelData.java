package org.uffr.rbmksim.simulation.fuels;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.uffr.rbmksim.util.InfoProvider;
import org.uffr.uffrlib.hashing.Hashable;

import com.google.common.hash.PrimitiveSink;

public class RBMKFuelData implements InfoProvider, Hashable, Serializable
{
	private static final long serialVersionUID = -8545270346483383342L;
	private final double reactivity, meltingPoint;
	private final double yield, selfRate, xenonGen, xenonBurn, heatGen, diffusion;
	private final String name, fullName;
	private final EnumBurnFunction burnFunction;
	private final EnumDepleteFunction depleteFunction;
	private final NeutronType inType, outType;
	private final FuelCategory fuelCategory;
	
	public RBMKFuelData(
			double reactivity, double meltingPoint, double yield, double selfRate, double xenonGen, double xenonBurn,
			double heatGen, double diffusion, String name, String fullName, EnumBurnFunction burnFunction,
			EnumDepleteFunction depleteFunction, NeutronType inType, NeutronType outType, FuelCategory fuelCategory
	)
	{
		this.reactivity = reactivity;
		this.meltingPoint = meltingPoint;
		this.yield = yield;
		this.selfRate = selfRate;
		this.xenonGen = xenonGen;
		this.xenonBurn = xenonBurn;
		this.heatGen = heatGen;
		this.diffusion = diffusion;
		this.name = name;
		this.fullName = fullName;
		this.burnFunction = burnFunction;
		this.depleteFunction = depleteFunction;
		this.inType = inType;
		this.outType = outType;
		this.fuelCategory = fuelCategory;
	}

	public String name()
	{
		return name;
	}

	public String fullName()
	{
		return fullName;
	}

	public double reactivity()
	{
		return reactivity;
	}

	public double getYield()
	{
		return yield;
	}

	public EnumBurnFunction burnFunction()
	{
		return burnFunction;
	}

	public EnumDepleteFunction depleteFunction()
	{
		return depleteFunction;
	}

	public double selfRate()
	{
		return selfRate;
	}

	public double xenonGen()
	{
		return xenonGen;
	}

	public double xenonBurn()
	{
		return xenonBurn;
	}

	public double heatGen()
	{
		return heatGen;
	}

	public double meltingPoint()
	{
		return meltingPoint;
	}

	public double diffusion()
	{
		return diffusion;
	}

	public NeutronType receiveType()
	{
		return inType;
	}

	public NeutronType returnType()
	{
		return outType;
	}

	public FuelCategory fuelCategory()
	{
		return fuelCategory;
	}
	
	@Override
	public void addInformation(List<String> info)
	{
		info.add(name());
		info.add(fullName());
		info.add("Reactivity: " + reactivity());
		if (selfRate() > 0 || burnFunction() == EnumBurnFunction.SIGMOID)
			info.add("Self-igniting");
		info.add("Yield:" + getYield());
		info.add("Flux function: " + burnFunction());
		info.add("Depletion function: " + depleteFunction());
		info.add("Self-rate: " + selfRate());
		info.add("Xenon generation: " + xenonGen());
		info.add("Xenon burn: " + xenonBurn());
		info.add("Heat generation: " + heatGen());
		info.add("Melting point: " + meltingPoint());
		info.add("Diffusion: " + diffusion());
		info.add("Neutron receive type: " + receiveType());
		info.add("Neutron return type: " + returnType());
		info.add("Category: " + fuelCategory());
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		sink
		.putInt(burnFunction.ordinal())
		.putInt(depleteFunction.ordinal())
		.putDouble(diffusion)
		.putInt(fuelCategory.ordinal())
		.putString(fullName, UTF_8)
		.putDouble(heatGen)
		.putInt(inType.ordinal())
		.putDouble(meltingPoint)
		.putString(name, UTF_8)
		.putInt(outType.ordinal())
		.putDouble(reactivity)
		.putDouble(selfRate)
		.putDouble(xenonBurn)
		.putDouble(xenonGen)
		.putDouble(yield);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(burnFunction, depleteFunction, diffusion, fuelCategory, fullName, heatGen, inType,
				meltingPoint, name, outType, reactivity, selfRate, xenonBurn, xenonGen, yield);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RBMKFuelData))
			return false;
		final RBMKFuelData other = (RBMKFuelData) obj;
		return burnFunction == other.burnFunction && depleteFunction == other.depleteFunction
				&& Double.doubleToLongBits(diffusion) == Double.doubleToLongBits(other.diffusion)
				&& fuelCategory == other.fuelCategory && Objects.equals(fullName, other.fullName)
				&& Double.doubleToLongBits(heatGen) == Double.doubleToLongBits(other.heatGen) && inType == other.inType
				&& Double.doubleToLongBits(meltingPoint) == Double.doubleToLongBits(other.meltingPoint)
				&& Objects.equals(name, other.name) && outType == other.outType
				&& Double.doubleToLongBits(reactivity) == Double.doubleToLongBits(other.reactivity)
				&& Double.doubleToLongBits(selfRate) == Double.doubleToLongBits(other.selfRate)
				&& Double.doubleToLongBits(xenonBurn) == Double.doubleToLongBits(other.xenonBurn)
				&& Double.doubleToLongBits(xenonGen) == Double.doubleToLongBits(other.xenonGen)
				&& Double.doubleToLongBits(yield) == Double.doubleToLongBits(other.yield);
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("RBMKFuelDataImpl [reactivity=").append(reactivity).append(", meltingPoint=")
				.append(meltingPoint).append(", yield=").append(yield).append(", selfRate=").append(selfRate)
				.append(", xenonGen=").append(xenonGen).append(", xenonBurn=").append(xenonBurn).append(", heatGen=")
				.append(heatGen).append(", diffusion=").append(diffusion).append(", name=").append(name)
				.append(", fullName=").append(fullName).append(", burnFunction=").append(burnFunction)
				.append(", depleteFunction=").append(depleteFunction).append(", inType=").append(inType)
				.append(", outType=").append(outType).append(", fuelCategory=").append(fuelCategory).append(']');
		return builder.toString();
	}
}
