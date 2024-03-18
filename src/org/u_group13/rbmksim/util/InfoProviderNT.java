package org.u_group13.rbmksim.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface InfoProviderNT
{
	Supplier<Text> NEW_LINE_SUPPLIER = InfoProviderNT::getNewline;

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
	
	@Contract(" -> new")
	static @NotNull Text getNewline()
	{
		return new Text("\n");
	}
}
