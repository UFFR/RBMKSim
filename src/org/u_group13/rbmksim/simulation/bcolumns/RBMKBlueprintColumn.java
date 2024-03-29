package org.u_group13.rbmksim.simulation.bcolumns;

import java.io.Serial;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.simulation.RBMKColumnBase;
import org.u_group13.rbmksim.simulation.scolumns.RBMKSimColumnBase;
import org.u_group13.rbmksim.util.RBMKRenderHelper;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

import javafx.scene.canvas.GraphicsContext;

import javax.annotation.Nonnull;

@SuppressWarnings("UnstableApiUsage")
public class RBMKBlueprintColumn extends RBMKColumnBase
{
	@Serial
	private static final long serialVersionUID = -1620510951364863121L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RBMKBlueprintColumn.class);
	public static final Funnel<RBMKBlueprintColumn> FUNNEL = (col, sink) ->
			sink.putInt(col.getColumnType().ordinal()).putInt(col.getLocation().x()).putInt(col.getLocation().y()).putBoolean(col.isModerated());
	
	private final ColumnType columnType;
	// I can't remember what this was for
	@Deprecated
	private final boolean moderated;
	public RBMKBlueprintColumn(@Nonnull RBMKSimColumnBase columnBase)
	{
		super(columnBase.getLocation());
		LOGGER.trace("Creating new RBMKBlueprintColumn from {}", columnBase);
		columnType = columnBase.getColumnType();
		moderated = columnBase.isModerated();
	}
	
	public RBMKBlueprintColumn(@Nonnull GridLocation location, @Nonnull ColumnType columnType, boolean moderated)
	{
		super(location);
		LOGGER.trace("Creating new RBMKBlueprintColumn at {}, of type {}, and moderated: {}", location, columnType, moderated);
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
		RBMKRenderHelper.basicRender(columnType, location, graphics, getCurrentFrame().getRenderer().zoom);
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
		if (!(obj instanceof RBMKBlueprintColumn other))
			return false;
        return columnType == other.columnType && Objects.equals(location, other.location)
				&& moderated == other.moderated;
	}

	@Override
	public String toString()
	{
		return "BlueprintColumn [columnType=" + columnType + ", moderated=" + moderated +
				']';
	}
}
