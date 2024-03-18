package org.u_group13.rbmksim.config;

import static org.u_group13.rbmksim.util.MiscUtil.parseBoolean;
import static org.u_group13.rbmksim.util.MiscUtil.parseDouble;
import static org.u_group13.rbmksim.util.MiscUtil.parseInt;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.u_group13.rbmksim.main.Main;
import org.u_group13.rbmksim.util.MiscUtil;
import org.uffr.uffrlib.hashing.Hashable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.hash.PrimitiveSink;

@SuppressWarnings("UnstableApiUsage")
public class SimulationConfig implements Config<SimulationConfig>, Hashable, Serializable, Cloneable
{
	@Serial
	private static final long serialVersionUID = 7368336732849301763L;
	private static final Logger LOGGER = LoggerFactory.getLogger(SimulationConfig.class);
	
	/// Defaults ///
	public static final double	PASSIVE_COOLING			= 1,
								COLUMN_HEAT_FLOW		= 0.2,
								FUEL_DIFFUSION_MOD		= 1,
								HEAT_PROVISION			= 0.4,
								BOILER_HEAT_CONSUMPTION	= 0.1,
								CONTROL_SPEED_MOD		= 1,
								REACTIVITY_MOD			= 1,
								OUTGASSER_MOD			= 1,
								SURGE_MOD				= 1,
								REASIM_MOD				= 1,
								REASIM_BOILER_SPEED		= 0.05;
	public static final byte	FLUX_RANGE				= 5,
								REASIM_RANGE			= 10,
								REASIM_COUNT			= 6;
	public static final boolean	REASIM_BOILERS			= false;
	
	/// Instance ///
	public double passiveCooling = PASSIVE_COOLING;
	public double columnHeatFlow = COLUMN_HEAT_FLOW;
	public double fuelDiffusionMod = FUEL_DIFFUSION_MOD;
	public double heatProvision = HEAT_PROVISION;
	public double boilerHeatConsumption = BOILER_HEAT_CONSUMPTION;
	public double controlSpeedMod = CONTROL_SPEED_MOD;
	public double reactivityMod = REACTIVITY_MOD;
	public double outgasserMod = OUTGASSER_MOD;
	public double surgeMod = SURGE_MOD;
	public double reasimMod = REASIM_MOD;
	public double reasimBoilerSpeed = REASIM_BOILER_SPEED;
	public int fluxRange = FLUX_RANGE;
	public int reasimRange = REASIM_RANGE;
	public int reasimCount = REASIM_COUNT;
	public boolean reasimBoilers = REASIM_BOILERS;
	
	public SimulationConfig()
	{}
	
	@Override
	public SimulationConfig copy(SimulationConfig config)
	{
		this.passiveCooling = config.passiveCooling;
		this.columnHeatFlow = config.columnHeatFlow;
		this.fuelDiffusionMod = config.fuelDiffusionMod;
		this.heatProvision = config.heatProvision;
		this.boilerHeatConsumption = config.boilerHeatConsumption;
		this.controlSpeedMod = config.controlSpeedMod;
		this.reactivityMod = config.reactivityMod;
		this.outgasserMod = config.outgasserMod;
		this.surgeMod = config.surgeMod;
		this.reasimBoilerSpeed = config.reasimBoilerSpeed;
		this.fluxRange = config.fluxRange;
		this.reasimRange = config.reasimRange;
		this.reasimCount = config.reasimCount;
		this.reasimBoilers = config.reasimBoilers;
		return this;
	}
	
	@Override
	public void fromJson(JsonNode config)
	{
		passiveCooling = config.get("passiveCooling").asDouble(PASSIVE_COOLING);
		columnHeatFlow = config.get("columnHeatFlow").asDouble(COLUMN_HEAT_FLOW);
		fuelDiffusionMod = config.get("fuelDiffusionMod").asDouble(FUEL_DIFFUSION_MOD);
		heatProvision = config.get("heatProvision").asDouble(HEAT_PROVISION);
		boilerHeatConsumption = config.get("boilerHeatConsumption").asDouble(BOILER_HEAT_CONSUMPTION);
		controlSpeedMod = config.get("controlSpeedMod").asDouble(CONTROL_SPEED_MOD);
		reactivityMod = config.get("reactivityMod").asDouble(REACTIVITY_MOD);
		outgasserMod = config.get("outgasserMod").asDouble(OUTGASSER_MOD);
		surgeMod = config.get("surgeMod").asDouble(SURGE_MOD);
		reasimMod = config.get("reasimMod").asDouble(REASIM_MOD);
		reasimBoilerSpeed = config.get("reasimBoilerSpeed").asDouble(REASIM_BOILER_SPEED);
		fluxRange = config.get("fluxRange").asInt(FLUX_RANGE);
		reasimRange = config.get("reasimRange").asInt(REASIM_RANGE);
		reasimCount = config.get("reasimCount").asInt(REASIM_COUNT);
		reasimBoilers = config.get("reasimBoilers").asBoolean(REASIM_BOILERS);
	}
	
