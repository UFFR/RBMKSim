package org.uffr.rbmksim.simulation.scolumns;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.uffr.rbmksim.main.RBMKSimulation;
import org.uffr.rbmksim.main.dialog.ColumnDialogBase;
import org.uffr.rbmksim.main.dialog.FuelDialog;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.Direction;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.fuels.NeutronType;
import org.uffr.rbmksim.simulation.fuels.RBMKFuelData;
import org.uffr.rbmksim.simulation.fuels.RBMKFuelRod;
import org.uffr.rbmksim.util.RBMKRenderHelper;

import com.google.common.hash.PrimitiveSink;

import javafx.scene.canvas.GraphicsContext;

public class RBMKFuel extends RBMKFluxReceiverBase
{
	private static final long serialVersionUID = -416153160444730096L;

	protected static NeutronType stream;
	private final boolean moderated;
	protected Optional<RBMKFuelRod> fuelRod = Optional.empty();
	protected double fluxFast, fluxSlow;
	public RBMKFuel(GridLocation location, RBMKSimulation simulation, boolean moderated)
	{
		super(location, simulation);
		this.moderated = moderated;
	}
	
	public RBMKFuel(GridLocation location, RBMKSimulation simulation, boolean moderated, Optional<RBMKFuelData> data)
	{
		this(location, simulation, moderated);
		fuelRod = Optional.ofNullable(data.isPresent() ? new RBMKFuelRod(data.get()) : null);
	}
	
	@Override
	public boolean isModerated()
	{
		return moderated;
	}

	public void setFuelRod(RBMKFuelRod fuelRod)
	{
		this.fuelRod = Optional.ofNullable(fuelRod);
	}
	
	public Optional<RBMKFuelRod> getFuelRod()
	{
		return fuelRod;
	}
	
	@Override
	public void tick()
	{
		if (fuelRod.isPresent())
		{
			final RBMKFuelRod rod = fuelRod.get();
			final double fluxIn = fluxFromType(rod.data.receiveType());
			final double fluxOut = rod.burn(fluxIn);
			rod.updateHeat(1);
			heat += rod.provideHeat(heat, 1);
			
			if (heat > maxHeat())
			{
				((RBMKSimulation) getCurrentFrame()).triggerMeltdown();
				return;
			}
			
			super.tick();
			
			fluxFast = 0;
			fluxSlow = 0;
			
			spreadFlux(rod.data.returnType(), fluxOut);
		}
		else
		{
			fluxFast = 0;
			fluxSlow = 0;
			super.tick();
		}
	}
	
	@Override
	public void receiveFlux(NeutronType type, double flux)
	{
		switch (type)
		{
			case FAST: fluxFast += flux; break;
			case SLOW: fluxSlow += flux; break;
			default: break;
		}
	}
	
	private double fluxFromType(NeutronType type)
	{
		switch (type)
		{
			case ANY: return fluxFast + fluxSlow;
			case FAST: return fluxFast + fluxSlow * 0.3;
			case SLOW: return fluxFast * 0.5 + fluxSlow;
			default: return 0;
		}
	}
	
	protected void spreadFlux(NeutronType type, double flux)
	{
		final int range = getConfig().fluxRange;
		
		for (Direction dir : Direction.values())
		{
			stream = type;
			double fluxOut = flux;
			
			for (int i = 0; i <= range; i++)
			{
				fluxOut = runInteraction(dir.getNeighbor(location), fluxOut);
				if (flux <= 0) break;
			}
		}
	}
	
	protected double runInteraction(GridLocation loc, double flux)
	{
		if (getCurrentFrame().validCoords(loc))
		{
			final RBMKSimColumnBase columnBase = (RBMKSimColumnBase) getCurrentFrame().getColumnAtCoords(loc);
			if (columnBase instanceof RBMKFuel)
			{
				final RBMKFuel fuel = (RBMKFuel) columnBase;
				if (fuel.getFuelRod().isPresent())
				{
					fuel.receiveFlux(stream, flux);
					return 0;
				}
			}
			if (columnBase instanceof RBMKFluxReceiverBase)
			{
				final RBMKFluxReceiverBase receiver = (RBMKFluxReceiverBase) columnBase;
				receiver.receiveFlux(stream, flux);
				return 0;
			}
			if (columnBase instanceof RBMKControl)
			{
				final RBMKControl control = (RBMKControl) columnBase;
				if (control.getLevel() == 0)
					return 0;
				return flux * control.getLevel();
			}
			if (columnBase instanceof RBMKModerator)
			{
				stream = NeutronType.SLOW;
				return flux;
			}
			if (columnBase instanceof RBMKReflector)
			{
				receiveFlux(isModerated() ? NeutronType.SLOW : stream, flux);
				return 0;
			}
			if (columnBase instanceof RBMKAbsorber)
				return 0;
			return flux;
		}
		else
			return 0;
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		sink.putDouble(fluxFast).putDouble(fluxSlow).putBoolean(moderated);
		fuelRod.ifPresent(f -> f.funnelInto(sink));
	}

	@Override
	public ColumnType getColumnType()
	{
		return ColumnType.FUEL;
	}

	@Override
	public ColumnDialogBase<RBMKFuel> getMenu()
	{
		return new FuelDialog(this);
	}
	

	@Override
	public void render(GraphicsContext context)
	{
		RBMKRenderHelper.genericRender(getColumnType(), location, context, getCurrentFrame().zoom);
	}
	
	@Override
	public void addInformation(List<String> info)
	{
		super.addInformation(info);
		if (fuelRod.isPresent())
			fuelRod.get().addInformation(info);
		else
			info.add("Has no fuel rod");
	}
	
	@Override
	public void reset()
	{
		super.reset();
		if (fuelRod.isPresent())
			fuelRod.get().reset();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RBMKFuel))
			return false;
		final RBMKFuel other = (RBMKFuel) obj;
		return Double.doubleToLongBits(fluxFast) == Double.doubleToLongBits(other.fluxFast)
				&& Double.doubleToLongBits(fluxSlow) == Double.doubleToLongBits(other.fluxSlow)
				&& Objects.equals(fuelRod, other.fuelRod) && moderated == other.moderated;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(fluxFast, fluxSlow, fuelRod, moderated);
		return result;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("RBMKFuel [heat=").append(heat).append(", water=").append(water).append(", steam=").append(steam)
				.append(", heatCache=").append(Arrays.toString(heatCache)).append(", location=").append(location)
				.append(", moderated=").append(moderated).append(", fuelRod=").append(fuelRod).append(", fluxFast=")
				.append(fluxFast).append(", fluxSlow=").append(fluxSlow).append(']');
		return builder.toString();
	}

}
