package org.uffr.rbmksim.main;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.bcolumns.RBMKBlueprintColumn;
import org.uffr.rbmksim.util.I18n;
import org.uffr.rbmksim.util.InfoProvider;
import org.uffr.rbmksim.util.RBMKRenderHelper;

import com.google.common.collect.ImmutableSet;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;

public class MainController implements Initializable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
	public static final Set<String> ALLOWED_PROPERTIES = ImmutableSet.of("text", "promptText", "tooltip", "accessibleText");
	private static final double SCROLL_FACTOR = 40, DEFAULT_ZOOM = 0.25;
	private Optional<RBMKFrame> currentFrame = Optional.empty();
	@FXML
	private TextArea infoTextArea;
	@FXML
	private TextField nameTextField, creatorTextField, versionTextField;
	@FXML
	private DatePicker dateInput;
	@FXML
	private Tab mainViewTab, graphsTab, costEstimatorTab;
	@FXML
	private TitledPane frameInfoPane, frameOptionsPane, columnOptionsPane;
	@FXML
	private ChoiceBox<GraphType> graphSelectionBox;
	@FXML
	private Pane canvasPane;
	@FXML
	private Canvas mainCanvas;
	
	private enum GraphType
	{
		HEAT("heat"),
		FLUX("flux"),
		STEAM("steam"),
		POWER("power"),
		COOLANT("coolant");
		
		public final String key;
		private GraphType(String key)
		{
			this.key = key;
		}
		
		@Override
		public String toString()
		{
			return I18n.resolve("app.graphType." + key);
		}
	}
	
	@Override
	public void initialize(URL url, ResourceBundle bundle)
	{
		LOGGER.debug("Initializing main window...");
		LOGGER.trace("Setting up I18n for window...");
		for (Field field : getClass().getDeclaredFields())
		{
			LOGGER.trace("Trying field [{}]...", field);
			try
			{
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				
				LOGGER.trace("Field [{}] passed modifier check", field);
				
				final Object item = field.get(this);
				if (item == null)
					continue;
				
//				if (!(item instanceof Node))
//					continue;
				
//				LOGGER.trace("Field [" + field + "] passed class check");
				
				/*final Node node = (Node) object;
				
				for (Object key : node.getProperties().keySet())
				{
					LOGGER.trace("Testing key [" + key + ']');
					if (key instanceof String)
					{
						LOGGER.trace("Key [" + key + "] is a string");
						if (I18n.hasKey("app." + field.getName() + '.' + (String) key))
							node.getProperties().put(key, I18n.resolve((String) key));
					}
				}*/
				
//				final Object item = field.get(this);
				LOGGER.trace("Accumulating field methods...");
				final ArrayList<Method> setterMethods = new ArrayList<>();
				for (Method method : item.getClass().getMethods())
					if (method.getName().startsWith("set"))
						setterMethods.add(method);
				
				for (Method method : setterMethods)
				{
					LOGGER.trace("Trying method [{}]...", method);
					final String property = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
					LOGGER.trace("Got assumed property [{}]", property);
					if (!ALLOWED_PROPERTIES.contains(property))
						continue;
					final String unlocalized = "app." + field.getName() + '.' + property;
					LOGGER.trace("Checking if field has I18n entry...");
					if (I18n.hasKey(unlocalized))
					{
						LOGGER.trace("Trying: [{}]...", unlocalized);
						try
						{
							method.invoke(item, I18n.resolve(unlocalized));
						} catch (InvocationTargetException | SecurityException e)
						{
							LOGGER.debug("Assumed property controlled by setter method [" + method.getName() + "] has entry in lang file but does not have associated assumed getter method", e);
						}
					}
				}
			
			} catch (IllegalArgumentException | IllegalAccessException e)
			{
				LOGGER.error("Unable to retrieve field!", e);
			}
		}
		LOGGER.trace("I18n complete");
		
		mainCanvas.heightProperty().bind(canvasPane.heightProperty());
		mainCanvas.widthProperty().bind(canvasPane.widthProperty());
		
		LOGGER.trace("Setting up default values...");
		for (GraphType type : GraphType.values())
			graphSelectionBox.getItems().add(type);
		graphSelectionBox.selectionModelProperty().get().selectFirst();
		
		LOGGER.debug("Initialization complete");
	}
	
	private void onFrameChanged()
	{
		LOGGER.debug("onFrameChanged() triggered");
		currentFrame = Main.getFrame();
		nameTextField.setText(currentFrame.isPresent() ? currentFrame.get().getName() : "");
		creatorTextField.setText(currentFrame.isPresent() ? currentFrame.get().getCreatorName() : "");
		versionTextField.setText(currentFrame.isPresent() ? currentFrame.get().getVersion() : "");
		dateInput.setValue(currentFrame.isPresent() ? currentFrame.get().getDate() : null);
		RBMKRenderHelper.renderBackground(mainCanvas.getGraphicsContext2D(), mainCanvas);
		currentFrame.get().render();
	}
	
	@FXML
	private void onClickNewBlueprint()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickNewBlueprint() triggered");
		
		Main.setFrame(new RBMKBlueprint(mainCanvas));
		
		// TODO Remove
		final RBMKFrame frame = Main.getFrame().get();
		frame.addColumn(new RBMKBlueprintColumn(new GridLocation(0, 0), frame, ColumnType.BLANK, false));
		frame.addColumn(new RBMKBlueprintColumn(new GridLocation(0, 10), frame, ColumnType.BLANK, false));
		frame.addColumn(new RBMKBlueprintColumn(new GridLocation(10, 0), frame, ColumnType.BLANK, false));
		frame.addColumn(new RBMKBlueprintColumn(new GridLocation(10, 10), frame, ColumnType.BLANK, false));
		
		onFrameChanged();
	}
	
	@FXML
	private void onClickNewSimulation()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickNewSimulation() triggered");
		onFrameChanged();
	}
	
	@FXML
	private void onClickOpen()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickOpen() triggered");
		onFrameChanged();
	}
	
	@FXML
	private void onClickClose()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickClose() triggered");
		Main.closeFrame();
		currentFrame = Optional.empty();
		onFrameChanged();
	}
	
	@FXML
	private void onClickSave()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickSave() triggered");
	}
	
	@FXML
	private void onClickSaveAs()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickSaveAs() triggered");
	}
	
	@FXML
	private void onClickRevert()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickRevert() triggered");
	}
	
	@FXML
	private void onClickPreferences()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickPreferences() triggered");
	}
	
	@SuppressWarnings("static-method")
	@FXML
	private void onClickAbout()
	{
		LOGGER.debug("onClickAbout() triggered");
		Main.openDialog(I18n.resolve("app.title.about"), I18n.resolve("app.title"), Main.getAboutString(), AlertType.INFORMATION);
	}
	
	@SuppressWarnings("static-method")
	@FXML
	private void onClickCredits()
	{
		LOGGER.debug("onClickCredits() triggered");
		Main.openDialog(I18n.resolve("app.title.credits"), I18n.resolve("app.header.credits"), Main.getCreditsString(), AlertType.INFORMATION);
	}
	
	@FXML
	private void onClickHelp()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickHelp() triggered");
	}
	
	@SuppressWarnings("static-method")
	@FXML
	private void onClickQuit()
	{
		LOGGER.debug("onClickQuit() triggered");
		LOGGER.info("Exiting...");
		Platform.exit();
	}
	
	private void tryZoom(double amount)
	{
		if (currentFrame.isPresent())
		{
			LOGGER.trace("Changing frame render zoom level by: {}", amount);
			currentFrame.get().zoom += amount;
			currentFrame.get().render();
		}
	}
	
	@FXML
	private void onClickZoomIn()
	{
		LOGGER.trace("onClickZoomIn() triggered");
		tryZoom(DEFAULT_ZOOM);
	}
	
	@FXML
	private void onClickZoomOut()
	{
		LOGGER.trace("onClickZoomOut() triggered");
		tryZoom(-DEFAULT_ZOOM);
	}
	
	@FXML
	private void onClickZoomReset()
	{
		LOGGER.trace("onClickZoomReset() triggered");
		if (currentFrame.isPresent())
		{
			currentFrame.get().zoom = 1;
			currentFrame.get().render();
		}
	}
	
	@FXML
	private void onCanvasScroll(ScrollEvent event)
	{
		LOGGER.trace("onCanvasScroll(ScrollEvent) triggered");
		if (event.isControlDown())
			tryZoom((event.getDeltaY() / SCROLL_FACTOR) * DEFAULT_ZOOM);
		
		// TODO Regular scrolling
	}
	
	@FXML
	private void onNameTextFieldTextChanged()
	{
		LOGGER.trace("onNameTextFieldTextChanged() triggered");
		if (currentFrame.isPresent())
			currentFrame.get().setName(nameTextField.getText());
	}
	
	@FXML
	private void onCreatorNameTextChanged()
	{
		LOGGER.trace("onCreatorNameTextChanged() triggered");
		if (currentFrame.isPresent())
			currentFrame.get().setCreatorName(creatorTextField.getText());
	}
	
	@FXML
	private void onVersionTextChanged()
	{
		LOGGER.trace("onVersionTextChanged() triggered");
		if (currentFrame.isPresent())
			currentFrame.get().setVersion(versionTextField.getText());
	}
	
	@FXML
	private void onChangeDateInput()
	{
		LOGGER.trace("onChangeDateInput() triggered");
		if (currentFrame.isPresent())
			currentFrame.get().setDate(dateInput.getValue());
	}
	
	public void setInfoArea(InfoProvider infoProvider)
	{
		LOGGER.debug("Set infoTextArea with data provided by InfoProvider instance");
		infoTextArea.setText(infoProvider.asProperString());
	}
	
}
