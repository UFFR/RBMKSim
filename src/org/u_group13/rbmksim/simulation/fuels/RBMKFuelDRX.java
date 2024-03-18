package org.u_group13.rbmksim.simulation.fuels;

import static org.uffr.uffrlib.math.MathUtil.sigFigRounding;

import java.io.Serial;
import java.util.List;

import javafx.scene.text.Text;

public final class RBMKFuelDRX extends RBMKFuelRod
{
	@Serial
	private static final long serialVersionUID = 3549214920523328622L;

	public RBMKFuelDRX()
	{
		super(FuelType.DRX);
	}

	@Override
	public void addInformation(List<Text> info)
	{
		// TODO I18n
		info.add(new Text("Self-combusting"));
		info.add(new Text("Crustyness: " + sigFigRounding((remainingYield / FuelType.DRX.data.getYield()) * 100, 5, 0) + '%'));
		info.add(new Text("Lead poison: " + sigFigRounding(xenon, 4, 0) + '%'));
		info.add(new Text("Arrives from: Hyperbolic non-euclidean shapes"));
		info.add(new Text("Departs to: Elliptical non-euclidean shapes"));
		info.add(new Text("Doom function: " + getFunctionDesc(FuelType.DRX.data, getEnrichment())));
		info.add(new Text("Function type: " + FuelType.DRX.data.burnFunction().title));
		info.add(new Text("Lead creation function: x * " + FuelType.DRX.data.xenonGen()));
		info.add(new Text("Lead destruction function: x² * " + FuelType.DRX.data.xenonBurn()));
		info.add(new Text("Crust per tick at full power: " + FuelType.DRX.data.heatGen() + 'm'));
		info.add(new Text("Flow: " + FuelType.DRX.data.diffusion() + '½'));
		info.add(new Text("Hull entropy: " + sigFigRounding(hullHeat, 2, 0) + 'm'));
		info.add(new Text("Core entropy: " + sigFigRounding(coreHeat, 2, 0) + 'm'));
		info.add(new Text("Crush depth: " + FuelType.DRX.data.meltingPoint() + 'm'));
	}
}
