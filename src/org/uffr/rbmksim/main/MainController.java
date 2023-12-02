package org.uffr.rbmksim.main;

import org.uffr.rbmksim.util.InfoProvider;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController
{
	@FXML
	private TextArea infoTextArea;
	@FXML
	private TextField nameTextField, creatorTextField, versionTextField;
	@FXML
	private DatePicker dateInput;
	
	@FXML
	private void onClickNewBlueprint()
	{
		// TODO Auto-generated method stub

	}
	
	@FXML
	private void onClickNewSimulation()
	{
		// TODO Auto-generated method stub

	}
	
	@FXML
	private void onClickOpen()
	{
		// TODO Auto-generated method stub

	}
	
	@FXML
	private void onClickClose()
	{
		// TODO Auto-generated method stub

	}
	
	@FXML
	private void onClickSave()
	{
		// TODO Auto-generated method stub

	}
	
	@FXML
	private void onClickSaveAs()
	{
		// TODO Auto-generated method stub

	}
	
	@FXML
	private void onClickRevert()
	{
		// TODO Auto-generated method stub

	}
	
	@FXML
	private void onClickPreferences()
	{
		// TODO Auto-generated method stub

	}
	
	@SuppressWarnings("static-method")
	@FXML
	private void onClickAbout()
	{
		Main.openDialog("About", "RBMK Blueprint Designer and Simulator", Main.getAboutString(), AlertType.INFORMATION);
	}
	
	@FXML
	private void onClickHelp()
	{
		// TODO Auto-generated method stub

	}
	
	@SuppressWarnings("static-method")
	@FXML
	private void onClickQuit()
	{
		System.exit(0);
	}
	
	public void setInfoArea(InfoProvider infoProvider)
	{
		infoTextArea.setText(infoProvider.asProperString());
	}
	
}
