package org.uffr.rbmksim.simulation.scolumns;

import java.util.Objects;

import com.google.common.hash.PrimitiveSink;
import org.uffr.rbmksim.main.RBMKSimulation;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class RBMKControl extends RBMKSimColumnBase
{
	public enum ControlRodColor
	{
		RED(Color.RED),
		YELLOW(Color.YELLOW),
		GREEN(Color.GREEN),
		BLUE(Color.BLUE),
		PURPLE(Color.PURPLE);
		public final Color renderColor;
		private ControlRodColor(Color renderColor)
		{
			this.renderColor = renderColor;
		}
	}

	private static final long serialVersionUID = -8157601179242556675L;
	public static final double SPEED = 0.00277;
	protected final boolean moderated;
	protected ControlRodColor rodColor;
	protected double lastLevel, level, targetLevel;
	public RBMKControl(GridLocation location, RBMKSimulation simulation, boolean moderated)
	{
		super(location, simulation);
		this.moderated = moderated;
	}

	@Override
	public void tick()
	{
		if (level > targetLevel)
		{
			level -= SPEED * rbmkFrame.getConfig().controlSpeedMod;
			if (level < targetLevel) level = targetLevel;
		}
		if (level < targetLevel)
		{
			level += SPEED * rbmkFrame.getConfig().controlSpeedMod;
			if (level > targetLevel) level = targetLevel;
		}
		super.tick();
	}
	
	@Override
	public void render(GraphicsContext graphics)
	{
		super.render(graphics);
		graphics.setFill(Color.BLACK);
		// TODO Check if renders properly
		final int offset = (int) Math.round(level * 8);
		graphics.fillRect(location.getX() + offset, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - offset, location.getY() + CELL_SIZE - LINE_WIDTH);
	}
	
	public double getTargetLevel()
	{
		return targetLevel;
	}
	
	public void setTargetLevel(double targetLevel)
	{
		this.targetLevel = targetLevel;
	}
	
	public double getLevel()
	{
		return level;
	}
	
	public void setLevel(double level)
	{
		this.level = level;
	}
	
	@Override
	public boolean isModerated()
	{
		return moderated;
	}

	@Override
	public ColumnType getColumnType()
	{
		return ColumnType.CONTROL;
	}

	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		sink.putInt(rodColor.ordinal()).putBoolean(moderated).putDouble(level).putDouble(lastLevel).putDouble(targetLevel);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(lastLevel, level, moderated, rodColor, targetLevel);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RBMKControl))
			return false;
		final RBMKControl other = (RBMKControl) obj;
		return Double.doubleToLongBits(lastLevel) == Double.doubleToLongBits(other.lastLevel)
				&& Double.doubleToLongBits(level) == Double.doubleToLongBits(other.level)
				&& moderated == other.moderated && rodColor == other.rodColor
				&& Double.doubleToLongBits(targetLevel) == Double.doubleToLongBits(other.targetLevel);
	}
}
