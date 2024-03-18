package org.u_group13.rbmksim.main.dialog;

import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.u_group13.rbmksim.simulation.RBMKColumnBase;
import org.u_group13.rbmksim.util.Localizable;

import javax.annotation.Nullable;

public abstract class ColumnSidebarPaneBase
{
	protected final AnchorPane anchorPane;
	protected final Pane pane = new Pane();
	@Localizable
	protected final Button resetButton = new Button();
	protected RBMKColumnBase currentColumn;

	public ColumnSidebarPaneBase(AnchorPane anchorPane)
	{
		this.anchorPane = anchorPane;
		setupNodes();
	}

	public void setCurrentColumn(@Nullable RBMKColumnBase column)
	{
		currentColumn = column;
	}

	public void activate()
	{
		if (!anchorPane.getChildren().contains(pane))
			anchorPane.getChildren().add(pane);
	}

	public void deactivate()
	{
		anchorPane.getChildren().remove(pane);
	}

	protected void setupNodes()
	{
		resetButton.setCancelButton(true);

		pane.getChildren().add(resetButton);
	}

}
