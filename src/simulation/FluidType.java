package simulation;

public enum FluidType
{
	WATER("Water", 20),
	STEAM("Steam", 100),
	DENSE_STEAM("Dense Steam", 300),
	SUPER_DENSE_STEAM("Super Dense Steam", 450),
	ULTRA_DENSE_STEAM("Ultra Dense Steam", 600);
	public final int temperature;
	public final String name;
	private FluidType(String name, int temperature)
	{
		this.name = name;
		this.temperature = temperature;
	}
	
	@Override
	public String toString()
	{
		return name + " (" + temperature + "°C)";
	}
}
