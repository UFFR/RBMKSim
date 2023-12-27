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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.simulation.ColumnType;
import org.uffr.rbmksim.simulation.GridLocation;
import org.uffr.rbmksim.simulation.RBMKColumnBase;
import org.uffr.rbmksim.simulation.bcolumns.RBMKBlueprintColumn;
import org.uffr.rbmksim.util.I18n;
import org.uffr.rbmksim.util.InfoProviderNT;
import org.uffr.rbmksim.util.RBMKRenderHelper;
import org.uffr.uffrlib.misc.Version;

import com.google.common.collect.ImmutableSet;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.TextFlow;
import javafx.util.StringConverter;

public class MainController implements Initializable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
	public static final Set<String> ALLOWED_PROPERTIES = ImmutableSet.of("text", "promptText", "accessibleText");
	// From https://semver.org/
	private static final String SEMVER_REGEX = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";
	private static final Pattern SEMVER_PATTERN = Pattern.compile(SEMVER_REGEX);
	private static final double SCROLL_FACTOR = 40, DEFAULT_ZOOM = 0.25, MAX_ZOOM = 5, MIN_ZOOM = 0.25;
	private RBMKFrame currentFrame = null;
	@FXML
	private TextFlow infoTextArea;
//	private TextArea infoTextArea;
	@FXML
	private TextField nameTextField, creatorTextField, versionTextField, zoomTextField;
	@FXML
	private DatePicker dateInput;
	@FXML
	private Tab mainViewTab, graphsTab, costEstimatorTab;
	@FXML
	private TitledPane frameInfoPane, frameOptionsPane, columnOptionsPane, controlOptionsPane;
	@FXML
	private ChoiceBox<GraphType> graphSelectionBox;
	@FXML
	private Pane canvasPane;
	@FXML
	private AnchorPane canvasAnchor;
	@FXML
	private Canvas mainCanvas;
	@FXML
	private Label nameLabel, creatorLabel, versionLabel, dateLabel, zoomLabel;
	@FXML
	private Tooltip nameTooltip, creatorNameTooltip, versionTooltip, dateTooltip;
	
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
					LOGGER.trace("Checking if [{}] has I18n entry...", unlocalized);
					if (I18n.hasKey(unlocalized))
					{
						LOGGER.trace("Trying: [{}]...", unlocalized);
						try
						{
							method.invoke(item, I18n.resolve(unlocalized));
						} catch (IllegalArgumentException | InvocationTargetException | SecurityException e)
						{
							LOGGER.warn("Either assumed property controlled by setter method [" + method.getName() + "] has entry in lang file but does not have associated assumed getter method or method is not a string", e);
						}
					}
				}
			
			} catch (IllegalArgumentException | IllegalAccessException e)
			{
				LOGGER.error("Unable to retrieve field!", e);
			}
		}
		LOGGER.trace("I18n complete");

		LOGGER.trace("Setting up miscellaneous fields...");
		
		mainCanvas.heightProperty().bind(canvasPane.heightProperty());
		mainCanvas.widthProperty().bind(canvasPane.widthProperty());
		
		for (GraphType type : GraphType.values())
			graphSelectionBox.getItems().add(type);
		graphSelectionBox.selectionModelProperty().get().selectFirst();
		
		infoTextArea.setStyle("-fx-font-family: monospace; -fx-background-color: DIMGRAY; -fx-text-fill: WHITE;");

		versionTextField.setTextFormatter(new TextFormatter<>(new StringConverter<Version>()
		{
			@Override
			public Version fromString(String arg0)
			{
				final String major, minor, patch, suffix, metadata;
				final Matcher matcher = SEMVER_PATTERN.matcher(arg0);
				
				if (!matcher.find())
					return null;
				
				major = matcher.group(1);
				minor = matcher.group(2);
				patch = matcher.group(3);
				suffix = matcher.group(4);
				metadata = matcher.group(5);
				
				return new Version(Integer.parseUnsignedInt(major), Integer.parseUnsignedInt(minor), Integer.parseUnsignedInt(patch), suffix, metadata);
			}

			@Override
			public String toString(Version arg0)
			{
				return arg0 == null ? "1.0.0" : arg0.toString();
			}
		}));
		
		LOGGER.debug("Initialization complete");
	}
	
	private void onFrameChanged()
	{
		LOGGER.debug("onFrameChanged() triggered");
		currentFrame = Main.getFrame().orElse(null);
		RBMKRenderHelper.clearCanvas(mainCanvas.getGraphicsContext2D(), mainCanvas);
		RBMKRenderHelper.renderBackground(mainCanvas.getGraphicsContext2D(), mainCanvas);
		RBMKColumnBase.setCurrentFrame(currentFrame);
		if (currentFrame == null)
		{
			nameTextField.setText("");
			creatorTextField.setText("");
			versionTextField.setText("");
			dateInput.setValue(null);
			canvasAnchor.setPrefWidth(Region.USE_COMPUTED_SIZE);
			canvasAnchor.setPrefHeight(Region.USE_COMPUTED_SIZE);
		} else
		{
			nameTextField.setText(currentFrame.getName());
			creatorTextField.setText(currentFrame.getCreatorName());
			versionTextField.setText(currentFrame.getVersion());
			dateInput.setValue(currentFrame.getDate());
			canvasAnchor.setPrefWidth(currentFrame.columns * RBMKRenderHelper.CELL_SIZE);
			canvasAnchor.setPrefHeight(currentFrame.rows * RBMKRenderHelper.CELL_SIZE);
			currentFrame.render();
		}
	}
	
	@FXML
	private void onClickNewBlueprint()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickNewBlueprint() triggered");
		
		Main.setFrame(new RBMKBlueprint(mainCanvas));
		
		// TODO Remove
		final ColumnType testType = ColumnType.BLANK;
		final RBMKFrame frame = Main.getFrame().get();
		frame.addColumn(new RBMKBlueprintColumn(new GridLocation(0, 0), frame, testType, false));
		frame.addColumn(new RBMKBlueprintColumn(new GridLocation(0, 10), frame, testType, false));
		frame.addColumn(new RBMKBlueprintColumn(new GridLocation(10, 0), frame, testType, false));
		frame.addColumn(new RBMKBlueprintColumn(new GridLocation(10, 10), frame, testType, false));
		
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
		LOGGER.trace("tryZoom(double) triggered");
		if (currentFrame != null)
		{
			LOGGER.trace("Changing frame render zoom level by: {}", amount);
			currentFrame.renderer.zoom += amount;
			currentFrame.renderer.zoom = clampZoom(currentFrame.renderer.zoom);
			zoomTextField.setText(String.valueOf(currentFrame.renderer.zoom * 100));
			currentFrame.render();
		}
	}
	
	private void trySetZoom(double amount)
	{
		LOGGER.trace("trySetZoom(double) triggered");
		if (currentFrame != null)
		{
			LOGGER.trace("Changing frame render zoom level to: {}", amount);
			currentFrame.renderer.zoom = clampZoom(amount);
			zoomTextField.setText(String.valueOf(currentFrame.renderer.zoom * 100));
			currentFrame.render();
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
		if (currentFrame != null)
			trySetZoom(1);
	}
	
	@FXML
	private void onCanvasClicked(MouseEvent event)
	{
		LOGGER.trace("onCanvasClicked(MouseEvent) triggered at coordinate [x={}, y={}] using button {}", event.getX(), event.getY(), event.getButton());
		final int x = (int) event.getX() / RBMKRenderHelper.CELL_SIZE, y = (int) event.getY() / RBMKRenderHelper.CELL_SIZE;
		if (currentFrame != null)
		{
			final GridLocation loc = new GridLocation(x, y);
			final RBMKColumnBase column = currentFrame.getColumnAtCoords(loc);
			currentFrame.setSelectedLocation(Optional.of(loc));
			if (column != null && currentFrame.getSelectedLocation().isPresent())
				setInfoArea(column);
			else
				infoTextArea.getChildren().clear();
			currentFrame.render();
		}
	}
	
	@FXML
	private void onCanvasScroll(ScrollEvent event)
	{
		LOGGER.trace("onCanvasScroll(ScrollEvent) triggered with [\u0394x={}, \u0394y={}]", event.getDeltaX(), event.getDeltaY());
		if (event.isControlDown())
			tryZoom((event.getDeltaY() / SCROLL_FACTOR) * DEFAULT_ZOOM);
		
		// TODO Regular scrolling
	}
	
	@FXML
	private void onCanvasZoom(ZoomEvent event)
	{
		LOGGER.trace("onCanvasZoom(ZoomEvent) triggered with factor {}", event.getZoomFactor());
		tryZoom(event.getZoomFactor() - 1);
	}
	
	@FXML
	private void onNameTextFieldTextChanged(KeyEvent event)
	{
		LOGGER.trace("onNameTextFieldTextChanged() triggered");
		if (currentFrame != null && event.getCode() == KeyCode.ENTER)
			currentFrame.setName(nameTextField.getText());
	}
	
	@FXML
	private void onCreatorNameTextChanged(KeyEvent event)
	{
		LOGGER.trace("onCreatorNameTextChanged() triggered");
		if (currentFrame != null && event.getCode() == KeyCode.ENTER)
			currentFrame.setCreatorName(creatorTextField.getText());
	}
	
	@FXML
	private void onVersionTextChanged(KeyEvent event)
	{
		LOGGER.trace("onVersionTextChanged() triggered");
		if (currentFrame != null && event.getCode() == KeyCode.ENTER)
			currentFrame.setVersion(versionTextField.getText());
	}
	
	@FXML
	private void onChangeDateInput()
	{
		LOGGER.trace("onChangeDateInput() triggered");
		if (currentFrame != null)
			currentFrame.setDate(dateInput.getValue());
	}
	
	@FXML
	private void onZoomLevelTextChanged(KeyEvent event)
	{
		LOGGER.trace("onZoomLevelTextChanged() triggered with code: {}", event.getCode());
		if (currentFrame != null && event.getCode() == KeyCode.ENTER)
		{
			double newZoom = currentFrame.renderer.zoom;
			try
			{
				LOGGER.trace("Attempting to parse double...");
				newZoom = clampZoom(Double.parseDouble(zoomTextField.getText()) / 100);
				trySetZoom(newZoom);
			} catch (NumberFormatException e)
			{
				LOGGER.warn("Caught exception with message: \"{}\" trying to parse double", e.getMessage());
				zoomTextField.setText(String.valueOf(currentFrame.renderer.zoom * 100));
			}
		}
	}
	
	public void setInfoArea(@Nullable InfoProviderNT infoProvider)
	{
		LOGGER.debug("Set infoTextArea with data provided by InfoProvider instance");
		infoTextArea.getChildren().clear();
		if (infoProvider != null)
			infoTextArea.getChildren().addAll(infoProvider.getText());
		
//		infoTextArea.getChildren().add(new Text(infoProvider.asProperString()));
//		infoTextArea.setText(infoProvider.asProperString());
	}
	
	private static double clampZoom(double amount)
	{
		LOGGER.trace("Clamping zoom amount...");
		return amount > MAX_ZOOM ? MAX_ZOOM : amount < MIN_ZOOM ? MIN_ZOOM : amount;
	}
	
}
