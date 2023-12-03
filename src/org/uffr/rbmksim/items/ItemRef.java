package org.uffr.rbmksim.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemRef
{
	private final Item item;
	private final int count;
	private final ArrayList<ItemRef> children = new ArrayList<>();
	public ItemRef(Item item, int count, ItemRef... children)
	{
		this.item = item;
		this.count = count;
		Collections.addAll(this.children, children);
	}
	
	public Item getItem()
	{
		return item;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public List<ItemRef> getChildren()
	{
		return Collections.unmodifiableList(children);
	}
}
