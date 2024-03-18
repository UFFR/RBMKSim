package org.u_group13.rbmksim.simulation.scolumns;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.u_group13.rbmksim.main.dialog.ColumnDialogBase;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.Direction;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.simulation.fuels.FuelType;
import org.u_group13.rbmksim.simulation.fuels.NeutronType;
import org.u_group13.rbmksim.simulation.fuels.RBMKFuelRod;
import org.u_group13.rbmksim.util.I18n;
import org.u_group13.rbmksim.util.MiscUtil;
import org.u_group13.rbmksim.util.RBMKRenderHelper;
import org.u_group13.rbmksim.main.RBMKSimulation;
import org.u_group13.rbmksim.simulation.bcolumns.RBMKBlueprintFuel;

import com.google.common.hash.PrimitiveSink;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;

@SuppressWarnings("UnstableApiUsage")
public class RBMKFuel extends RBMKFluxReceiverBase
{
	@Serial
	private static final long serialVersionUID = -416153160444730096L;

	protected static NeutronType stream;
	private final boolean moderated;
	protected RBMKFuelRod fuelRod = null;
	protected double fluxFast, fluxSlow;
	public RBMKFuel(GridLocation location, boolean moderated)
	{
		super(location);
		this.moderated = moderated;
	}
	
	public RBMKFuel(GridLocation location, boolean moderated, FuelType type)
	{
		this(location, moderated);
		fuelRod = MiscUtil.convertOrNull(type, RBMKFuelRod::new);
	}

	public RBMKFuel(RBMKBlueprintFuel fuel)
	{
		this(fuel.getLocation(), fuel.isModerated(), fuel.getFuelType().orElse(null));
	}
	
	@Override
	public boolean isModerated()
	{
		return moderated;
	}

	public void setFuelRod(RBMKFuelRod fuelRod)
	{
		this.fuelRod = fuelRod;
	}
	
	public Optional<RBMKFuelRod> getFuelRod()
	{
		return Optional.ofNullable(fuelRod);
	}
	
	@Override
	public void tick()
	{
		if (fuelRod != null)
		{
			final double fluxIn = fluxFromType(fuelRod.type.data.receiveType());
			final double fluxOut = fuelRod.burn(fluxIn);
			fuelRod.updateHeat(1);
			heat += fuelRod.provideHeat(heat, 1);
			
			if (heat > maxHeat())
			{
				((RBMKSimulation) getCurrentFrame()).triggerMeltdown();
				return;
			}
			
			super.tick();
			
			fluxFast = 0;
			fluxSlow = 0;
			
			spreadFlux(fuelRod.type.data.returnType(), fluxOut);
		} else
		{
			fluxFast = 0;
			fluxSlow = 0;
			super.tick();
		}
	}
	
	@Override
	public void receiveFlux(NeutronType type, double flux)
	{
		// TODO "ANY" enum type
		switch (type)
		{
			case FAST: fluxFast += flux; break;
			case SLOW: fluxSlow += flux; break;
			default: break;
		}
	}
	
	private double fluxFromType(NeutronType type)
	{
        return switch (type)
        {
            case ANY -> fluxFast + fluxSlow;
            case FAST -> fluxFast + fluxSlow * 0.3;
            case SLOW -> fluxFast * 0.5 + fluxSlow;
            default -> 0;
        };
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
			if (columnBase instanceof RBMKFuel fuel)
			{
                if (fuel.getFuelRod().isPresent())
				{
					fuel.receiveFlux(stream, flux);
					return 0;
				}
			}
			if (columnBase instanceof RBMKFluxReceiverBase receiver)
			{
                receiver.receiveFlux(stream, flux);
				return 0;
			}
			if (columnBase instanceof RBMKControl control)
			{
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
		} else
			return 0;
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		sink.putDouble(fluxFast).putDouble(fluxSlow).putBoolean(moderated);
		if (fuelRod != null)
			fuelRod.funnelInto(sink);
	}

	@Override
	public ColumnType getColumnType()
	{
		return ColumnType.FUEL;
	}

	@Override
	public ColumnDialogBase<RBMKFuel> getMenu()
	{
		// TODO
		return null;
	}
	

	@Override
	public void render(GraphicsContext context)
	{
		RBMKRenderHelper.basicRender(getColumnType(), location, context, getCurrentFrame().getRenderer().zoom);
	}
	
	@Override
	public void addInformation(List<Text> info)
	{
		super.addInformation(info);
		if (fuelRod != null)
			fuelRod.addInformation(info);
		else
			info.add(new Text(I18n.resolve("column.type.fuel.empty")));
	}
	
	@Override
	public void reset()
	{
		super.reset();
		if (fuelRod != null)
			fuelRod.reset();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RBMKFuel other))
			return false;
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
        return "RBMKFuel [heat=" + heat + ", water=" + water + ", steam=" + steam +
                ", heatCache=" + Arrays.toString(heatCache) + ", location=" + location +
                ", moderated=" + moderated + ", fuelRod=" + fuelRod + ", fluxFast=" +
                fluxFast + ", fluxSlow=" + fluxSlow + ']';
	}

}
