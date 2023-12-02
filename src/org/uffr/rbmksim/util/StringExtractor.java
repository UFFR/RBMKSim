package org.uffr.rbmksim.util;

import java.io.Serializable;
import java.util.function.Function;

public class StringExtractor<T> implements Serializable
{
	private static final long serialVersionUID = -5110263921630894098L;
	private final T storedObj;
	private transient final Function<T, String> extractorFunction;
	public StringExtractor(T obj, Function<T, String> extractor)
	{
		storedObj = obj;
		extractorFunction = extractor;
	}
	
	public T getStoredObj()
	{
		return storedObj;
	}

	@Override
	public String toString()
	{
		return extractorFunction.apply(storedObj);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return storedObj.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		return storedObj.hashCode();
	}
}
