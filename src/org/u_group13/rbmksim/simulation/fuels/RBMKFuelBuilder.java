package org.u_group13.rbmksim.simulation.fuels;

public class RBMKFuelBuilder
{
	public static final int DEFAULT_YIELD = 100_000_000;
	private final double reactivity, meltingPoint;
	private double yield = DEFAULT_YIELD, selfRate = 0, xenonGen = 0.5, xenonBurn = 50, heatGen = 1, diffusion = 0.2;
	private final String name, fullName;
	private EnumBurnFunction burnFunction = EnumBurnFunction.LOG_TEN;
	private EnumDepleteFunction depleteFunction = EnumDepleteFunction.GENTLE_SLOPE;
	private NeutronType inType = NeutronType.SLOW, outType = NeutronType.FAST;
	private FuelCategory fuelCategory = FuelCategory.REAL;
	public RBMKFuelBuilder(String name, double reactivity, double meltingPoint, double selfRate)
	{
		this.name = "fuel.type.name." + name;
		this.fullName = "fuel.type.desc." + name;
		this.reactivity = reactivity;
		this.meltingPoint = meltingPoint;
		this.selfRate = selfRate;
	}
	public RBMKFuelBuilder setYield(double yield)
	{
		this.yield = yield;
		return this;
	}
	public RBMKFuelBuilder setSelfRate(double selfRate)
	{
		this.selfRate = selfRate;
		return this;
	}
	public RBMKFuelBuilder setXenonGen(double xenonGen)
	{
		this.xenonGen = xenonGen;
		return this;
	}
	public RBMKFuelBuilder setXenonBurn(double xenonBurn)
	{
		this.xenonBurn = xenonBurn;
		return this;
	}
	public RBMKFuelBuilder setHeatGen(double heatGen)
	{
		this.heatGen = heatGen;
		return this;
	}
	public RBMKFuelBuilder setDiffusion(double diffusion)
	{
		this.diffusion = diffusion;
		return this;
	}
	public RBMKFuelBuilder setBurnFunction(EnumBurnFunction burnFunction)
	{
		this.burnFunction = burnFunction;
		return this;
	}
	public RBMKFuelBuilder setDepleteFunction(EnumDepleteFunction depleteFunction)
	{
		this.depleteFunction = depleteFunction;
		return this;
	}
	public RBMKFuelBuilder setInType(NeutronType inType)
	{
		this.inType = inType;
		return this;
	}
	public RBMKFuelBuilder setOutType(NeutronType outType)
	{
		this.outType = outType;
		return this;
	}
	public RBMKFuelBuilder setFuelCategory(FuelCategory fuelCategory)
	{
		this.fuelCategory = fuelCategory;
		return this;
	}

	public RBMKFuelData construct()
	{
		return new RBMKFuelData(reactivity, meltingPoint, yield, selfRate, xenonGen, xenonBurn, heatGen, diffusion, name, fullName, burnFunction, depleteFunction, inType, outType, fuelCategory);
	}
}
