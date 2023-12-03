package org.uffr.rbmksim.main;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uffr.rbmksim.util.I18n;
import org.uffr.rbmksim.util.InfoProvider;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController implements Initializable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
	@FXML
	private TextArea infoTextArea;
	@FXML
	private TextField nameTextField, creatorTextField, versionTextField;
	@FXML
	private DatePicker dateInput;
	
//	private static void localizeString(Consumer<String> setter, Supplier<String> getter)
//	{
//		setter.accept(I18n.resolve(getter.get()));
//	}
	
	@Override
	public void initialize(URL url, ResourceBundle bundle)
	{
		// TODO Auto-generated method stub
//		infoTextArea.setPromptText(I18n.resolve(infoTextArea.getPromptText()));
//		localizeString(infoTextArea::setPromptText, infoTextArea::getPromptText);
//		localizeString(nameTextField::setPromptText, nameTextField::getPromptText);
		
		for (Field field : getClass().getDeclaredFields())
		{
			LOGGER.trace("Trying field [" + field + ']');
			try
			{
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				
				LOGGER.trace("Field [" + field + "] passed");
				final Node node = (Node) field.get(this);
				
				for (Object key : node.getProperties().keySet())
				{
					LOGGER.trace("Testing key [" + key + ']');
					if (key instanceof String)
					{
						LOGGER.trace("Key [" + key + "] is a string");
						if (I18n.hasKey("app." + field.getName() + '.' + (String) key))
							node.getProperties().put(key, I18n.resolve((String) key));
					}
				}
				
				/*final Object item = field.get(this);
				final ArrayList<Method> setterMethods = new ArrayList<>();
				for (Method method : item.getClass().getMethods())
					if (method.getName().startsWith("set"))
						setterMethods.add(method);
				
				for (Method method : setterMethods)
				{
					final String property = method.getName().substring(3);
					if (I18n.hasKey("app." + field.getName() + '.' + property))
					{
						try
						{
							method.invoke(item, I18n.resolve((String) item.getClass().getMethod("get" + property).invoke(item)));
						} catch (InvocationTargetException | NoSuchMethodException | SecurityException e)
						{
							LOGGER.debug("Assumed property controlled by setter method [" + method.getName() + "] has entry in lang file but does not have associated assumed getter method", e);
						}
					}
				}*/
				
			} catch (IllegalArgumentException | IllegalAccessException e)
			{
				LOGGER.error("Unable to retrieve field!", e);
			}
		}
	}
	
	@FXML
	private void onClickNewBlueprint()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickNewBlueprint() triggered");
	}
	
	@FXML
	private void onClickNewSimulation()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickNewSimulation() triggered");
	}
	
	@FXML
	private void onClickOpen()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickOpen() triggered");
	}
	
	@FXML
	private void onClickClose()
	{
		// TODO Auto-generated method stub
		LOGGER.debug("MainController.onClickClose() triggered");
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
		System.exit(0);
	}
	
	public void setInfoArea(InfoProvider infoProvider)
	{
		LOGGER.debug("Set infoTextArea with data provided by InfoProvider instance");
		infoTextArea.setText(infoProvider.asProperString());
	}
	
}
