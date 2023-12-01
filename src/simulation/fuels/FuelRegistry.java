package simulation.fuels;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class FuelRegistry
{
	public static final Map<String, RBMKFuelData> FUEL_REGISTRY = new HashMap<String, RBMKFuelData>();
	
	public static final RBMKFuelData NU_FUEL = register(new RBMKFuelBuilder("NU Fuel", "Unenriched Uranium", 15, 2865, 0));
	public static final RBMKFuelData DRX_FUEL = register(new RBMKFuelBuilder("Digamma Fuel", "Can you hear? Can you hear the thunder?", 10_000, 1_000_000, 0).setBurnFunction(EnumBurnFunction.QUADRATIC));
	
	
	public static RBMKFuelData register(RBMKFuelBuilder data)
	{
		final RBMKFuelData fuelData = data.construct();
		FUEL_REGISTRY.put(fuelData.name(), fuelData);
		return fuelData;
	}
	
	public static List<RBMKFuelData> getFuelRegistrySorted()
	{
		final List<RBMKFuelData> list = new ArrayList<RBMKFuelData>(FUEL_REGISTRY.values());
		list.sort(Comparator.comparing(RBMKFuelData::fuelCategory).thenComparing(RBMKFuelData::name));
		return ImmutableList.copyOf(list);
	}
	
	public static Map<String, RBMKFuelData> getFuelMap()
	{
		return ImmutableMap.copyOf(FUEL_REGISTRY);
	}
	
	public static RBMKFuelRod getFuelRod(RBMKFuelData data)
	{
		return data == DRX_FUEL ? new RBMKFuelDRX() : new RBMKFuelRod(data);
	}
	
	public static RBMKFuelData getByName(String name)
	{
		return FUEL_REGISTRY.get(name);
	}
	
	public static boolean nameExists(String name)
	{
		return FUEL_REGISTRY.containsKey(name);
	}
	
	public static boolean valueExists(RBMKFuelData data)
	{
		return FUEL_REGISTRY.containsValue(data);
	}
}
