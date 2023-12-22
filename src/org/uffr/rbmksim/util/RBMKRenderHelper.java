package org.uffr.rbmksim.util;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.RBMKColumnBase;
import org.uffr.uffrlib.bytes.ByteSequence;
import org.uffr.uffrlib.images.Images;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class RBMKRenderHelper
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RBMKRenderHelper.class);
	// Render size for cells.
	public static final byte CELL_SIZE = 20, LINE_WIDTH = 4;
	public static final Color CELL_COLOR = Color.LIGHTGREY, BACKGROUND_COLOR = Color.BLACK.brighter();
	private final Canvas canvas;
	private final GraphicsContext graphics;
	private final Queue<Consumer<GraphicsContext>> renderQueue = new ArrayDeque<>();
	private final int rows, cols;
	
	public RBMKRenderHelper(int rows, int cols)
	{
		LOGGER.debug("Type 1 renderer constructed");
		canvas = new Canvas(cols, rows);
		
		graphics = canvas.getGraphicsContext2D();
		
		this.rows = rows;
		this.cols = cols;
		
		reset();
	}
	
	public RBMKRenderHelper(Canvas canvas, GraphicsContext graphics)
	{
		LOGGER.debug("Type 2 renderer constructed");
		this.canvas = canvas;
		this.graphics = graphics;
		
		rows = (int) canvas.getHeight();
		cols = (int) canvas.getWidth();
		
		reset();
	}

	public void reset()
	{
		LOGGER.debug("Current rendering reset");
		graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		graphics.fill();
	}
	
	public void flush()
	{
		LOGGER.debug("Flushing render queue");
		reset();
		renderBackground(graphics, canvas);
		while (!renderQueue.isEmpty())
			renderQueue.poll().accept(graphics);
	}
	
	public void renderColumn(RBMKColumnBase column)
	{
		LOGGER.debug("New column at {} offered to render queue", column.getLocation());
		renderQueue.offer(column::render);
	}
	
	public boolean validBounds(int x, int y)
	{
		return (y >= 0 && y < rows) && (x >= 0 && x < cols);
	}

	public boolean outOfBounds(int x, int y)
	{
		return !validBounds(x, y);
	}

	public int getRows()
	{
		return rows;
	}

	public int getCols()
	{
		return cols;
	}

	// AAAAAAAAAAAAAAAAAA
	public void save(Path path)
	{
		LOGGER.debug("Saving renders to file...");
		if (!renderQueue.isEmpty())
			flush();
		
		graphics.save();
		final WritableImage writableImage = canvas.snapshot(null, null);
		final PixelReader pixelReader = writableImage.getPixelReader();
		final IntBuffer buffer = IntBuffer.allocate(cols * rows);
		final ByteSequence rawSeq = ByteSequence.allocate(cols * rows, false);
		pixelReader.getPixels(0, 0, cols, rows, PixelFormat.getIntArgbInstance(), buffer, 0);
		while (buffer.hasRemaining())
		{
			int pixel = buffer.get();
			final int alpha = pixel >>> 24;
			
			pixel <<= 8;
			pixel |= alpha;
			
			rawSeq.writeInt(pixel);
		}
		
		Images.convertInternalToPng(rawSeq, path);
	}
	
	public static void genericRender(ColumnType type, GridLocation location, GraphicsContext graphics, double zoom)
	{
		LOGGER.debug("Generic render handler requested for type {} at {}", type, location);
		renderCell(location, graphics, zoom);
		graphics.setLineWidth(LINE_WIDTH);
		switch (type)
		{
			case ABSORBER:
				graphics.setFill(Color.DARKGRAY);
				graphics.fillRect((location.getX() + LINE_WIDTH) * CELL_SIZE, (location.getY() + LINE_WIDTH) * CELL_SIZE, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				break;
			case BOILER:
				graphics.setFill(Color.BLACK);
				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				graphics.setFill(Color.GREEN);
				graphics.fillRect(location.getX() + 8, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 8, location.getY() + CELL_SIZE - LINE_WIDTH);
				break;
			case BREEDER:
			case OUTGASSER:
				graphics.setFill(Color.DARKGRAY);
				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				graphics.setFill(Color.LIGHTYELLOW);
				graphics.fillRect(location.getX() + 8, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 8, location.getY() + CELL_SIZE - LINE_WIDTH);
				break;
			case CONTROL_AUTO:// Pass through expected
				graphics.setFill(Color.RED);
				graphics.rect(location.getX() + 6, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 6, location.getY() + CELL_SIZE - LINE_WIDTH);
			case CONTROL:
				graphics.setFill(Color.GRAY);
				graphics.rect(location.getX() + 8, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 8, location.getY() + CELL_SIZE - LINE_WIDTH);
				graphics.setFill(Color.BLACK);
				graphics.fillRect(location.getX() + 8, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 8, location.getY() + CELL_SIZE - LINE_WIDTH);
				break;
			case COOLER:
				graphics.setFill(Color.WHITE);
				graphics.fillRect(location.getX() + 8, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 8, location.getY() + CELL_SIZE - LINE_WIDTH);
				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + 8, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + 8 - LINE_WIDTH);
				break;
			case FUEL:
			case FUEL_SIM:
				graphics.setFill(Color.DARKGREEN);
				graphics.rect(location.getX() + 8, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 8, location.getY() + CELL_SIZE - LINE_WIDTH);
				graphics.setFill(Color.YELLOW);
				graphics.fillRect(location.getX() + 8, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 8, location.getY() + CELL_SIZE - LINE_WIDTH);
			case MODERATOR:
				graphics.setFill(Color.DARKGRAY.darker());
				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				break;
			case HEATEX:
				graphics.setFill(Color.BLACK);
				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				graphics.setFill(Color.WHEAT);
				graphics.fillRect(location.getX() + 8, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 8, location.getY() + CELL_SIZE - LINE_WIDTH);
				break;
			case REFLECTOR:
				graphics.setFill(Color.LIGHTGRAY);
				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				break;
			case STORAGE:
				graphics.setFill(Color.GRAY.darker());
				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				break;
			case BLANK:
			default: break;
		}
		renderEdges(location, graphics, zoom);
	}
	
	public static void renderBackground(GraphicsContext graphics, Canvas canvas)
	{
		LOGGER.trace("Filling background...");
		graphics.setFill(BACKGROUND_COLOR);
		graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	public static void renderCell(GridLocation location, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Rendering cell background...");
		graphics.setFill(Color.GRAY);
		graphics.fillRect(location.getX() * CELL_SIZE * zoom, location.getY() * CELL_SIZE * zoom, CELL_SIZE * zoom, CELL_SIZE * zoom);
	}
	
	public static void renderEdges(GridLocation location, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Rendering cell edges...");
		graphics.setLineWidth(LINE_WIDTH);
		graphics.setFill(Color.BLACK);
		graphics.rect(location.getX() * CELL_SIZE * zoom, location.getY() * CELL_SIZE * zoom, CELL_SIZE * zoom, CELL_SIZE * zoom);
	}
}
