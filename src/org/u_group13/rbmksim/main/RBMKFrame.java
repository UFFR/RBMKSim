package org.u_group13.rbmksim.main;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonKey;
import com.google.common.hash.PrimitiveSink;
import javafx.scene.canvas.Canvas;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.config.SimulationConfig;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.simulation.RBMKColumnBase;
import org.u_group13.rbmksim.util.RBMKRenderHelper;
import org.uffr.uffrlib.hashing.Hashable;
import org.uffr.uffrlib.misc.Version;

import javax.annotation.Nullable;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/**
 * Base RBMK class.
 * <br>
 * Holds a matrix where columns are held, a set of valid locations within said matrix, a canvas for rendering, and some other basic information.
 * <br>
 * Since it is a matrix, coordinates are 0-indexed but begin in the top left corner, similar to a table.
 * <br>
 * <h6>Note on terminology:</h6>
 * <li> <i>Frame</i>: This, the container for the RBMK grid and associated data.
 * <li> <i>Grid</i>: A matrix/table containing the RBMK columns in its cells.
 * <li> <i>Add</i>: Since the underlying container is a matrix and not a collection like a list, this means setting a certain cell, overwriting the previous column if possible.
 * <li> <i>Remove</i>: Same reason as above, the cell is set to null and the location removed from the registry, so it is skipped while ticking.
 * @author UFFR
 *
 */
