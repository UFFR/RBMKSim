package org.uffr.rbmksim.util;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@FunctionalInterface
public interface InfoProvider
{
	void addInformation(List<String> info);
	
	default String asProperString()
	{
		final List<String> info = new ArrayList<>();
		addInformation(info);
		final StringBuilder builder = new StringBuilder(info.size() * 20);
		
		for (String s : info)
			builder.append(s).append('\n');
		
		return builder.toString();
	}
}
