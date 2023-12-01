package main.dialog;

import java.util.function.Supplier;

import simulation.scolumns.RBMKSimColumnBase;

public abstract class ColumnDialogBase<T extends RBMKSimColumnBase> extends ConfigDialogBase
{
	protected final RBMKSimColumnBase column;
	protected final Supplier<T> cSupplier;
	public ColumnDialogBase(RBMKSimColumnBase column)
	{
		super();
		this.column = column;
		cSupplier = () -> (T) column;
	}
}
