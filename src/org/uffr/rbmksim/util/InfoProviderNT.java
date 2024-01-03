package org.uffr.rbmksim.util;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

@FunctionalInterface
public interface InfoProviderNT
{
	void addInformation(List<Text> info);
	
	default List<Text> getText()
	{
		final ArrayList<Text> list = new ArrayList<>();
		addInformation(list);
		list.forEach(text ->
        {
			if (Color.BLACK.equals(text.getFill()))
				text.setFill(Color.YELLOW);
		});
		return list;
	}
	
	static Text getNewline()
	{
		return new Text("\n");
	}
}
