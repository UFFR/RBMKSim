package org.uffr.rbmksim.simulation;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.config.SimulationConfig;
import org.uffr.rbmksim.main.Main;
import org.uffr.rbmksim.main.RBMKFrame;
import org.uffr.rbmksim.util.InfoProvider;
import org.uffr.uffrlib.hashing.Hashable;

import com.google.common.hash.PrimitiveSink;

import javafx.scene.canvas.GraphicsContext;

public abstract class RBMKColumnBase implements InfoProvider, Hashable, Serializable
{
	private static final long serialVersionUID = -4281135799096443502L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RBMKColumnBase.class);
	private static RBMKFrame currentFrame;
	public static final short MAX_WATER = 16000, MAX_STEAM = 16000, MAX_HEAT_DEFAULT = 1500;
	
	@Deprecated
	protected transient RBMKFrame rbmkFrame;
	protected final GridLocation location;
	protected boolean shouldRender = true;
	public RBMKColumnBase(GridLocation location, RBMKFrame frame)
	{
		LOGGER.trace("New RBMKColumnBase constructed");
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
		LOGGER.trace("RBMKColumnBase copy constructor called");
	}
	
	public static void setCurrentFrame(RBMKFrame currentFrame)
	{
		RBMKColumnBase.currentFrame = currentFrame;
	}

	public abstract ColumnType getColumnType();
	public abstract boolean isModerated();
	public abstract void reset();
	public abstract void render(GraphicsContext graphics);
	
	public void setRbmkFrame(RBMKFrame rbmkFrame)
	{
		LOGGER.debug("Column RBMKFrame changed");
		this.rbmkFrame = rbmkFrame;
	}
	
	protected static SimulationConfig getConfig()
	{
		return currentFrame.getConfig();
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

	protected static RBMKFrame getCurrentFrame()
	{
		return currentFrame == null ? currentFrame = Main.getFrame().get() : currentFrame;
	}
	
	@Override
	public abstract String toString();
	
}
