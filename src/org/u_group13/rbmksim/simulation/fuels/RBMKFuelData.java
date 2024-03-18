package org.u_group13.rbmksim.simulation.fuels;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.u_group13.rbmksim.util.I18n;
import org.u_group13.rbmksim.util.InfoProviderNT;
import org.u_group13.rbmksim.util.TextBuilder;

import java.util.List;
import java.util.Objects;

public final class RBMKFuelData implements InfoProviderNT
{
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
		return I18n.resolve(name);
	}

	public String fullName()
	{
		return I18n.resolve(fullName);
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
	public void addInformation(List<Text> info)
	{
		info.add(new Text(I18n.resolve(name)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve(fullName)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.reactivity", reactivity)));
		info.add(InfoProviderNT.getNewline());
		if (selfRate() > 0 || burnFunction() == EnumBurnFunction.SIGMOID)
		{
			info.add(new TextBuilder(I18n.resolve("fuel.selfIgniting")).setColor(Color.RED).getText());
			info.add(InfoProviderNT.getNewline());
		}
		info.add(new Text(I18n.resolve("fuel.yield", yield)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.fluxFunction", burnFunction)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.depletionFunction", depleteFunction)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.selfRate", selfRate)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.xenonGen", xenonGen)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.xenonBurn", xenonBurn)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.heatGen", heatGen)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.diffusion", diffusion)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.neutronIn", inType)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.neutronOut", outType)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.meltingPoint", meltingPoint)));
		info.add(InfoProviderNT.getNewline());
		info.add(new Text(I18n.resolve("fuel.category", fuelCategory)));
		info.add(InfoProviderNT.getNewline());
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
		if (!(obj instanceof RBMKFuelData other))
			return false;
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
		String builder = "RBMKFuelDataImpl [reactivity=" + reactivity + ", meltingPoint=" +
				meltingPoint + ", yield=" + yield + ", selfRate=" + selfRate +
				", xenonGen=" + xenonGen + ", xenonBurn=" + xenonBurn + ", heatGen=" +
				heatGen + ", diffusion=" + diffusion + ", name=" + name +
				", fullName=" + fullName + ", burnFunction=" + burnFunction +
				", depleteFunction=" + depleteFunction + ", inType=" + inType +
				", outType=" + outType + ", fuelCategory=" + fuelCategory + ']';
		return builder;
	}
}
