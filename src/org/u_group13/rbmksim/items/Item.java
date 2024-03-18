package org.u_group13.rbmksim.items;

import org.u_group13.rbmksim.util.I18n;

public class Item
{
	private final String unlocalizedName;
	private final transient String localizedName;
	public Item(String unlocalizedName)
	{
		this.unlocalizedName = unlocalizedName;
		
		localizedName = I18n.resolve(unlocalizedName);
	}
	
	public String getUnlocalizedName()
	{
		return unlocalizedName;
	}
	
	public String getLocalizedName()
	{
		return localizedName;
	}
}
