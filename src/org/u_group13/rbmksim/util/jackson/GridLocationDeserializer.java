package org.u_group13.rbmksim.util.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.jetbrains.annotations.NotNull;
import org.u_group13.rbmksim.simulation.GridLocation;

import java.io.IOException;

public class GridLocationDeserializer extends JsonDeserializer<GridLocation>
{
	@Override
	public GridLocation deserialize(@NotNull JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
	{
		final long stored = p.getLongValue();
		return new GridLocation((int) (stored >>> 32), (int) stored);
	}
}
