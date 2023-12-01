package util;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface InfoProvider
{
	public void addInformation(List<String> info);
	
	public default String asProperString()
	{
		final List<String> info = new ArrayList<>();
		addInformation(info);
		final StringBuilder builder = new StringBuilder(info.size() * 20);
		
		for (String s : info)
			builder.append(s).append('\n');
		
		return builder.toString();
	}
}
