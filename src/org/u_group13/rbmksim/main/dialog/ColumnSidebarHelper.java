package org.u_group13.rbmksim.main.dialog;

import javafx.scene.layout.AnchorPane;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.RBMKColumnBase;

import java.util.EnumMap;

public class ColumnSidebarHelper
{
	private final EnumMap<ColumnType, ColumnSidebarPaneBase> paneMap = new EnumMap<>(ColumnType.class);
	private final ColumnSidebarGeneric sidebarGeneric;
	private final AnchorPane anchorPane;

	public ColumnSidebarHelper(AnchorPane anchorPane)
	{
		this.anchorPane = anchorPane;
		this.sidebarGeneric = new ColumnSidebarGeneric(anchorPane);
		initialize();
	}

	private void initialize()
	{
		// TODO
	}

	private void setup(RBMKColumnBase column)
	{
		paneMap.getOrDefault(column.getColumnType(), sidebarGeneric);
	}
}
