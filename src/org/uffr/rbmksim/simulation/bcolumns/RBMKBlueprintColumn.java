package org.uffr.rbmksim.simulation.bcolumns;

import java.util.Objects;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.uffr.rbmksim.main.RBMKFrame;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.RBMKColumnBase;
import org.uffr.rbmksim.simulation.scolumns.RBMKSimColumnBase;
import org.uffr.rbmksim.util.RBMKRenderHelper;

import javafx.scene.canvas.GraphicsContext;

public class RBMKBlueprintColumn extends RBMKColumnBase
{
	private static final long serialVersionUID = -1620510951364863121L;
	public static final Funnel<RBMKBlueprintColumn> FUNNEL = (col, sink) ->
	{
		sink.putInt(col.getColumnType().ordinal()).putInt(col.getLocation().getX()).putInt(col.getLocation().getY()).putBoolean(col.isModerated());
	};
	
	private final ColumnType columnType;
	private final boolean moderated;
	public RBMKBlueprintColumn(RBMKSimColumnBase columnBase)
	{
		super(columnBase.getLocation(), columnBase.getRbmkFrame());
		columnType = columnBase.getColumnType();
		moderated = columnBase.isModerated();
	}
	
	public RBMKBlueprintColumn(GridLocation location, RBMKFrame rbmkFrame, ColumnType columnType, boolean moderated)
	{
		super(location, rbmkFrame);
		this.columnType = columnType;
		this.moderated = moderated;
	}

	@Override
	public ColumnType getColumnType()
	{
		return columnType;
	}
	
	@Override
	public boolean isModerated()
	{
		return moderated;
	}
	
	@Override
	public void render(GraphicsContext graphics)
	{
		RBMKRenderHelper.genericRender(columnType, location, graphics);
	}
	
	@Override
	public void reset()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		super.funnelInto(sink);
		sink.putInt(columnType.ordinal()).putBoolean(moderated);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(columnType, location, moderated);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RBMKBlueprintColumn))
			return false;
		final RBMKBlueprintColumn other = (RBMKBlueprintColumn) obj;
		return columnType == other.columnType && Objects.equals(location, other.location)
				&& moderated == other.moderated;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("BlueprintColumn [columnType=").append(columnType).append(", moderated=").append(moderated)
				.append(']');
		return builder.toString();
	}
}
