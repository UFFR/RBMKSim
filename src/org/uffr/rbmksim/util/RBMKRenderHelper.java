package org.uffr.rbmksim.util;

import static org.uffr.rbmksim.simulation.RBMKColumnBase.CELL_SIZE;
import static org.uffr.rbmksim.simulation.RBMKColumnBase.LINE_WIDTH;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

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
	public static final Color BACKGROUND_COLOR = Color.DARKGRAY;
	private final Canvas canvas;
	private final GraphicsContext graphics;
	private final Queue<Consumer<GraphicsContext>> renderQueue = new ArrayDeque<>();
	private final int rows, cols;
	
	public RBMKRenderHelper(int rows, int cols)
	{
		canvas = new Canvas(cols, rows);
		
		graphics = canvas.getGraphicsContext2D();
		
		this.rows = rows;
		this.cols = cols;
		
		reset();
	}
	
	public RBMKRenderHelper(Canvas canvas, GraphicsContext graphics)
	{
		this.canvas = canvas;
		this.graphics = graphics;
		
		rows = (int) canvas.getHeight();
		cols = (int) canvas.getWidth();
		
		reset();
	}
	
	public void reset()
	{
		graphics.setFill(BACKGROUND_COLOR);
		graphics.fill();
	}
	
	public void flush()
	{
		while (!renderQueue.isEmpty())
			renderQueue.poll().accept(graphics);
	}
	
	public void renderColumn(RBMKColumnBase column)
	{
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
	
	public static void genericRender(ColumnType type, GridLocation location, GraphicsContext graphics)
	{
		RBMKColumnBase.renderBackground(location, graphics);
		graphics.setLineWidth(LINE_WIDTH);
		switch (type)
		{
			case ABSORBER:
				graphics.setFill(Color.DARKGRAY);
				graphics.fillRect(location.getX() + LINE_WIDTH, location.getY() + LINE_WIDTH, location.getX() + CELL_SIZE - LINE_WIDTH, location.getY() + CELL_SIZE - LINE_WIDTH);
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
		RBMKColumnBase.renderEdges(location, graphics);
	}
}
