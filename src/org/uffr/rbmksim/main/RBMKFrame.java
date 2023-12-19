package org.uffr.rbmksim.main;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.config.SimulationConfig;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.RBMKColumnBase;
import org.uffr.rbmksim.util.RBMKRenderHelper;
import org.uffr.uffrlib.collections.matrix.ExpandingArrayMatrix;
import org.uffr.uffrlib.collections.matrix.Matrices;
import org.uffr.uffrlib.collections.matrix.Matrix;
import org.uffr.uffrlib.hashing.Hashable;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.PrimitiveSink;

import javafx.scene.canvas.Canvas;

/**
 * Base RBMK class.
 * <br>
 * Holds a matrix where columns are held, a set of valid locations within said matrix, a canvas for rendering, and some other basic information.
 * <br>
 * Since it is a matrix, coordinates are 0-indexed but begin at the top left corner, similar to a table.
 * <br>
 * <h6>Note on terminology:</h6>
 * <li> <i>Frame</i>: This, the container for the RBMK grid and associated data.
 * <li> <i>Grid</i>: A matrix/table containing the RBMK columns in its cells.
 * <li> <i>Add</i>: Since the underlying container is a matrix and not a collection like a list, this means setting a certain cell, overwriting the previous column if possible.
 * <li> <i>Remove</i>: Same reason as above, the cell is set to null and the location removed from the registry so it is skipped while ticking.
 * @author UFFR
 *
 */
public abstract class RBMKFrame implements Hashable, Serializable, Cloneable
{
	private static final long serialVersionUID = 6249022375925162759L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RBMKFrame.class);
	private static int creationCount = 1;
	public static final byte DEFAULT_SIZE = 15;
	// Configuration instance for use by concrete classes as needed.
	protected final SimulationConfig config;
	// The matrix holding the columns themselves.
	protected final Matrix<RBMKColumnBase> grid;
	// A set of grid locations known to have columns, for fast checking and looping.
	protected final Set<GridLocation> registeredLocations;
	// A reference to the UI's canvas for rendering columns.
	protected transient Canvas canvas;
	// Render helper
	protected transient RBMKRenderHelper renderer;
	// Basic information about the RBMK: the design's name, its creator, and the version. All may be empty.
	protected String name, creatorName, version;
	protected LocalDate date;
	// Some quick tracking values, a timer for delays, and a tick counter.
	protected int rows = DEFAULT_SIZE, columns = DEFAULT_SIZE, ticks;
	protected transient int graphTimer;
	
	public RBMKFrame(Canvas canvas)
	{
		LOGGER.info("Generic RBMKFrame constructor called");
		this.canvas = canvas;
		
		config = new SimulationConfig();
		grid = new ExpandingArrayMatrix<>(DEFAULT_SIZE, DEFAULT_SIZE);
		registeredLocations = new HashSet<>(DEFAULT_SIZE * DEFAULT_SIZE);
		date = LocalDate.now();
		renderer = new RBMKRenderHelper(canvas, canvas.getGraphicsContext2D());
		
		// Defaults
		name = "Untitled" + creationCount++;
		creatorName = Main.config.username;
		version = "1.0.0";
		date = LocalDate.now();
	}
	
	// What is this, C++?
	public RBMKFrame(RBMKFrame frame)
	{
		LOGGER.info("RBMKFrame copy constructor called");
		this.canvas = frame.canvas;
		this.config = frame.config.clone();
		this.grid = new ExpandingArrayMatrix<>(frame.grid);
		this.registeredLocations = new HashSet<>(frame.registeredLocations);
		this.name = frame.name;
		this.creatorName = frame.creatorName;
		this.version = frame.version;
		this.rows = frame.rows;
		this.columns = frame.columns;
		this.name = frame.name;
		this.creatorName = frame.creatorName;
		this.version = frame.version;
		this.date = frame.date;
		// Probably shouldn't copy ticks if it's a new frame
		
		renderer = new RBMKRenderHelper(canvas, canvas.getGraphicsContext2D());
	}
	
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
	public abstract void convertColumns();
	/**
	 * Render the design to an exportable image.
	 * @return A raw image.
	 */
