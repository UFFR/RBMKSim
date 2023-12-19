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
import javafx.scene.layout.Pane;

public class MainController implements Initializable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
	public static final Set<String> ALLOWED_PROPERTIES = ImmutableSet.of("text", "promptText", "tooltip", "accessibleText");
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
		for (Field field : getClass().getDeclaredFields())
		{
			LOGGER.trace("Trying field [{}]", field);
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
				final ArrayList<Method> setterMethods = new ArrayList<>();
				for (Method method : item.getClass().getMethods())
					if (method.getName().startsWith("set"))
						setterMethods.add(method);
				
				for (Method method : setterMethods)
				{
					LOGGER.trace("Trying method [{}]", method);
					final String property = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
					LOGGER.trace("Got assumed property [{}]", property);
					if (!ALLOWED_PROPERTIES.contains(property))
						continue;
					final String unlocalized = "app." + field.getName() + '.' + property;
					LOGGER.trace("Trying: [{}]", unlocalized);
					if (I18n.hasKey(unlocalized))
					{
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
		LOGGER.debug("MainController.onFrameChanged() triggered");
		currentFrame = Main.getFrame();
		nameTextField.setText(currentFrame.isPresent() ? currentFrame.get().getName() : "");
		creatorTextField.setText(currentFrame.isPresent() ? currentFrame.get().getCreatorName() : "");
		versionTextField.setText(currentFrame.isPresent() ? currentFrame.get().getVersion() : "");
		dateInput.setValue(currentFrame.isPresent() ? currentFrame.get().getDate() : null);
		
		currentFrame.get().render();
	}
	
	@FXML
	private void onClickNewBlueprint()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickNewBlueprint() triggered");
		
		Main.setFrame(new RBMKBlueprint(mainCanvas));
		
		// TODO Remove
		final RBMKFrame frame = Main.getFrame().get();
		LOGGER.debug("Added col 1? {}", frame.addColumn(new RBMKBlueprintColumn(new GridLocation(0, 0), frame, ColumnType.BLANK, false)));
		LOGGER.debug("Added col 2? {}", frame.addColumn(new RBMKBlueprintColumn(new GridLocation(0, 10), frame, ColumnType.BLANK, false)));
		LOGGER.debug("Added col 3? {}", frame.addColumn(new RBMKBlueprintColumn(new GridLocation(10, 0), frame, ColumnType.BLANK, false)));
		LOGGER.debug("Added col 4? {}", frame.addColumn(new RBMKBlueprintColumn(new GridLocation(10, 10), frame, ColumnType.BLANK, false)));
		
		onFrameChanged();
	}
	
	@FXML
	private void onClickNewSimulation()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickNewSimulation() triggered");
		onFrameChanged();
	}
	
	@FXML
	private void onClickOpen()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickOpen() triggered");
		onFrameChanged();
	}
	
	@FXML
	private void onClickClose()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickClose() triggered");
		Main.closeFrame();
		currentFrame = Optional.empty();
		onFrameChanged();
	}
	
	@FXML
	private void onClickSave()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickSave() triggered");
	}
	
	@FXML
	private void onClickSaveAs()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickSaveAs() triggered");
	}
	
	@FXML
	private void onClickRevert()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickRevert() triggered");
	}
	
	@FXML
	private void onClickPreferences()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickPreferences() triggered");
	}
	
	@SuppressWarnings("static-method")
	@FXML
	private void onClickAbout()
	{
		LOGGER.debug("MainController.onClickAbout() triggered");
		Main.openDialog(I18n.resolve("app.title.about"), I18n.resolve("app.title"), Main.getAboutString(), AlertType.INFORMATION);
	}
	
	@SuppressWarnings("static-method")
	@FXML
	private void onClickCredits()
	{
		LOGGER.debug("MainController.onClickCredits() triggered");
		Main.openDialog(I18n.resolve("app.title.credits"), I18n.resolve("app.header.credits"), Main.getCreditsString(), AlertType.INFORMATION);
	}
	
	@FXML
	private void onClickHelp()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickHelp() triggered");
	}
	
	@SuppressWarnings("static-method")
	@FXML
	private void onClickQuit()
	{
		LOGGER.debug("MainController.onClickQuit() triggered");
		LOGGER.info("Exiting...");
		Platform.exit();
	}
	
	@FXML
	private void onNameTextFieldTextChanged()
	{
		if (currentFrame.isPresent())
			currentFrame.get().setName(nameTextField.getText());
	}
	
	@FXML
	private void onCreatorNameTextChanged()
	{
		if (currentFrame.isPresent())
			currentFrame.get().setCreatorName(creatorTextField.getText());
	}
	
	@FXML
	private void onVersionTextChanged()
	{
		if (currentFrame.isPresent())
			currentFrame.get().setVersion(versionTextField.getText());
	}
	
	@FXML
	private void onChangeDateInput()
	{
		if (currentFrame.isPresent())
			currentFrame.get().setDate(dateInput.getValue());
	}
	
	public void setInfoArea(InfoProvider infoProvider)
	{
		LOGGER.debug("Set infoTextArea with data provided by InfoProvider instance");
		infoTextArea.setText(infoProvider.asProperString());
	}
	
}
