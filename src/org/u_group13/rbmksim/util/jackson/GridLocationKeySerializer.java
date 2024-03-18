package org.u_group13.rbmksim.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import org.u_group13.rbmksim.simulation.GridLocation;

import javax.annotation.Nonnull;
import java.io.IOException;

public class GridLocationKeySerializer extends StdKeySerializers.Default
{
	public GridLocationKeySerializer(int typeId, Class<?> type)
	{
		super(typeId, type);
	}

	@Override
	public void serialize(@Nonnull Object value, @Nonnull JsonGenerator g, SerializerProvider provider) throws IOException
	{
		final GridLocation location = (GridLocation) value;
		g.writeFieldName(String.valueOf(((long) location.x() << 32L) | location.y()));
	}
}
