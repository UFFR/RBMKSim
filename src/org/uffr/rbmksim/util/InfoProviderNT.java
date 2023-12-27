package org.uffr.rbmksim.util;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.text.Text;

@FunctionalInterface
public interface InfoProviderNT
{
	public void addInformation(List<Text> info);
	
	public default List<Text> getText()
	{
		final ArrayList<Text> list = new ArrayList<>();
		addInformation(list);
		return list;
	}
	
	public static Text getNewline()
	{
		return new Text("\n");
	}
}
