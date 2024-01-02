package org.uffr.rbmksim.simulation;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.config.SimulationConfig;
import org.uffr.rbmksim.main.Main;
import org.uffr.rbmksim.main.RBMKFrame;
import org.uffr.rbmksim.util.I18n;
import org.uffr.rbmksim.util.InfoProviderNT;
import org.uffr.rbmksim.util.RBMKRenderHelper;
import org.uffr.rbmksim.util.TextBuilder;
import org.uffr.uffrlib.hashing.Hashable;

import com.google.common.hash.PrimitiveSink;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class RBMKColumnBase implements InfoProviderNT, Hashable, Serializable
{
	@Serial
	private static final long serialVersionUID = -4281135799096443502L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RBMKColumnBase.class);
	private static RBMKFrame currentFrame;
	public static final short MAX_WATER = 16000, MAX_STEAM = 16000, MAX_HEAT_DEFAULT = 1500;
	
	protected final GridLocation location;
	// Most columns only need to be rendered when necessary and others seldomly to begin with
	protected boolean shouldRender = true;
	public RBMKColumnBase(GridLocation location)
	{
		LOGGER.trace("New RBMKColumnBase constructed");
		this.location = location;
	}
	
	/**
	 * Copy constructor but with a different location, for moving columns. Recommended to properly implement.
	 * @param column Old column to copy.
	 * @param location (Presumably) new location this column should reside.
	 */
	public RBMKColumnBase(RBMKColumnBase column, GridLocation location)
	{
		this(location);
		LOGGER.trace("RBMKColumnBase copy constructor called");
	}
	
	public static void setCurrentFrame(RBMKFrame currentFrame)
	{
		RBMKColumnBase.currentFrame = currentFrame;
	}

	public abstract ColumnType getColumnType();
	public abstract boolean isModerated();
	public abstract void reset();
	
	public void render(GraphicsContext graphics)
	{
		LOGGER.trace("Column rendering requested; self-type: {}", getColumnType());
		RBMKRenderHelper.renderEdges(location, graphics, getRendererZoom());
		RBMKRenderHelper.renderCenter(location, graphics, getRendererZoom());
	}
	
	protected static SimulationConfig getConfig()
	{
		return currentFrame.getConfig();
	}
	
	protected static double getRendererZoom()
	{
		return currentFrame.getRenderer().zoom;
	}
	
	public final GridLocation getLocation()
	{
		return location;
	}
	
	// For performance
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
	public void addInformation(List<Text> info)
	{
		info.add(new TextBuilder(I18n.resolve("column.info")).setUnderline(true).setFont(new Font(20)).getText());
		info.add(InfoProviderNT.getNewline());
		info.add(new TextBuilder(I18n.resolve("column.type", getColumnType())).setFill(Color.YELLOW).getText());
		info.add(InfoProviderNT.getNewline());
		info.add(new TextBuilder(I18n.resolve("column.moderated", isModerated())).setFill(Color.YELLOW).getText());
		info.add(InfoProviderNT.getNewline());
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
		if (!(obj instanceof RBMKColumnBase other))
			return false;
        return Objects.equals(location, other.location) && shouldRender == other.shouldRender;
	}

	protected static RBMKFrame getCurrentFrame()
	{
		return currentFrame == null ? currentFrame = Main.getFrame().get() : currentFrame;
	}
	
	@Override
	public abstract String toString();
	
}
