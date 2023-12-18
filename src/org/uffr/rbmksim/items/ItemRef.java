package org.uffr.rbmksim.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.collections.api.factory.primitive.ObjectIntMaps;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.api.map.primitive.ObjectIntMap;

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
	
	public ObjectIntMap<Item> getItemCounts()
	{
		final MutableObjectIntMap<Item> map = ObjectIntMaps.mutable.empty();
		getItemCounts(map, count);
		return map;
	}
	
	protected void getItemCounts(MutableObjectIntMap<Item> map, int mult)
	{
		for (ItemRef child : children)
		{
			if (child.children.isEmpty())
			{
				if (map.containsKey(child.item))
					map.addToValue(child.item, mult);
				else
					map.put(item, mult);
			} else
				child.getItemCounts(map, child.count * mult);
		}
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
