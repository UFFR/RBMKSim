package org.u_group13.rbmksim.simulation.scolumns;

import com.google.common.hash.PrimitiveSink;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.util.I18n;
import org.u_group13.rbmksim.util.RBMKRenderHelper;
import org.u_group13.rbmksim.util.TextBuilder;
import org.u_group13.rbmksim.main.Main;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.io.Serial;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
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
		public final String uloc;
		ControlRodColor(Color renderColor)
		{
			this.renderColor = renderColor;
			uloc = "color." + name().toLowerCase();
		}

		@Override
		public String toString()
		{
			return I18n.resolve(uloc);
		}
	}

	@Serial
	private static final long serialVersionUID = -8157601179242556675L;
	public static final double SPEED = 0.00277;
	protected final boolean moderated;
	@Nullable
	protected ControlRodColor rodColor;

	protected double lastLevel, level, targetLevel;
	public RBMKControl(GridLocation location, boolean moderated)
	{
		super(location);
		this.moderated = moderated;
	}

	@Override
	public void tick()
	{
		if (level > targetLevel)
		{
			level -= SPEED * getCurrentFrame().getConfig().controlSpeedMod;
			if (level < targetLevel) level = targetLevel;
		}
		if (level < targetLevel)
		{
			level += SPEED * getCurrentFrame().getConfig().controlSpeedMod;
			if (level > targetLevel) level = targetLevel;
		}
		super.tick();
	}

	@Override
	public void addInformation(List<Text> info)
	{
		super.addInformation(info);
		if (rodColor != null)
		{
			info.add(new TextBuilder(I18n.resolve("column.type.control.color")).setColor(Color.YELLOW).getText());
			info.add(new TextBuilder(rodColor.toString()).setColor(rodColor.renderColor).getText());
			info.add(NEW_LINE_SUPPLIER.get());
		}
		info.add(new TextBuilder(I18n.resolve("column.type.control.level", NumberFormat.getPercentInstance(Main.config.locale).format(level))).setColor(Color.YELLOW).getText());
		info.add(NEW_LINE_SUPPLIER.get());
	}

	@Override
	public void render(GraphicsContext graphics)
	{
		super.render(graphics);
		graphics.setFill(Color.BLACK);
		// TODO Check if renders properly
		final int offset = (int) Math.round(level * 8);
		graphics.fillRect(location.x() + offset, location.y() + RBMKRenderHelper.LINE_WIDTH, location.x() + RBMKRenderHelper.CELL_SIZE - offset, location.y() + RBMKRenderHelper.CELL_SIZE - RBMKRenderHelper.LINE_WIDTH);
	}

	@CheckForNull
	public ControlRodColor getRodColor()
	{
		return rodColor;
	}

	public void setRodColor(@Nullable ControlRodColor rodColor)
	{
		this.rodColor = rodColor;
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
		sink.putInt(rodColor == null ? -1 : rodColor.ordinal()).putBoolean(moderated).putDouble(level).putDouble(lastLevel).putDouble(targetLevel);
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
		if (!(obj instanceof RBMKControl other))
			return false;
        return Double.doubleToLongBits(lastLevel) == Double.doubleToLongBits(other.lastLevel)
				&& Double.doubleToLongBits(level) == Double.doubleToLongBits(other.level)
				&& moderated == other.moderated && rodColor == other.rodColor
				&& Double.doubleToLongBits(targetLevel) == Double.doubleToLongBits(other.targetLevel);
	}
}