//	public abstract InternalImage renderToImage();
	
	/**
	 * The render helper associated with this frame.
	 * @return A render helper class.
	 */
	protected RBMKRenderHelper getRenderer()
	{
		LOGGER.trace("RBMKFrame.getRenderer() called...");
		// In case it was deserialized
		return renderer == null ? renderer = new RBMKRenderHelper(canvas, canvas.getGraphicsContext2D()) : renderer;
	}
	
	/**
	 * Tick over every matrix cell with a column.
	 */
	@SuppressWarnings("static-method")
	public void tick()
	{
		LOGGER.trace("RBMKFrame ticked...");
		// We can be pretty sure registeredLocations is valid.
//		for (GridLocation location : registeredLocations)
//			getColumnAtCoords(location).render(location.getX(), location.getY(), canvas.getGraphicsContext2D());
//		ticks++;
	}
	
	public void render()
	{
		LOGGER.debug("RBMKFrame.render() called...");
		grid.stream().filter(Objects::nonNull).filter(RBMKColumnBase::shouldRender).forEach(getRenderer()::renderColumn);
//		for (RBMKColumnBase col : grid)
//			if (col != null && col.shouldRender())
//				getRenderer().renderColumn(col);
		renderer.flush();
	}
	
	// Change the canvas, if needed.
	public void setCanvas(Canvas canvas)
	{
		LOGGER.debug("RBMKFrame Canvas reset...");
		this.canvas = canvas;
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
	
	public void setVersion(String version)
	{
		LOGGER.debug("RBMKFrame version changed to: {}", version);
		this.version = version;
	}
	
	public String getVersion()
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
	public boolean addColumn(RBMKColumnBase column)
	{
		LOGGER.debug("Attempting to add column at: {}", column.getLocation());
		if (!validCoords(column.getLocation()))
			return false;
		LOGGER.trace("Column was accepted");
		grid.set(column.getLocation().getX(), column.getLocation().getY(), column);
		registeredLocations.add(column.getLocation());
		return true;
	}
	
	/**
	 * Remove a column at the specified coordinates.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	public void removeColumn(int x, int y)
	{
		LOGGER.debug("Removing column at: [x={}, y={}]", x, y);
		grid.remove(x, y);
		registeredLocations.removeIf(loc -> loc.getX() == x && loc.getY() == y);
	}
	
	/**
	 * Remove a column at the specified {@code GridLocation}. Convenience method for {@link #removeColumn(int, int)}.
	 * @param location The location to remove the column at.
	 */
	public void removeColumn(GridLocation location)
	{
		removeColumn(location.getX(), location.getY());
	}
	
	/**
	 * Checks the entire frame for the given column.
	 * @param column The column to check.
	 * @return True, if it is in the frame, false otherwise.
	 */
	public boolean columnInGrid(RBMKColumnBase column)
	{
		return grid.contains(column);
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
	public boolean validCoords(GridLocation loc)
	{
		return validCoords(loc.getX(), loc.getY());
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
		return grid.get(x, y);
	}
	
	/**
	 * Retrieve a column in the grid.
	 * @param location The desired location.
	 * @return The column or null if empty cell.
	 * @throws ArrayIndexOutOfBoundsException If the coordinates were out of bounds.
	 */
	public RBMKColumnBase getColumnAtCoords(GridLocation location)
	{
		return getColumnAtCoords(location.getX(), location.getY());
	}
	
	/**
	 * Removes all columns out of bounds.
	 */
	protected void removeInvalid()
	{
		LOGGER.debug("RBMKFrame removing invalid values...");
		grid.removeIf(col -> invalidCoords(col.getLocation()));
		registeredLocations.removeIf(this::invalidCoords);
	}
	
	/**
	 * Resets all columns to their default and sets the tick counter back to 0.
	 */
	public void reset()
	{
		LOGGER.debug("RBMKFrame resetting state...");
		grid.stream().filter(Objects::nonNull).forEach(RBMKColumnBase::reset);
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
		{
			for (int i = 0; i < diff; i++)
				grid.removeRow();// TODO Implement a proper bulk row removal method
			removeInvalid();
		}
	}
	
	public void setColumns(int columns)
	{
		LOGGER.debug("Changed column count from {} to {}", this.columns, columns);
		final int diff = columns - this.columns;
		final boolean shouldCheck = diff < 0;
		this.columns = columns;
		if (shouldCheck)
		{
			for (int i = 0; i < diff; i++)
				grid.removeCol();// TODO Implement a proper bulk column removal method
			removeInvalid();
		}
	}
	
	/**
	 * Add another row.
	 */
	public void incrementRows()
	{
		LOGGER.debug("Incrementing rows from {}", rows);
		rows++;
		grid.addRow();
	}
	
	/**
	 * Add another column.
	 */
	public void incrementColumns()
	{
		LOGGER.debug("Incrementing columns from {}", columns);
		columns++;
		grid.addCol();
	}
	
	/**
	 * Remove a row and any columns part of it.
	 */
	public void decrementRows()
	{
		LOGGER.debug("Decrementing rows from {}", rows);
		rows--;
		grid.removeRow();
		removeInvalid();
	}
	
	/**
	 * Remove a column and any columns part of it.
	 */
	public void decrementColumns()
	{
		LOGGER.debug("Decrementing columns from {}", columns);
		columns--;
		grid.removeCol();
		removeInvalid();
	}
	
	/**
	 * Get the matrix itself.
	 * @return An unmodifiable reference to the matrix used internally.
	 */
	public Matrix<RBMKColumnBase> getGrid()
	{
		return Matrices.asUnmodifiable(grid);
	}
	
	/**
	 * Get the set of registered locations in the matrix.
	 * @return An immutable copy of the set.
	 */
	public Set<GridLocation> getRegisteredLocations()
	{
		return ImmutableSet.copyOf(registeredLocations);
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		sink.putInt(rows).putInt(columns).putInt(ticks).putString(name, UTF_8).putString(creatorName, UTF_8).putString(version, UTF_8);
		config.funnelInto(sink);
		grid.forEach(c -> c.funnelInto(sink));
		registeredLocations.forEach(l -> GridLocation.FUNNEL.funnel(l, sink));
	}
	
	@Override
	public RBMKFrame clone()
	{
		try
		{
			return (RBMKFrame) super.clone();
		} catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
			Main.openErrorDialog(e);
			return null;
		}
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(columns, config, creatorName, date, grid, name, registeredLocations, rows, ticks, version);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof RBMKFrame))
			return false;
		final RBMKFrame other = (RBMKFrame) obj;
		return columns == other.columns && Objects.equals(config, other.config)
				&& Objects.equals(creatorName, other.creatorName) && Objects.equals(date, other.date)
				&& Objects.equals(grid, other.grid) && Objects.equals(name, other.name)
				&& Objects.equals(registeredLocations, other.registeredLocations) && rows == other.rows
				&& ticks == other.ticks && Objects.equals(version, other.version);
	}
	
	@Override
	public abstract String toString();
	
}