	@Override
	public void fromBasic(String config)
	{
		final Map<String, String> map = MiscUtil.basicKVToMap(config, new HashMap<>(14));
		
		passiveCooling = parseDouble(map.get("passiveCooling"), PASSIVE_COOLING, LOGGER::warn);
		columnHeatFlow = parseDouble(map.get("columnHeatFlow"), COLUMN_HEAT_FLOW, LOGGER::warn);
		fuelDiffusionMod = parseDouble(map.get("fuelDiffusionMod"), FUEL_DIFFUSION_MOD, LOGGER::warn);
		heatProvision = parseDouble(map.get("heatProvision"), HEAT_PROVISION, LOGGER::warn);
		boilerHeatConsumption = parseDouble(map.get("boilerHeatConsumption"), BOILER_HEAT_CONSUMPTION, LOGGER::warn);
		controlSpeedMod = parseDouble(map.get("controlSpeedMod"), CONTROL_SPEED_MOD, LOGGER::warn);
		reactivityMod = parseDouble(map.get("reactivityMod"), REACTIVITY_MOD, LOGGER::warn);
		outgasserMod = parseDouble(map.get("outgasserMod"), OUTGASSER_MOD, LOGGER::warn);
		surgeMod = parseDouble(map.get("surgeMod"), SURGE_MOD, LOGGER::warn);
		reasimMod = parseDouble(map.get("reasimMod"), REASIM_MOD, LOGGER::warn);
		reasimBoilerSpeed = parseDouble(map.get("reasimBoilerSpeed"), REASIM_BOILER_SPEED, LOGGER::warn);
		fluxRange = parseInt(map.get("fluxRange"), FLUX_RANGE, LOGGER::warn);
		reasimRange = parseInt(map.get("reasimRange"), reasimRange, LOGGER::warn);
		reasimCount = parseInt(map.get("reasimCount"), reasimCount, LOGGER::warn);
		reasimBoilers = parseBoolean(map.get("reasimBoilers"), REASIM_BOILERS, LOGGER::warn);
	}
	
	@Override
	public void resetToDefault()
	{
		this.passiveCooling = PASSIVE_COOLING;
		this.columnHeatFlow = COLUMN_HEAT_FLOW;
		this.fuelDiffusionMod = FUEL_DIFFUSION_MOD;
		this.heatProvision = HEAT_PROVISION;
		this.boilerHeatConsumption = BOILER_HEAT_CONSUMPTION;
		this.controlSpeedMod = CONTROL_SPEED_MOD;
		this.reactivityMod = REACTIVITY_MOD;
		this.outgasserMod = OUTGASSER_MOD;
		this.surgeMod = SURGE_MOD;
		this.reasimMod = REASIM_MOD;
		this.reasimBoilerSpeed = REASIM_BOILER_SPEED;
		this.fluxRange = FLUX_RANGE;
		this.reasimRange = REASIM_RANGE;
		this.reasimCount = REASIM_COUNT;
		this.reasimBoilers = REASIM_BOILERS;
	}
	
	@Override
	public JsonNode asJsonConfig()
	{
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode rootNode = mapper.createObjectNode();
		
		rootNode
		.put("passiveCooling", passiveCooling)
		.put("columnHeatFlow", columnHeatFlow)
		.put("fuelDiffusionMod", fuelDiffusionMod)
		.put("heatProvision", heatProvision)
		.put("boilerHeatConsumption", boilerHeatConsumption)
		.put("controlSpeedMod", controlSpeedMod)
		.put("reactivityMod", reactivityMod)
		.put("outgasserMod", outgasserMod)
		.put("surgeMod", surgeMod)
		.put("reasimBoilerSpeed", reasimBoilerSpeed)
		.put("fluxRange", fluxRange)
		.put("reasimRange", reasimRange)
		.put("reasimCount", reasimCount)
		.put("reasimBoilers", reasimBoilers);
		
		return rootNode;
	}
	
