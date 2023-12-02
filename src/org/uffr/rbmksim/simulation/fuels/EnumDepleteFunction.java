package org.uffr.rbmksim.simulation.fuels;

public enum EnumDepleteFunction
{
	LINEAR,			//old function
	RAISING_SLOPE,	//for breeding fuels such as MEU, maximum of 110% at 28% depletion
	BOOSTED_SLOPE,	//for strong breeding fuels such Th232, maximum of 132% at 64% depletion
	GENTLE_SLOPE,	//recommended for most fuels, maximum barely over the start, near the beginning
	STATIC;			//for arcade-style neutron sources
}
