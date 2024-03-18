package org.u_group13.rbmksim.main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.config.ProgramConfig;
import org.u_group13.rbmksim.util.I18n;
import org.u_group13.rbmksim.util.MiscUtil;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;

public class ProgramConfigController implements Initializable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramConfigController.class);
	@FXML
	public Label localeLabel, userPathLabel, usernameLabel, tickDelayLabel;
	@FXML
	public ChoiceBox<Locale> localeChoiceBox;
	@FXML
	public TextField directoryPathTextField, usernameTextField, tickDelayTextField;
	@FXML
	public Button directoryPathSelectButton, directoryPathResetButton, okButton, cancelButton, applyButton;
	@FXML
	public CheckBox repairCheckBox, binarySavesCheckBox;
	@FXML
	public Tooltip localeChoiceBoxTooltip, userPathTooltip, userPathChooseTooltip, userPathResetTooltip, usernameTextFieldTooltip, tickDelayTextFieldTooltip, repairCheckBoxTooltip, binarySavesCheckBoxTooltip;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		for (Locale availableLocale : Locale.getAvailableLocales())
			if (I18n.hasLocale(availableLocale))
				localeChoiceBox.getItems().add(availableLocale);

		directoryPathTextField.setTextFormatter(new TextFormatter<>(new StringConverter<Path>()
		{
			@Override
			public String toString(Path object)
			{
				return String.valueOf(object);
			}

			@Override
			public Path fromString(String string)
			{
				try
				{
					return string == null ? Main.instance.overrideUserPath : Path.of(string);
				} catch (Exception e)
				{
					LOGGER.warn("Couldn't get path!", e);
					return Main.instance.overrideUserPath;
				}
			}
		}));

		tickDelayTextField.setTextFormatter(new TextFormatter<>(new StringConverter<Integer>()
		{
			@Override
			public String toString(Integer object)
			{
				return String.valueOf(object);
			}

			@Override
			public Integer fromString(String string)
			{
				return string == null ? ProgramConfig.TICK_DELAY : string.matches("\\d+") ? Integer.parseInt(string) : ProgramConfig.TICK_DELAY;
			}
		}));

		localeChoiceBox.setValue(Main.config.locale);
		directoryPathTextField.setText(Main.config.userPath.toString());
		usernameTextField.setText(Main.config.username);
		tickDelayTextField.setText(String.valueOf(Main.config.tickDelay));
		repairCheckBox.setSelected(Main.config.repairDiscrepancies);
		binarySavesCheckBox.setSelected(Main.config.binarySaveFiles);
	}

	@FXML
	private void onClickChoosePath()
	{
		final DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(I18n.resolve("app.programConfig.userPathChoose"));
		chooser.setInitialDirectory(Main.getUserPath().toFile());
		final Path path = MiscUtil.convertOrNull(chooser.showDialog(Main.getStage()), File::toPath);
		if (path == null)
			return;

		directoryPathTextField.setText(path.toString());
	}

	@FXML
	private void onClickResetPath()
	{
		directoryPathTextField.setText(String.valueOf(Main.getUserPath()));
	}

	@FXML
	private void onClickOK()
	{
		LOGGER.debug("Will apply and close");
		onClickApply();
		closeWindow();
	}

	@FXML
	private void onClickApply()
	{
		LOGGER.debug("Applying settings...");

		Main.config.locale = localeChoiceBox.getValue();
		Main.config.userPath = Path.of(directoryPathTextField.getText());
		Main.config.username = usernameTextField.getText();
		Main.config.tickDelay = Integer.parseInt(tickDelayTextField.getText());
		Main.config.repairDiscrepancies = repairCheckBox.isSelected();
		Main.config.binarySaveFiles = binarySavesCheckBox.isSelected();

		Main.instance.trySaveConfig();
	}

	@FXML
	private void onClickCancel()
	{
		LOGGER.debug("Will close without applying");
		closeWindow();
	}

	private static void closeWindow()
	{
		LOGGER.debug("Closing window");
		MainController.programConfigWindow.close();
		MainController.programConfigWindow = null;
	}
}
