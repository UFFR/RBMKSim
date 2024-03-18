package org.u_group13.rbmksim.util.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import org.u_group13.rbmksim.simulation.GridLocation;

import java.io.IOException;

public class GridLocationKeyDeserializer extends KeyDeserializer
{
	@Override
	public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException
	{
		final long l = Long.parseLong(key);
		return new GridLocation((int) (l >>> 32), (int) l);
	}
}
