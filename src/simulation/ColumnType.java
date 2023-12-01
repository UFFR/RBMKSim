package simulation;

public enum ColumnType
{
	BLANK(false, '#', "Structure"),
	FUEL(true, 'F', "Fuel"),
	FUEL_SIM(true, 'F', "ReaSim Fuel"),
	CONTROL(true, 'C', "Control Rod"),
	CONTROL_AUTO(true, 'C', "Automatic Control Rod"),
	BOILER(true, 'B', "Boiler"),
	MODERATOR(false, 'M', "Graphite Moderator"),
	ABSORBER(false, 'A', "Boron Absorber"),
	REFLECTOR(false, 'R', "Tungsten Carbide Reflector"),
	OUTGASSER(true, 'O', "Irradiator"),
	BREEDER(true, 'I', "Breeder"),
	STORAGE(true, 'S', "Storage"),
	COOLER(true, 'V', "Cooler"),
	HEATEX(false, 'H', "Heat Exchanger");
	public final boolean hasGUI;
	public final char symbol;
	public final String fullName;
	private ColumnType(boolean hasGUI, char symbol, String fullName)
	{
		this.hasGUI = hasGUI;
		this.symbol = symbol;
		this.fullName = fullName;
	}
}
