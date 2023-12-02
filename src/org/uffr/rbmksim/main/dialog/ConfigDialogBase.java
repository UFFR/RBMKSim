package org.uffr.rbmksim.main.dialog;

import java.awt.AWTEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public abstract class ConfigDialogBase
{
	protected final AWTEvent closeDialog = new WindowEvent(getFrame(), WindowEvent.WINDOW_CLOSING);
	private final ActionListener cancelActionListener = e -> getFrame().dispatchEvent(closeDialog);
	private final ActionListener acceptActionListener = e -> saveAndClose();
	protected final JButton acceptButton, cancelButton;

	public ConfigDialogBase()
	{
		acceptButton = new JButton("Save");
		cancelButton = new JButton("Cancel");
		acceptButton.addActionListener(acceptActionListener);
		cancelButton.addActionListener(cancelActionListener);
		getFrame().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		initGUI();
	}

	protected abstract void initGUI();
	protected abstract void saveAndClose();
	public abstract JFrame getFrame();

}
