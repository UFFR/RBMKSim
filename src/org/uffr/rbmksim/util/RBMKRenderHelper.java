package org.uffr.rbmksim.util;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Optional;
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
	public static final byte CELL_SIZE = 20, LINE_WIDTH = 2;
	public static final Color
							CELL_COLOR = colorFromHex(0xa3a3a3),
							LINE_COLOR = colorFromHex(0x808080),
							FUEL_OUTLINE = colorFromHex(0x0c3f0c),
							BOILER_FILL = colorFromHex(0xbe6036),
							GRAPHITE = colorFromHex(0x2c2c2c),
							ABSORBER = colorFromHex(0x5b666f),
							BREEDER = colorFromHex(0x404040),
							TRITIUM = colorFromHex(0xffed88),
							BORON = colorFromHex(0x5b666f),
							COOL = colorFromHex(0x00ffff),
							BACKGROUND_COLOR = colorFromHex(0x272727);
	private final Canvas canvas;
	private final GraphicsContext graphics;
	private final Queue<Consumer<GraphicsContext>> renderQueue;
	private final int rows, cols;
	public Optional<GridLocation> selectedLocation = Optional.empty();
	public double zoom = 1;
	
	public RBMKRenderHelper(int rows, int cols)
	{
		LOGGER.debug("Type 1 renderer constructed");
		canvas = new Canvas(cols, rows);
		renderQueue = new ArrayDeque<>(rows * cols);
		
		graphics = canvas.getGraphicsContext2D();
		
		this.rows = rows;
		this.cols = cols;
		
		reset();
	}
	
	public RBMKRenderHelper(Canvas canvas, GraphicsContext graphics, int size)
	{
		LOGGER.debug("Type 2 renderer constructed");
		this.canvas = canvas;
		this.graphics = graphics;
		
		renderQueue = new ArrayDeque<>(size);
		
		rows = (int) canvas.getHeight() / CELL_SIZE;
		cols = (int) canvas.getWidth() / CELL_SIZE;
		
		reset();
	}

	public void reset()
	{
		LOGGER.trace("Current rendering reset");
		graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		graphics.fill();
	}
	
	public void flush()
	{
		LOGGER.trace("Flushing render queue");
		reset();
		renderBackground(graphics, canvas);
		while (!renderQueue.isEmpty())
			renderQueue.poll().accept(graphics);
		if (selectedLocation.isPresent())
			drawSelectionRect(selectedLocation.get(), graphics, zoom);
	}
	
	public void renderColumn(RBMKColumnBase column)
	{
		LOGGER.trace("New column at {} offered to render queue", column.getLocation());
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
	
	// TODO Move to dedicated classes
	public static void genericRender(ColumnType type, GridLocation location, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Generic render handler requested for type {} at {}", type, location);
		renderEdges(location, graphics, zoom);
		renderCell(location, graphics, zoom);
		graphics.setLineWidth(LINE_WIDTH);
		switch (type)
		{
			case ABSORBER:
				graphics.setFill(BORON);
//				graphics.fillRect((location.getX() + LINE_WIDTH) * CELL_SIZE, (location.getY() + LINE_WIDTH) * CELL_SIZE, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				drawRect(location, LINE_WIDTH * 2, LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 4, CELL_SIZE - LINE_WIDTH * 4, graphics, zoom);
				break;
			case BOILER:
				graphics.setFill(Color.BLACK);
//				graphics.fillRect((location.getX() * CELL_SIZE + LINE_WIDTH) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom);
				drawRect(location, LINE_WIDTH, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setFill(BOILER_FILL);
//				graphics.fillRect((location.getX() * CELL_SIZE + LINE_WIDTH + 6) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH) * zoom, (CELL_SIZE - 16) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom);
				drawRect(location, LINE_WIDTH + 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case BREEDER:
			case OUTGASSER:
				graphics.setFill(BREEDER);
//				graphics.fillRect((location.getX() * CELL_SIZE + LINE_WIDTH) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom);
				drawRect(location, LINE_WIDTH, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setFill(TRITIUM);
//				graphics.fillRect((location.getX() * CELL_SIZE + LINE_WIDTH + 6) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH) * zoom, (CELL_SIZE - 16) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom);
				drawRect(location, LINE_WIDTH + 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case CONTROL_AUTO:// Pass through expected
				graphics.setStroke(Color.RED);
				graphics.setLineWidth(LINE_WIDTH * zoom);
//				graphics.strokeLine((location.getX() * CELL_SIZE + (LINE_WIDTH * 4.5) * 1.5) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH * 1.5) * zoom, (location.getX() * CELL_SIZE + (LINE_WIDTH * 4.5) * 1.5) * zoom, (location.getY() * CELL_SIZE + CELL_SIZE - LINE_WIDTH * 1.5) * zoom);
//				graphics.strokeLine((location.getX() * CELL_SIZE + CELL_SIZE - (LINE_WIDTH * 4.5) * 1.5) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH * 1.5) * zoom, (location.getX() * CELL_SIZE + CELL_SIZE - (LINE_WIDTH * 4.5) * 1.5) * zoom, (location.getY() * CELL_SIZE + CELL_SIZE - LINE_WIDTH * 1.5) * zoom);
				drawLine(location, LINE_WIDTH * 3, LINE_WIDTH, LINE_WIDTH * 3, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				drawLine(location, CELL_SIZE - LINE_WIDTH * 4, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 4, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
			case CONTROL:
				graphics.setStroke(BORON);
				graphics.setLineWidth(LINE_WIDTH * zoom);
//				graphics.strokeLine((location.getX() * CELL_SIZE + LINE_WIDTH * 1.5) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH * 1.5) * zoom, (location.getX() * CELL_SIZE + LINE_WIDTH * 1.5) * zoom, (location.getY() * CELL_SIZE + CELL_SIZE - LINE_WIDTH * 1.5) * zoom);
//				graphics.strokeLine((location.getX() * CELL_SIZE + CELL_SIZE - LINE_WIDTH * 1.5) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH * 1.5) * zoom, (location.getX() * CELL_SIZE + CELL_SIZE - LINE_WIDTH * 1.5) * zoom, (location.getY() * CELL_SIZE + CELL_SIZE - LINE_WIDTH * 1.5) * zoom);
				drawLine(location, LINE_WIDTH, LINE_WIDTH, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				drawLine(location, CELL_SIZE - LINE_WIDTH * 2, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setStroke(LINE_COLOR);
//				graphics.strokeLine((location.getX() * CELL_SIZE + (LINE_WIDTH * 2) * 1.25) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH * 1.5) * zoom, (location.getX() * CELL_SIZE + (LINE_WIDTH * 2) * 1.25) * zoom, (location.getY() * CELL_SIZE + CELL_SIZE - LINE_WIDTH * 1.5) * zoom);
//				graphics.strokeLine((location.getX() * CELL_SIZE + CELL_SIZE - (LINE_WIDTH * 2) * 1.25) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH * 1.5) * zoom, (location.getX() * CELL_SIZE + CELL_SIZE - (LINE_WIDTH * 2) * 1.25) * zoom, (location.getY() * CELL_SIZE + CELL_SIZE - LINE_WIDTH * 1.5) * zoom);
				drawLine(location, LINE_WIDTH * 2, LINE_WIDTH, LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				drawLine(location, CELL_SIZE - LINE_WIDTH * 3, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 3, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setFill(Color.BLACK);
//				graphics.fillRect((location.getX() * CELL_SIZE + 8) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH) * zoom, (CELL_SIZE - 16) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom);
				drawRect(location, LINE_WIDTH + 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case COOLER:
				graphics.setFill(COOL);
//				graphics.fillRect(location.getX() + 8, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 8, location.getY() + CELL_SIZE - LINE_WIDTH);
//				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + 8, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + 8 - LINE_WIDTH);
				drawRect(location, LINE_WIDTH + 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				drawRect(location, LINE_WIDTH, LINE_WIDTH + 6, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 8, graphics, zoom);
				break;
			case FUEL:
			case FUEL_SIM:
				graphics.setFill(FUEL_OUTLINE);
//				graphics.fillRect((location.getX() * CELL_SIZE + 6) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH) * zoom, (CELL_SIZE - 12) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom);
				drawRect(location, 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 6, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setFill(Color.BLACK);
//				graphics.fillRect((location.getX() * CELL_SIZE + 8) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH) * zoom, (CELL_SIZE - 16) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom);
				drawRect(location, 8, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case MODERATOR:
				graphics.setFill(GRAPHITE);
//				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				drawRect(location, LINE_WIDTH * 2, LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 4, CELL_SIZE - LINE_WIDTH * 4, graphics, zoom);
				break;
			case HEATEX:
				graphics.setFill(Color.BLACK);
//				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				drawRect(location, LINE_WIDTH, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setFill(COOL);
//				graphics.fillRect(location.getX() + 8, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - 8, location.getY() + CELL_SIZE - LINE_WIDTH);
				drawRect(location, LINE_WIDTH + 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case REFLECTOR:
				graphics.setFill(LINE_COLOR);
//				graphics.fillRect((location.getX() * CELL_SIZE + LINE_WIDTH * 2) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH * 2) * zoom, (CELL_SIZE - LINE_WIDTH * 4) * zoom, (CELL_SIZE - LINE_WIDTH * 4) * zoom);
				drawRect(location, LINE_WIDTH * 2, LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 4, CELL_SIZE - LINE_WIDTH * 4, graphics, zoom);
				break;
			case STORAGE:
				graphics.setFill(Color.BLACK.brighter());
//				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
				drawRect(location, LINE_WIDTH, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case BLANK:
			default: break;
		}
	}
	
	public static void clearCanvas(GraphicsContext graphics, Canvas canvas)
	{
		LOGGER.trace("Clearing canvas...");
		graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	public static void renderBackground(GraphicsContext graphics, Canvas canvas)
	{
		LOGGER.trace("Filling background...");
		graphics.setFill(BACKGROUND_COLOR);
		graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	public static void renderCell(GridLocation location, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Rendering generic cell background at {} with zoom {}...", graphics, zoom);
		graphics.setFill(CELL_COLOR);
		graphics.fillRect((location.getX() * CELL_SIZE + LINE_WIDTH) * zoom, (location.getY() * CELL_SIZE + LINE_WIDTH) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom, (CELL_SIZE - LINE_WIDTH * 2) * zoom);
	}
	
	public static void renderEdges(GridLocation location, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Rendering generic cell edges at {} with zoom {}...", location, zoom);
		graphics.setLineWidth(LINE_WIDTH);
		graphics.setFill(LINE_COLOR);
		graphics.fillRect(location.getX() * CELL_SIZE * zoom, location.getY() * CELL_SIZE * zoom, CELL_SIZE * zoom, CELL_SIZE * zoom);
	}
	
	public static void drawLine(GridLocation location, double x1, double y1, double x2, double y2, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Drawing line at {}, starting at offset [x={}, y={}], ending at offset [x={}, y={}], and with zoom {}", location, x1, y1, x2, y2, zoom);
		graphics.strokeLine(
				(location.getX() * CELL_SIZE + x1 + 1) * zoom,
				(location.getY() * CELL_SIZE + y1 + 1) * zoom,
				(location.getX() * CELL_SIZE + x2 + 1) * zoom,
				(location.getY() * CELL_SIZE + y2 + 1) * zoom);
	}
	
	public static void drawRect(GridLocation location, double x, double y, double w, double h, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Drawing rectangle at {}, with offset [x={}, y={}], width {} and height {}, with zoom {}", location, x, y, w, h, zoom);
		graphics.fillRect(
				(location.getX() * CELL_SIZE + x) * zoom,
				(location.getY() * CELL_SIZE + y) * zoom,
				w * zoom,
				h * zoom);
	}
	
	public static void drawSelectionRect(GridLocation location, GraphicsContext graphics, double zoom)
	{
		graphics.setLineWidth(LINE_WIDTH * 2);
		graphics.setStroke(Color.WHITE);
		graphics.strokeRect(
				location.getX() * CELL_SIZE * zoom,
				location.getY() * CELL_SIZE * zoom,
				CELL_SIZE * zoom,
				CELL_SIZE * zoom);
	}
	
	private static Color colorFromHex(int rgb)
	{
		final double max = 255;
		return new Color(((rgb >>> 16) & 0xff) / max, ((rgb >>> 8) & 0xff) / max, (rgb & 0xff) / max, 1);
	}
}
