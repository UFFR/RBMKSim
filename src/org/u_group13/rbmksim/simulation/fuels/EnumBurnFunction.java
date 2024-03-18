package org.u_group13.rbmksim.simulation.fuels;

public enum EnumBurnFunction
{
	PASSIVE("SAFE / PASSIVE", ""),			//const, no reactivity
	LOG_TEN("MEDIUM / LOGARITHMIC", "log10(%1$s + 1) * 0.5 * %2$s"),		//log10(x + 1) * reactivity * 50
	PLATEU("SAFE / EULER", "(1 - e^-%1$s / 25)) * %2$s"),					//(1 - e^(-x/25)) * reactivity * 100
	ARCH("MEDIUM / NEGATIVE-QUADRATIC", "(%1$s - %1$s² / 10000) / 100 * %2$s [0;∞)"),	//x-(x²/1000) * reactivity
	SIGMOID("SAFE / SIGMOID", "%2$s / (1 + e^(-(%1$s - 50) / 10)"),				//100 / (1 + e^(-(x - 50) / 10)) <- tiny amount of reactivity at x=0 !
	SQUARE_ROOT("MEDIUM / SQUARE ROOT", "√(%1$s) * %2$s / 10"),	//sqrt(x) * 10 * reactivity
	LINEAR("DANGEROUS / LINEAR", "%1$s / 100 * %2$s"),				//x * reactivity
	QUADRATIC("DANGEROUS / QUADRATIC", "%1$s² / 10000 * %2$s"),		//x^2 / 100 * reactivity
	EXPERIMENTAL("EXPERIMENTAL / SINE SLOPE", "%1$s * (sin(%1$s) + 1) * %2$s");		//x * (sin(x) + 1)
	
	public final String title, format;
	EnumBurnFunction(String title, String format)
	{
		this.title = title;
		this.format = format;
	}
}
