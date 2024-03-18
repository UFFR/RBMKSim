package org.u_group13.rbmksim.util;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.simulation.RBMKColumnBase;
import org.uffr.uffrlib.bytes.ByteSequence;
import org.uffr.uffrlib.images.Images;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

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
	public GridLocation selectedLocation = null;
	public double zoom = 1;
	
	public RBMKRenderHelper(int rows, int cols)
	{
		LOGGER.debug("Independent renderer constructed");
		canvas = new Canvas(cols, rows);
		renderQueue = new ArrayDeque<>(rows * cols);
		
		graphics = canvas.getGraphicsContext2D();
		
		this.rows = rows;
		this.cols = cols;
		
		reset();
	}
	
	public RBMKRenderHelper(Canvas canvas, GraphicsContext graphics, int rows, int cols)
	{
		LOGGER.debug("Linked renderer constructed");
		this.canvas = canvas;
		this.graphics = graphics;
		
		renderQueue = new ArrayDeque<>(rows * cols);
		
		this.rows = rows;
		this.cols = cols;
		
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
		if (selectedLocation != null)
			drawSelectionRect(selectedLocation, graphics, zoom);
	}
	
	public void renderColumn(@NotNull RBMKColumnBase column)
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
	
	@SuppressWarnings("SuspiciousNameCombination")
	public static void basicRender(ColumnType type, GridLocation location, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Generic render handler requested for type {} at {}", type, location);
		renderEdges(location, graphics, zoom);
		renderCenter(location, graphics, zoom);
		graphics.setLineWidth(LINE_WIDTH);
		switch (type)
		{
			case ABSORBER:
				graphics.setFill(BORON);
				drawRect(location, LINE_WIDTH * 2, LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 4, CELL_SIZE - LINE_WIDTH * 4, graphics, zoom);
				break;
			case BOILER:
				graphics.setFill(Color.BLACK);
				drawRect(location, LINE_WIDTH, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setFill(BOILER_FILL);
				drawRect(location, LINE_WIDTH + 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case BREEDER:
			case OUTGASSER:
				graphics.setFill(BREEDER);
				drawRect(location, LINE_WIDTH, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setFill(TRITIUM);
				drawRect(location, LINE_WIDTH + 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case CONTROL_AUTO:// Pass through expected
				graphics.setStroke(Color.RED);
				graphics.setLineWidth(LINE_WIDTH * zoom);
				drawLine(location, LINE_WIDTH * 3, LINE_WIDTH, LINE_WIDTH * 3, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				drawLine(location, CELL_SIZE - LINE_WIDTH * 4, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 4, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
			case CONTROL:
				graphics.setStroke(BORON);
				graphics.setLineWidth(LINE_WIDTH * zoom);
				drawLine(location, LINE_WIDTH, LINE_WIDTH, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				drawLine(location, CELL_SIZE - LINE_WIDTH * 2, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setStroke(LINE_COLOR);
				drawLine(location, LINE_WIDTH * 2, LINE_WIDTH, LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				drawLine(location, CELL_SIZE - LINE_WIDTH * 3, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 3, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setFill(Color.BLACK);
				drawRect(location, LINE_WIDTH + 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case COOLER:
				graphics.setFill(COOL);
				drawRect(location, LINE_WIDTH + 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				drawRect(location, LINE_WIDTH, LINE_WIDTH + 6, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 8, graphics, zoom);
				break;
			case FUEL:
			case FUEL_SIM:
				graphics.setFill(FUEL_OUTLINE);
				drawRect(location, 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 6, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setFill(Color.BLACK);
				drawRect(location, 8, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case MODERATOR:
				graphics.setFill(GRAPHITE);
				drawRect(location, LINE_WIDTH * 2, LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 4, CELL_SIZE - LINE_WIDTH * 4, graphics, zoom);
				break;
			case HEATEX:
				graphics.setFill(Color.BLACK);
				drawRect(location, LINE_WIDTH, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				graphics.setFill(COOL);
				drawRect(location, LINE_WIDTH + 6, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 8, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
				break;
			case REFLECTOR:
				graphics.setFill(LINE_COLOR);
				drawRect(location, LINE_WIDTH * 2, LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 4, CELL_SIZE - LINE_WIDTH * 4, graphics, zoom);
				break;
			case STORAGE:
				graphics.setFill(Color.BLACK.brighter());
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
	
	public static void eraseColumn(GridLocation location, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Erasing {}", location);
		graphics.setFill(BACKGROUND_COLOR);
		drawRect(location, 0, 0, CELL_SIZE, CELL_SIZE, graphics, zoom);
	}
	
	public static void renderCenter(GridLocation location, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Rendering generic cell background at {} with zoom {}...", graphics, zoom);
		graphics.setFill(CELL_COLOR);
		//noinspection SuspiciousNameCombination
		drawRect(location, LINE_WIDTH, LINE_WIDTH, CELL_SIZE - LINE_WIDTH * 2, CELL_SIZE - LINE_WIDTH * 2, graphics, zoom);
	}
	
	public static void renderEdges(GridLocation location, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Rendering generic cell edges at {} with zoom {}...", location, zoom);
		graphics.setFill(LINE_COLOR);
		drawRect(location, 0, 0, CELL_SIZE, CELL_SIZE, graphics, zoom);
	}
	
	public static void drawLine(GridLocation location, double x1, double y1, double x2, double y2, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Drawing line at {}, starting at offset [x={}, y={}], ending at offset [x={}, y={}], and with zoom {}", location, x1, y1, x2, y2, zoom);
		graphics.strokeLine(
				(location.x() * CELL_SIZE + x1 + 1) * zoom,
				(location.y() * CELL_SIZE + y1 + 1) * zoom,
				(location.x() * CELL_SIZE + x2 + 1) * zoom,
				(location.y() * CELL_SIZE + y2 + 1) * zoom);
	}
	
	public static void drawRect(GridLocation location, double x, double y, double w, double h, GraphicsContext graphics, double zoom)
	{
		LOGGER.trace("Drawing rectangle at {}, with offset [x={}, y={}], width {} and height {}, with zoom {}", location, x, y, w, h, zoom);
		graphics.fillRect(
				(location.x() * CELL_SIZE + x) * zoom,
				(location.y() * CELL_SIZE + y) * zoom,
				w * zoom,
				h * zoom);
	}
	
	public static void drawSelectionRect(GridLocation location, GraphicsContext graphics, double zoom)
	{
		graphics.setLineWidth(LINE_WIDTH * 2);
		graphics.setStroke(Color.WHITE);
		graphics.strokeRect(
				location.x() * CELL_SIZE * zoom,
				location.y() * CELL_SIZE * zoom,
				CELL_SIZE * zoom,
				CELL_SIZE * zoom);
	}
	
	private static Color colorFromHex(int rgb)
	{
		final double max = 255;
		return new Color(((rgb >>> 16) & 0xff) / max, ((rgb >>> 8) & 0xff) / max, (rgb & 0xff) / max, 1);
	}
}
