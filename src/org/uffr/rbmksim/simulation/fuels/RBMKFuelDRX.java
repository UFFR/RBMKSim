package org.uffr.rbmksim.simulation.fuels;

import static org.uffr.uffrlib.math.MathUtil.sigFigRounding;

import java.util.List;

public class RBMKFuelDRX extends RBMKFuelRod
{
	private static final long serialVersionUID = 3549214920523328622L;

	public RBMKFuelDRX()
	{
		super(FuelRegistry.DRX_FUEL);
	}

	@Override
	public void addInformation(List<String> info)
	{
		info.add("Self-combusting");
		info.add("Crustyness: " + sigFigRounding((remainingYield / data.getYield()) * 100, 5, 0) + '%');
		info.add("Lead poison: " + sigFigRounding(xenon, 4, 0) + '%');
		info.add("Arrives from: Hyperbolic non-euclidean shapes");
		info.add("Departs to: Elliptical non-euclidean shapes");
		info.add("Doom function: " + getFunctionDesc(data, getEnrichment()));
		info.add("Function type: " + data.burnFunction().title);
		info.add("Lead creation function: x * " + data.xenonGen());
		info.add("Lead destruction function: x² * " + data.xenonBurn());
		info.add("Crust per tick at full power: " + data.heatGen() + 'm');
		info.add("Flow: " + data.diffusion() + '½');
		info.add("Hull entropy: " + sigFigRounding(hullHeat, 2, 0) + 'm');
		info.add("Core entropy: " + sigFigRounding(coreHeat, 2, 0) + 'm');
		info.add("Crush depth: " + data.meltingPoint() + 'm');
	}
}