@SuppressWarnings("UnstableApiUsage")
public abstract sealed class RBMKFrame implements Hashable, Serializable, Cloneable permits RBMKBlueprint, RBMKSimulation
{
	@Serial
	private static final long serialVersionUID = 6249022375925162759L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RBMKFrame.class);
	private static int creationCount = 1;
	public static final byte DEFAULT_SIZE = 15;
	// Configuration instance for use by concrete classes as needed.
	protected final SimulationConfig config;
	protected final HashMap<GridLocation,RBMKColumnBase> grid;// 15 * 15 by default
	// A reference to the UI's canvas for rendering columns.
	@JsonIgnore
	protected transient Canvas canvas;
	// Render helper
	@JsonIgnore
	protected transient RBMKRenderHelper renderer;
	// Basic information about the RBMK: the design's name, its creator, and the version.
	protected String name, creatorName;
	protected Version version;
	protected LocalDate date;
	// Some quick tracking values, a timer for delays, and a tick counter.
	protected int rows = DEFAULT_SIZE, columns = DEFAULT_SIZE, ticks;
	// Timer for adding nodes to the graph, for performance
	protected transient int graphTimer;
	// Selected column
	protected static GridLocation selectedLocation = null;

	public RBMKFrame(Canvas canvas)
	{
		LOGGER.info("Generic RBMKFrame constructor called");
		this.canvas = canvas;
		
		config = new SimulationConfig();
//		grid = new ExpandingArrayMatrix<>(DEFAULT_SIZE, DEFAULT_SIZE);
		grid = new HashMap<>(DEFAULT_SIZE * DEFAULT_SIZE);
		date = LocalDate.now();
		renderer = new RBMKRenderHelper(canvas, canvas.getGraphicsContext2D(), rows, columns);
		
		// Defaults
		name = "Untitled" + creationCount++;
		creatorName = Main.config.username;
		version = new Version(1);
		date = LocalDate.now();
	}
	
	// What is this, C++?
	public RBMKFrame(@NotNull RBMKFrame frame)
	{
		LOGGER.info("RBMKFrame copy constructor called");
		this.canvas = frame.canvas;
		this.config = frame.config.clone();
		this.grid = new HashMap<>(frame.grid);
		this.rows = frame.rows;
		this.columns = frame.columns;
		this.name = frame.name;
		this.creatorName = frame.creatorName;
		this.version = frame.version;
		this.date = frame.date;
		this.ticks = frame.ticks;// Might as well

		renderer = new RBMKRenderHelper(canvas, canvas.getGraphicsContext2D(), rows, columns);
	}
	

	protected abstract RBMKColumnBase newOfType(GridLocation location, ColumnType type);
	protected abstract RBMKColumnBase convertType(RBMKColumnBase column);
	/**
	 * Checks the matrix for columns that appear to be bugged or severely outdated. Implementation may be different between subclasses.  
	 * @param discrepancies A tracking list of all detected discrepancies and whatever action was taken. Compiled into a log and displayed to the user.
	 * @param repair If the method has permission to attempt and repair any discrepancies.
	 */
	public abstract void checkForDiscrepancies(List<String> discrepancies, boolean repair);
	/**
	 * Since there are only 2 subclasses, this converts from one to another.
	 * @return The other type.
	 */
	public abstract RBMKFrame convertType();
	protected abstract void convertColumns();
    //	public abstract InternalImage renderToImage();
	
	/**
	 * Sets the grid location to a column of type
	 * @param location The location to set
	 * @param type The column type to create or null to remove the column
	 * @return True, if successful and the grid changed or false if it couldn't be
	 */
	public boolean setColumn(GridLocation location, @Nullable ColumnType type)
	{
		LOGGER.debug("Setting {} type column at {}", type, location);
		if (invalidCoords(location))
			return false;
		if (type == null)
			removeColumn(location);
		else
			addColumn(newOfType(location, type));
		return true;
	}
	
	/**
	 * The render helper associated with this frame.
	 * @return A render helper class.
	 */
	public RBMKRenderHelper getRenderer()
	{
		LOGGER.trace("RBMKFrame.getRenderer() called...");
		// In case it was deserialized
		return renderer == null ? renderer = new RBMKRenderHelper(canvas, canvas.getGraphicsContext2D(), rows, columns) : renderer;
	}
	
	/**
	 * Tick over every matrix cell with a column.
	 */
	@SuppressWarnings("static-method")
	public void tick()
	{
		LOGGER.trace("Ticked...");
		// We can be pretty sure registeredLocations is valid.
	}
	
	public void render()
	{
		LOGGER.debug("render() called...");
		grid.values().stream().filter(RBMKColumnBase::shouldRender).forEach(getRenderer()::renderColumn);
		renderer.flush();
	}

	public void forceRender()
	{
		LOGGER.trace("forceRender() called...");
		grid.values().forEach(getRenderer()::renderColumn);
		renderer.flush();
	}
	
	// Change the canvas, if needed, such as during deserialization
	public void setCanvas(Canvas canvas)
	{
		LOGGER.debug("RBMKFrame Canvas reset...");
		this.canvas = canvas;
		renderer = new RBMKRenderHelper(canvas, canvas.getGraphicsContext2D(), rows, columns);
	}
	
	public Canvas getCanvas()
	{
		return canvas;
	}
	
	public void setName(String name)
	{
		LOGGER.debug("RBMKFrame name changed to: {}", name);
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setCreatorName(String creatorName)
	{
		LOGGER.debug("RBMKFrame creator changed to: {}", creatorName);
		this.creatorName = creatorName;
	}
	
	public String getCreatorName()
	{
		return creatorName;
	}
	
	public void setVersion(Version version)
	{
		LOGGER.debug("RBMKFrame version changed to: {}", version);
		this.version = version;
	}
	
	public Version getVersion()
	{
		return version;
	}
	
	public void setDate(LocalDate date)
	{
		LOGGER.debug("RBMKFrame date changed to: {}", date);
		this.date = date;
	}
	
	public LocalDate getDate()
	{
		return date;
	}
	
	public int getTicks()
	{
		return ticks;
	}
	
	/**
	 * Tries to add a column at its self specified location.
	 * @param column The column to add.
	 * @return True, if the column was added, false, if it was out of bounds and couldn't.
	 */
	public boolean addColumn(@NotNull RBMKColumnBase column)
	{
		LOGGER.debug("Attempting to add {} at: {}", column.getClass(), column.getLocation());
		if (!validCoords(column.getLocation()))
			return false;
		LOGGER.trace("Column was accepted");
		grid.put(column.getLocation(), column);
		return true;
	}
	
	/**
	 * Remove a column at the specified coordinates.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	public void removeColumn(int x, int y)
	{
		removeColumn(new GridLocation(x, y));
	}
	
	/**
	 * Remove a column at the specified {@code GridLocation}. Convenience method for {@link #removeColumn(int, int)}.
	 * @param location The location to remove the column at.
	 */
	public void removeColumn(GridLocation location)
	{
		LOGGER.debug("Removing column at: {}", location);
		grid.remove(location);
	}
	
	/**
	 * Checks the entire frame for the given column.
	 * @param column The column to check.
	 * @return True, if it is in the frame, false otherwise.
	 */
	public boolean columnInGrid(RBMKColumnBase column)
	{
		return grid.containsValue(column);
	}
	
	/**
	 * Checks the given coordinates if they are within the bounds of the frame.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @return True, if the coordinates are within the bounds and thus valid, false otherwise.
	 */
	public boolean validCoords(int x, int y)
	{
		return (x < columns && y < rows) && (x >= 0 && y >= 0);
	}
	
	/**
	 * Checks the given {@code GridLocation} if it is valid. Convenience method for {@link #validCoords(int, int)}.
	 * @param loc The location to check.
	 * @return True, if the coordinates are within the bounds and thus valid, false otherwise.
	 */
	public boolean validCoords(@NotNull GridLocation loc)
	{
		return validCoords(loc.x(), loc.y());
	}
	
	/**
	 * Convenience method that inverts the result of {@link #validCoords(GridLocation)}.
	 * Useful for being a bit easier to read in conditions and for method references.
	 * @param loc The location to check.
	 * @return True, if the coordinates are out of bound and thus invalid, false otherwise.
	 */
	public boolean invalidCoords(GridLocation loc)
	{
		return !validCoords(loc);
	}
	
	/**
	 * Retrieve a column in the grid.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @return The column or null if empty cell.
	 * @throws ArrayIndexOutOfBoundsException If the coordinates were out of bounds.
	 */
	public RBMKColumnBase getColumnAtCoords(int x, int y)
	{
		return grid.get(new GridLocation(x, y));
	}
	
	/**
	 * Retrieve a column in the grid.
	 * @param location The desired location.
	 * @return The column or null if empty cell.
	 * @throws ArrayIndexOutOfBoundsException If the coordinates were out of bounds.
	 */
	public RBMKColumnBase getColumnAtCoords(GridLocation location)
	{
		return grid.get(location);
	}
	
	/**
	 * Removes all columns out of bounds.
	 */
	protected void removeInvalid()
	{
		LOGGER.debug("RBMKFrame removing invalid values...");
		grid.entrySet().removeIf(entry -> invalidCoords(entry.getValue().getLocation()) || !entry.getKey().equals(entry.getValue().getLocation()));
	}
	
	/**
	 * Resets all columns to their default and sets the tick counter back to 0.
	 */
	public void reset()
	{
		LOGGER.debug("RBMKFrame resetting state...");
		grid.values().forEach(RBMKColumnBase::reset);
		ticks = 0;
	}
	
	public SimulationConfig getConfig()
	{
		return config;
	}
	
	public int getRows()
	{
		return rows;
	}
	
	public int getColumns()
	{
		return columns;
	}
	
	public void setRows(int rows)
	{
		LOGGER.debug("Changed row count from {} to {}", this.rows, rows);
		final int diff = rows - this.rows;
		final boolean shouldCheck = diff < 0;
		this.rows = rows;
		if (shouldCheck)
			removeInvalid();
	}
	
	public void setColumns(int columns)
	{
		LOGGER.debug("Changed column count from {} to {}", this.columns, columns);
		final int diff = columns - this.columns;
		final boolean shouldCheck = diff < 0;
		this.columns = columns;
		if (shouldCheck)
			removeInvalid();
	}
	
	/**
	 * Add another row.
	 */
	public void incrementRows()
	{
		LOGGER.debug("Incrementing rows from {}", rows);
		rows++;
	}
	
	/**
	 * Add another column.
	 */
	public void incrementColumns()
	{
		LOGGER.debug("Incrementing columns from {}", columns);
		columns++;
	}
	
	/**
	 * Remove a row and any columns part of it.
	 */
	public void decrementRows()
	{
		LOGGER.debug("Decrementing rows from {}", rows);
		rows--;
		removeInvalid();
	}
	
	/**
	 * Remove a column and any columns part of it.
	 */
	public void decrementColumns()
	{
		LOGGER.debug("Decrementing columns from {}", columns);
		columns--;
		removeInvalid();
	}
	
	/**
	 * Get the matrix itself.
	 * @return An unmodifiable reference to the matrix used internally.
	 */
	public Map<GridLocation, RBMKColumnBase> getGrid()
	{
		return Collections.unmodifiableMap(grid);
	}

	public static Optional<GridLocation> getSelectedLocation()
	{
		return Optional.ofNullable(selectedLocation);
	}
	
	public void setSelectedLocation(GridLocation selectedLocation)
	{
		renderer.selectedLocation = RBMKFrame.selectedLocation = Objects.equals(RBMKFrame.selectedLocation, selectedLocation) || (selectedLocation != null && invalidCoords(selectedLocation)) ? null : selectedLocation;
	}
	
	@Override
	public void funnelInto(@NotNull PrimitiveSink sink)
	{
		sink.putInt(rows).putInt(columns).putInt(ticks).putString(name, UTF_8).putString(creatorName, UTF_8);
		version.funnelInto(sink);
		config.funnelInto(sink);
		grid.values().forEach(col -> col.funnelInto(sink));
	}

	@Deprecated
	@Override
	public RBMKFrame clone()
	{
		try
		{
			return (RBMKFrame) super.clone();
		} catch (CloneNotSupportedException e)
		{
			LOGGER.warn("Could not clone frame, this should not be possible!", e);
			Main.openErrorDialog(e);
			return null;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final RBMKFrame rbmkFrame = (RBMKFrame) o;
		return rows == rbmkFrame.rows && columns == rbmkFrame.columns && ticks == rbmkFrame.ticks && graphTimer == rbmkFrame.graphTimer && Objects.equals(
				config, rbmkFrame.config) && Objects.equals(grid, rbmkFrame.grid) && Objects.equals(
				canvas, rbmkFrame.canvas) && Objects.equals(renderer,
		                                                    rbmkFrame.renderer) && Objects.equals(name,
		                                                                                          rbmkFrame.name) && Objects.equals(
				creatorName, rbmkFrame.creatorName) && Objects.equals(version,
		                                                              rbmkFrame.version) && Objects.equals(
				date, rbmkFrame.date);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(config, grid, canvas, renderer, name, creatorName, version, date, rows, columns, ticks,
		                    graphTimer);
	}

	@Override
	public abstract String toString();
	
}
