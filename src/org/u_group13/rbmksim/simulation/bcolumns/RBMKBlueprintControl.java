package org.u_group13.rbmksim.simulation.bcolumns;

import com.google.common.hash.PrimitiveSink;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.simulation.scolumns.RBMKControl;
import org.u_group13.rbmksim.util.I18n;
import org.u_group13.rbmksim.util.MiscUtil;
import org.u_group13.rbmksim.util.TextBuilder;
import org.u_group13.rbmksim.main.Main;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class RBMKBlueprintControl extends RBMKBlueprintColumn
{
	@Nullable
	protected RBMKControl.ControlRodColor rodColor;
	protected double level = 0;
	public RBMKBlueprintControl(RBMKControl columnBase)
	{
		super(columnBase);
		this.rodColor = columnBase.getRodColor();
	}

	public RBMKBlueprintControl(GridLocation location,
	                            ColumnType columnType, boolean moderated)
	{
		super(location, columnType, moderated);
	}

	@Override
	public void reset()
	{
		super.reset();
		rodColor = null;
		level = 0;
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

	@CheckForNull
	public RBMKControl.ControlRodColor getRodColor()
	{
		return rodColor;
	}

	public void setRodColor(@Nullable RBMKControl.ControlRodColor rodColor)
	{
		this.rodColor = rodColor;
	}

	public double getLevel()
	{
		return level;
	}

	public void setLevel(double level)
	{
		this.level = MiscUtil.clampDouble(level, 0, 1);
	}

	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		sink.putInt(rodColor == null ? -1 : rodColor.ordinal()).putDouble(level);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		final RBMKBlueprintControl that = (RBMKBlueprintControl) o;
		return Double.compare(level, that.level) == 0 && rodColor == that.rodColor;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), rodColor, level);
	}

	@Override
	public String toString()
	{
		return "RBMKBlueprintControl [" + "rodColor=" + rodColor +
				", level=" + level +
				", location=" + location +
				", shouldRender=" + shouldRender +
				']';
	}
}
