package org.uffr.rbmksim.simulation;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.uffr.rbmksim.config.SimulationConfig;
import org.uffr.rbmksim.main.RBMKFrame;
import org.uffr.rbmksim.util.InfoProvider;
import org.uffr.uffrlib.hashing.Hashable;

import com.google.common.hash.PrimitiveSink;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class RBMKColumnBase implements InfoProvider, Hashable, Serializable
{
	private static final long serialVersionUID = -4281135799096443502L;
	public static final short MAX_WATER = 16000, MAX_STEAM = 16000, MAX_HEAT_DEFAULT = 1500;
	// Render size for cells.
	public static final byte CELL_SIZE = 20, LINE_WIDTH = 4;
	
	protected transient RBMKFrame rbmkFrame;
	protected final GridLocation location;
	protected boolean shouldRender = true;
	public RBMKColumnBase(GridLocation location, RBMKFrame frame)
	{
		this.location = location;
		this.rbmkFrame = frame;
	}
	
	/**
	 * Copy constructor but with a different location, for moving columns. Recommended to properly implement.
	 * @param column Old column to copy.
	 * @param location (Presumably) new location this column should reside.
	 */
	public RBMKColumnBase(RBMKColumnBase column, GridLocation location)
	{
		this(location, column.rbmkFrame);
	}

	public abstract ColumnType getColumnType();
	public abstract boolean isModerated();
	public abstract void reset();
	public abstract void render(GraphicsContext graphics);
	
	public void setRbmkFrame(RBMKFrame rbmkFrame)
	{
		this.rbmkFrame = rbmkFrame;
	}
	
	public RBMKFrame getRbmkFrame()
	{
		return rbmkFrame;
	}
	
	protected final SimulationConfig getConfig()
	{
		return rbmkFrame.getConfig();
	}
	
	public final GridLocation getLocation()
	{
		return location;
	}
	
	public boolean shouldRender()
	{
		return shouldRender;
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		GridLocation.FUNNEL.funnel(location, sink);
	}

	@Override
	public void addInformation(List<String> info)
	{
		info.add("Column Info:");
		info.add("Type: " + getColumnType());
		info.add("Moderated: " + isModerated());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(location, shouldRender);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RBMKColumnBase))
			return false;
		final RBMKColumnBase other = (RBMKColumnBase) obj;
		return Objects.equals(location, other.location) && shouldRender == other.shouldRender;
	}
	
	@Override
	public abstract String toString();

	public static void renderBackground(GridLocation location, GraphicsContext graphics)
	{
		graphics.setFill(Color.GRAY);
		graphics.fillRect(location.getX(), location.getY(), location.getX() + CELL_SIZE, location.getY() + CELL_SIZE);
	}
	
	public static void renderEdges(GridLocation location, GraphicsContext graphics)
	{
		graphics.setLineWidth(4);
		graphics.rect(location.getX(), location.getY(), location.getX() + CELL_SIZE, location.getY() + CELL_SIZE);
	}
}
