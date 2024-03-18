package org.u_group13.rbmksim.main;

import static org.u_group13.rbmksim.main.Main.config;

import com.google.common.collect.ImmutableSet;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.main.dialog.ColumnSidebarHelper;
import org.u_group13.rbmksim.simulation.ColumnType;
import org.u_group13.rbmksim.simulation.GridLocation;
import org.u_group13.rbmksim.simulation.RBMKColumnBase;
import org.u_group13.rbmksim.util.*;
import org.uffr.uffrlib.misc.Version;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

public class MainController implements Initializable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
	public static final Set<String> ALLOWED_PROPERTIES = ImmutableSet.of("text", "promptText", "accessibleText");
	// From https://semver.org/
	private static final String DECIMAL_REGEX = "^-?\\d*\\.?\\d*$";
	private static final double SCROLL_FACTOR = 40, ZOOM_FACTOR = 0.25, MAX_ZOOM = 5, MIN_ZOOM = 0.25;
	private static Path licensePath = null, lastPath = null;
	private static URL cachedProgramConfigFXML = null;
	static Stage programConfigWindow = null;
	private RBMKFrame currentFrame = null, lastSavedState = null;
	private ColumnSidebarHelper sidebarHelper;
	@FXML
	private TextFlow infoTextArea;
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
	private ChoiceBox<ColumnType> columnTypeBox;
	@FXML
	private Button setColumnButton, resetColumnButton, startPauseButton;
	@FXML
	private Pane canvasPane;
	@FXML
	private AnchorPane canvasAnchor, columnOptionsAnchor;
	@FXML
	private Canvas mainCanvas;
	@FXML
	private Label nameLabel, creatorLabel, versionLabel, dateLabel, zoomLabel;
	@FXML
	private Tooltip nameTooltip, creatorNameTooltip, versionTooltip, dateTooltip, setColumnButtonTooltip;
	@FXML
	private MenuItem helpHelpMenuItem, helpCreditsMenuItem, helpLicenseMenuItem, helpAboutMenuItem;

	@Override
	public void initialize(URL url, ResourceBundle bundle)
	{
		LOGGER.debug("Initializing main window...");
		LOGGER.trace("Setting up I18n for window...");
		for (Field field : getClass().getDeclaredFields())
		{
			if (!field.isAnnotationPresent(FXML.class))
				continue;

			LOGGER.trace("Trying field [{}]...", field);
			try
			{
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				
				LOGGER.trace("Field [{}] passed modifier check", field);
				
				final Object item = field.get(this);
				if (item == null)
					continue;

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
							LOGGER.warn("Either assumed property controlled by setter method [{}] has entry in lang file but does not have associated assumed getter method or method is not a string", method.getName());
							LOGGER.warn("Got exception:", e);
						}
					}
				}
			
			} catch (IllegalArgumentException | IllegalAccessException e)
			{
				LOGGER.error("Unable to retrieve field!", e);
			}
		}
		LOGGER.trace("I18n complete. Setting up miscellaneous fields...");

		Main.getStage().widthProperty().addListener((o, oldVal, newVal) ->
		                                       {if (currentFrame != null) currentFrame.render();});
		Main.getStage().heightProperty().addListener((o, oldVal, newVal) ->
		                                       {if (currentFrame != null) currentFrame.render();});
		Main.getStage().maximizedProperty().addListener(b -> {if (currentFrame != null) currentFrame.render();});
		Main.getStage().fullScreenProperty().addListener(observable -> {if (currentFrame != null) currentFrame.render();});

		mainCanvas.heightProperty().bind(canvasPane.heightProperty());
		mainCanvas.widthProperty().bind(canvasPane.widthProperty());
		canvasPane.minHeightProperty().bind(canvasAnchor.heightProperty());
		canvasPane.minWidthProperty().bind(canvasAnchor.widthProperty());
		
		graphSelectionBox.getItems().addAll(GraphType.values());
		graphSelectionBox.selectionModelProperty().get().selectFirst();
		columnTypeBox.getItems().addAll(ColumnType.values());
		columnTypeBox.selectionModelProperty().get().selectFirst();
		
		// The "-fx-text-fill" thing doesn't do anything, doesn't seem to work
		infoTextArea.setStyle("-fx-font-family: monospace; -fx-background-color: DIMGRAY; -fx-text-fill: WHITE;");
		infoTextArea.getChildren().add(new TextBuilder(I18n.resolve("app.infoTextArea.promptText"))
				                               .setColor(Color.YELLOW).getText());

		versionTextField.setTextFormatter(new TextFormatter<>(new StringConverter<Version>()
		{
			@Override
			public Version fromString(String inString)
			{
				return inString.matches(Version.SEMVER_REGEX) ? new Version(inString) : null;
			}

			@Override
			public String toString(Version version)
			{
				return version == null ? "1.0.0" : version.toString();
			}
		}));
		zoomTextField.setTextFormatter(new TextFormatter<>(u ->
		                                                   {
			                                                   u.setText(u.getControlNewText().matches(DECIMAL_REGEX)
					                                                             ? u.getControlNewText()
					                                                             : u.getControlText());
															   return u;
		                                                   }));

		sidebarHelper = new ColumnSidebarHelper(columnOptionsAnchor);

		LOGGER.debug("Initialization complete");
	}
	
	private void onFrameChanged()
	{
		LOGGER.debug("onFrameChanged() triggered");
		currentFrame = Main.getFrame().orElse(null);
		RBMKRenderHelper.clearCanvas(mainCanvas.getGraphicsContext2D(), mainCanvas);
		RBMKColumnBase.setCurrentFrame(currentFrame);
		if (currentFrame == null)
		{
			nameTextField.setText("");
			creatorTextField.setText("");
			versionTextField.setText("");
			dateInput.setValue(null);
			canvasAnchor.setPrefWidth(Region.USE_COMPUTED_SIZE);
			canvasAnchor.setPrefHeight(Region.USE_COMPUTED_SIZE);
			RBMKRenderHelper.clearCanvas(mainCanvas.getGraphicsContext2D(), mainCanvas);
		} else
		{
			currentFrame.setCanvas(mainCanvas);
			RBMKRenderHelper.renderBackground(mainCanvas.getGraphicsContext2D(), mainCanvas);
			nameTextField.setText(currentFrame.getName());
			creatorTextField.setText(currentFrame.getCreatorName());
			versionTextField.setText(currentFrame.getVersion().toString());
			dateInput.setValue(currentFrame.getDate());
			canvasAnchor.setPrefWidth(currentFrame.columns * RBMKRenderHelper.CELL_SIZE);
			canvasAnchor.setPrefHeight(currentFrame.rows * RBMKRenderHelper.CELL_SIZE);
			currentFrame.render();
			saveState();
		}
	}
	
	@FXML
	private void onClickNewBlueprint()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickNewBlueprint() triggered");
		
		Main.setFrame(new RBMKBlueprint(mainCanvas));
		
		onFrameChanged();
	}
	
	@FXML
	private void onClickNewSimulation()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickNewSimulation() triggered");

		Main.setFrame(new RBMKSimulation(mainCanvas));

		onFrameChanged();
	}
	
	@FXML
	private void onClickOpen()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickOpen() triggered");
		final FileChooser chooser = new FileChooser();
		chooser.setTitle(I18n.resolve("dialog.file.open"));
		chooser.setInitialDirectory(config.userPath.toFile());
		chooser.setSelectedExtensionFilter(config.binarySaveFiles ? FileUtil.GENERIC_FILTER : FileUtil.JSON_FILTER);
		final Path path = MiscUtil.convertOrNull(chooser.showOpenDialog(Main.getStage()), File::toPath);
		if (path == null)
			return;

		LOGGER.info("Reading saved file...");
        try
        {
            Main.setFrame(FileUtil.readSaveFile(path));
//			Main.getFrame().ifPresent(frame -> frame.setCanvas(mainCanvas));
        } catch (NoSuchFileException e)
        {
			LOGGER.warn("File does not exist!", e);
			Main.openErrorDialog(e);
        }

        onFrameChanged();
	}
	
	@FXML
	private void onClickClose()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickClose() triggered");
		lastPath = null;
		Main.closeFrame();
		setInfoArea(null);
		onFrameChanged();
	}
	
	@FXML
	private void onClickSave()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickSave() triggered");
		if (lastPath == null)
			onClickSaveAs();
		else
		{
			FileUtil.writeSaveFile(lastPath, currentFrame);
			saveState();
		}
	}
	
	@FXML
	private void onClickSaveAs()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickSaveAs() triggered");
		if (currentFrame == null)
			return;
		final FileChooser chooser = new FileChooser();
		chooser.setTitle(I18n.resolve("dialog.file.save"));
		chooser.setInitialDirectory(config.userPath.toFile());
		chooser.setSelectedExtensionFilter(config.binarySaveFiles
            ? switch (currentFrame)
			{
				case RBMKBlueprint ignored -> FileUtil.BLUEPRINT_FILTER;
				case RBMKSimulation ignored -> FileUtil.SIMULATION_FILTER;
				default -> throw new IllegalStateException("Invalid type: " + MiscUtil.convertOrNull(currentFrame, Object::getClass));
			}
			: FileUtil.JSON_FILTER
		);
		final Path path = MiscUtil.convertOrNull(chooser.showSaveDialog(Main.getStage()), File::toPath);
		if (path != null)
		{
			lastPath = path;
			FileUtil.writeSaveFile(path, currentFrame);
			saveState();
		}
	}
	
	@FXML
	private void onClickRevert()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickRevert() triggered, resetting to last saved state...");
		Main.setFrame(lastSavedState);
		onFrameChanged();
	}
	
	@FXML
	private void onClickPreferences()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("onClickPreferences() triggered");
		if (programConfigWindow == null)
		{
			try
			{
				final Parent root = FXMLLoader.load(Objects.requireNonNull(
						cachedProgramConfigFXML == null
								? cachedProgramConfigFXML = getClass().getClassLoader().getResource("resources/program_config_window.fxml")
								: cachedProgramConfigFXML));
				programConfigWindow = new Stage();
				programConfigWindow.setTitle(I18n.resolve("app.programConfig.title"));
				programConfigWindow.getIcons().add(Main.ICON_IMAGE);
				programConfigWindow.setScene(new Scene(root));
				programConfigWindow.showAndWait();
			} catch (IOException e)
			{
				LOGGER.error("Unable to open preferences window because of internal IO error!", e);
			}
		}
	}
	
	@SuppressWarnings("static-method")
	@FXML
	private void onClickLicense()
	{
		LOGGER.debug("onClickLicense() triggered");
		if (!Desktop.isDesktopSupported())
		{
			LOGGER.warn("Unable to open license because there is no provided desktop instance!");
			return;
		}
		// Try to read as file first
		// If it doesn't already exist
		if (licensePath == null)
		{
			LOGGER.trace("License temp file doesn't already exist, creating...");
			try
			{
				licensePath = MiscUtil.extractResource("resources/quick-guide-gplv3.pdf", ".pdf");
			} catch (IOException e)
			{
				LOGGER.warn("Unable to extract license file!", e);
				Main.openErrorDialog(e);
				licensePath = null;
				return;
			}
		}
		LOGGER.trace("Displaying license file...");
		final Thread openThread = new Thread(new FileOpener(licensePath), "File Open Thread");
		openThread.start();
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
			trySetZoom(currentFrame.renderer.zoom + amount);
		}
	}
	
	private void trySetZoom(double amount)
	{
		LOGGER.trace("trySetZoom(double) triggered");
		if (currentFrame != null)
		{
			LOGGER.trace("Changing frame render zoom level to: {}", amount);
			currentFrame.renderer.zoom = MiscUtil.clampDouble(amount, MIN_ZOOM, MAX_ZOOM);
			zoomTextField.setText(String.valueOf(currentFrame.renderer.zoom * 100));
			currentFrame.render();
		}
	}
	
	@FXML
	private void onClickZoomIn()
	{
		LOGGER.trace("onClickZoomIn() triggered");
		tryZoom(ZOOM_FACTOR);
	}
	
	@FXML
	private void onClickZoomOut()
	{
		LOGGER.trace("onClickZoomOut() triggered");
		tryZoom(-ZOOM_FACTOR);
	}
	
	@FXML
	private void onClickZoomReset()
	{
		LOGGER.trace("onClickZoomReset() triggered");
		if (currentFrame != null)
			trySetZoom(1);
	}
	
	@FXML
	private void onCanvasClicked(@NotNull MouseEvent event)
	{
		LOGGER.trace("onCanvasClicked(MouseEvent) triggered at coordinate [x={}, y={}] using button {}", event.getX(), event.getY(), event.getButton());
		if (currentFrame != null)
		{
			final int x = (int) (event.getX() / RBMKRenderHelper.CELL_SIZE / currentFrame.renderer.zoom),
					y = (int) (event.getY() / RBMKRenderHelper.CELL_SIZE / currentFrame.renderer.zoom);
			final GridLocation loc = new GridLocation(x, y);
			final RBMKColumnBase column = currentFrame.getColumnAtCoords(loc);
			currentFrame.setSelectedLocation(loc);
			setInfoArea(column != null && RBMKFrame.getSelectedLocation().isPresent() ? column : null);
			currentFrame.render();
		}
	}
	
	@FXML
	private void onCanvasScroll(@NotNull ScrollEvent event)
	{
		LOGGER.trace("onCanvasScroll(ScrollEvent) triggered with [Δx={}, Δy={}]", event.getDeltaX(), event.getDeltaY());
		if (event.isControlDown())
			tryZoom((event.getDeltaY() / SCROLL_FACTOR) * ZOOM_FACTOR);
		
		// TODO Regular scrolling
	}
	
	@FXML
	private void onCanvasZoom(@NotNull ZoomEvent event)
	{
		LOGGER.trace("onCanvasZoom(ZoomEvent) triggered with factor {}", event.getZoomFactor());
		tryZoom(event.getZoomFactor() - 1);
	}
	
	@FXML
	private void onNameTextFieldTextChanged(@NotNull KeyEvent event)
	{
		LOGGER.trace("onNameTextFieldTextChanged() triggered");
		if (currentFrame != null && event.getCode() == KeyCode.ENTER)
			currentFrame.setName(nameTextField.getText());
	}
	
	@FXML
	private void onCreatorNameTextChanged(@NotNull KeyEvent event)
	{
		LOGGER.trace("onCreatorNameTextChanged() triggered");
		if (currentFrame != null && event.getCode() == KeyCode.ENTER)
			currentFrame.setCreatorName(creatorTextField.getText());
	}
	
	@FXML
	private void onVersionTextChanged(@NotNull KeyEvent event)
	{
		LOGGER.trace("onVersionTextChanged() triggered");
		if (currentFrame != null && event.getCode() == KeyCode.ENTER)
			currentFrame.setVersion((Version) versionTextField.getTextFormatter().getValue());
	}
	
	@FXML
	private void onChangeDateInput()
	{
		LOGGER.trace("onChangeDateInput() triggered");
		if (currentFrame != null)
			currentFrame.setDate(dateInput.getValue());
	}

	@FXML
	private void onClickSetColumn()
	{
		LOGGER.debug("onClickSetColumn() triggered");
		if (currentFrame != null && RBMKFrame.getSelectedLocation().isPresent())
		{
			currentFrame.setColumn(RBMKFrame.getSelectedLocation().get(), columnTypeBox.getValue());
			setInfoArea(currentFrame.getColumnAtCoords(RBMKFrame.getSelectedLocation().get()));
			currentFrame.render();
		}
	}
	
	@FXML
	private void onClickResetColumn()
	{
		LOGGER.debug("onClickResetColumn() triggered");
		final GridLocation loc = RBMKFrame.getSelectedLocation().orElse(null);
		if (currentFrame != null && loc != null)
		{
			currentFrame.setColumn(RBMKFrame.getSelectedLocation().get(), null);
			setInfoArea(null);
			// Better performance?
			RBMKRenderHelper.eraseColumn(loc, mainCanvas.getGraphicsContext2D(), currentFrame.getRenderer().zoom);
			RBMKRenderHelper.drawSelectionRect(loc, mainCanvas.getGraphicsContext2D(), currentFrame.getRenderer().zoom);
		}
	}

	@FXML
	private void onZoomLevelTextChanged(@NotNull KeyEvent event)
	{
		LOGGER.trace("onZoomLevelTextChanged() triggered with code: {}", event.getCode());
		if (currentFrame != null && event.getCode() == KeyCode.ENTER)
		{
			final double newZoom;
			try
			{
				LOGGER.trace("Attempting to parse double...");
				newZoom = MiscUtil.clampDouble(Double.parseDouble(zoomTextField.getText()) / 100, MIN_ZOOM, MAX_ZOOM);
				trySetZoom(newZoom);
			} catch (NumberFormatException e)
			{
				LOGGER.warn("Caught exception with message: \"{}\" trying to parse double", e.getMessage());
				zoomTextField.setText(String.valueOf(currentFrame.renderer.zoom * 100));
			}
		}
	}

	@FXML
	private void onClickStartPause()
	{
		// TODO
		LOGGER.debug("onClickStartPause() triggered");
		Main.toggleRunning();
	}

	private void saveState()
	{
		switch (currentFrame)
		{
			case RBMKBlueprint blueprint    -> lastSavedState = new RBMKBlueprint(blueprint);
			case RBMKSimulation simulation  -> lastSavedState = new RBMKSimulation(simulation);
			default                         -> throw new IllegalStateException("Couldn't save state because of unknown type: " + MiscUtil.convertOrNull(currentFrame, Object::getClass));
		}
	}

	public void setInfoArea(@Nullable InfoProviderNT infoProvider)
	{
		LOGGER.debug("Set infoTextArea with data provided by InfoProvider instance");
		infoTextArea.getChildren().clear();
		if (infoProvider == null)
		{
			infoTextArea.getChildren()
			            .add(new TextBuilder(I18n.resolve("app.infoTextArea.promptText")).setColor(Color.YELLOW)
			                                                                             .getText());
		} else
			infoTextArea.getChildren().addAll(infoProvider.getText());
	}

}
