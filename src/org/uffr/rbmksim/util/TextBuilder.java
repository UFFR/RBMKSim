package org.uffr.rbmksim.util;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TextBuilder
{
	public enum FontStyle
	{
		NORMAL("-fx-font-style: normal;"),
		ITALIC("-fx-font-style: italic;"),
		OBLIQUE("-fx-font-style: oblique;"),
		BOLD("-fx-font-weight: bold;");

		public final String id;
		private FontStyle(String id)
		{
			this.id = id;
		}
	}

	private final Text text;
	public TextBuilder(String defaultText)
	{
		text = new Text(defaultText);
	}
	
	public TextBuilder()
	{
		text = new Text();
	}
	
	public TextBuilder setText(String value)
	{
		text.setText(value);
		return this;
	}
	
	public TextBuilder setStyle(FontStyle style)
	{
		text.setStyle(style.id);
		return this;
	}
	
	public TextBuilder setFill(Paint color)
	{
		text.setFill(color);
		return this;
	}
	
	public TextBuilder setStroke(Paint color)
	{
		text.setStroke(color);
		return this;
	}
	
	public TextBuilder setFont(Font font)
	{
		text.setFont(font);
		return this;
	}
	
	public TextBuilder setUnderline(boolean value)
	{
		text.setUnderline(value);
		return this;
	}
	
	public TextBuilder setStrikethrough(boolean value)
	{
		text.setStrikethrough(value);
		return this;
	}
	
	public Text getText()
	{
		return text;
	}
}
