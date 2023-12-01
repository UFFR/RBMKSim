package main.dialog;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import simulation.fuels.FuelRegistry;
import simulation.fuels.RBMKFuelData;
import simulation.fuels.RBMKFuelRod;
import simulation.scolumns.RBMKFuel;

public class FuelDialog extends ColumnDialogBase<RBMKFuel>
{
	private final JFrame frame = new JFrame("RBMK Fuel Rod");
	private final JComboBox<RBMKFuelData> fuelComboBox;
	private final JButton setFuelButton = new JButton("Set Fuel"), resetButton = new JButton("Reset Fuel Rod");
	public FuelDialog(RBMKFuel column)
	{
		super(column);
		fuelComboBox = new JComboBox<>(new Vector<>(FuelRegistry.getFuelRegistrySorted()));
		fuelComboBox.addItem(null);
	}

	@Override
	protected void initGUI()
	{
		setFuelButton.addActionListener(e -> cSupplier.get().setFuelRod(fuelComboBox.getSelectedItem() == null ? null : new RBMKFuelRod(((RBMKFuelData) fuelComboBox.getSelectedItem()))));
		resetButton.addActionListener(e -> {if (cSupplier.get().getFuelRod().isPresent()) cSupplier.get().getFuelRod().get().reset();});
		
		frame.add(acceptButton);
		frame.add(cancelButton);
		frame.add(fuelComboBox);
		frame.add(setFuelButton);
		frame.add(resetButton);
		
		frame.setSize(400, 600);
		frame.setLayout(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}
	
	@Override
	protected void saveAndClose()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public JFrame getFrame()
	{
		return frame;
	}

}