	@Override
	public String asBasicConfig()
	{

		return "passiveCooling=" + passiveCooling + '\n' +
				"columnHeatFlow=" + columnHeatFlow + '\n' +
				"fuelDiffusionMod=" + fuelDiffusionMod + '\n' +
				"heatProvision=" + heatProvision + '\n' +
				"boilerHeatConsumption=" + boilerHeatConsumption + '\n' +
				"controlSpeedMod=" + controlSpeedMod + '\n' +
				"reactivityMod=" + reactivityMod + '\n' +
				"outgasserMod=" + outgasserMod + '\n' +
				"surgeMod=" + surgeMod + '\n' +
				"reasimBoilerSpeed=" + reasimBoilerSpeed + '\n' +
				"fluxRange=" + fluxRange + '\n' +
				"reasimRange=" + reasimRange + '\n' +
				"reasimCount=" + reasimCount + '\n' +
				"reasimBoilers=" + reasimBoilers + '\n';
	}
	
	@Override
	public void funnelInto(PrimitiveSink sink)
	{
		sink
		.putDouble(boilerHeatConsumption)
		.putDouble(columnHeatFlow)
		.putDouble(controlSpeedMod)
		.putDouble(fluxRange)
		.putDouble(fuelDiffusionMod)
		.putDouble(heatProvision)
		.putDouble(outgasserMod)
		.putDouble(passiveCooling)
		.putDouble(reactivityMod)
		.putDouble(reasimBoilerSpeed)
		.putBoolean(reasimBoilers)
		.putInt(reasimCount)
		.putDouble(reasimMod)
		.putInt(reasimRange)
		.putDouble(surgeMod);
	}
	
	@Override
	public SimulationConfig clone()
	{
		try
		{
			return (SimulationConfig) super.clone();
		} catch (CloneNotSupportedException e)
		{
			Main.openErrorDialog(e);
			return new SimulationConfig().copy(this);
		}
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(boilerHeatConsumption, columnHeatFlow, controlSpeedMod, fluxRange, fuelDiffusionMod,
				heatProvision, outgasserMod, passiveCooling, reactivityMod, reasimBoilerSpeed, reasimBoilers,
				reasimCount, reasimMod, reasimRange, surgeMod);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof SimulationConfig other))
			return false;
        return Double.doubleToLongBits(boilerHeatConsumption) == Double.doubleToLongBits(other.boilerHeatConsumption)
				&& Double.doubleToLongBits(columnHeatFlow) == Double.doubleToLongBits(other.columnHeatFlow)
				&& Double.doubleToLongBits(controlSpeedMod) == Double.doubleToLongBits(other.controlSpeedMod)
				&& fluxRange == other.fluxRange
				&& Double.doubleToLongBits(fuelDiffusionMod) == Double.doubleToLongBits(other.fuelDiffusionMod)
				&& Double.doubleToLongBits(heatProvision) == Double.doubleToLongBits(other.heatProvision)
				&& Double.doubleToLongBits(outgasserMod) == Double.doubleToLongBits(other.outgasserMod)
				&& Double.doubleToLongBits(passiveCooling) == Double.doubleToLongBits(other.passiveCooling)
				&& Double.doubleToLongBits(reactivityMod) == Double.doubleToLongBits(other.reactivityMod)
				&& Double.doubleToLongBits(reasimBoilerSpeed) == Double.doubleToLongBits(other.reasimBoilerSpeed)
				&& reasimBoilers == other.reasimBoilers && reasimCount == other.reasimCount
				&& Double.doubleToLongBits(reasimMod) == Double.doubleToLongBits(other.reasimMod)
				&& reasimRange == other.reasimRange
				&& Double.doubleToLongBits(surgeMod) == Double.doubleToLongBits(other.surgeMod);
	}
	
	@Override
	public String toString()
	{
		return "SimulationConfig [passiveCooling=" + passiveCooling + ", columnHeatFlow=" +
				columnHeatFlow + ", fuelDiffusionMod=" + fuelDiffusionMod +
				", heatProvision=" + heatProvision + ", boilerHeatConsumption=" +
				boilerHeatConsumption + ", controlSpeedMod=" + controlSpeedMod +
				", reactivityMod=" + reactivityMod + ", outgasserMod=" + outgasserMod +
				", surgeMod=" + surgeMod + ", reasimMod=" + reasimMod +
				", reasimBoilerSpeed=" + reasimBoilerSpeed + ", fluxRange=" + fluxRange +
				", reasimRange=" + reasimRange + ", reasimCount=" + reasimCount +
				", reasimBoilers=" + reasimBoilers + ']';
	}
	
}
