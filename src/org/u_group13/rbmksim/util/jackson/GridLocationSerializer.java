package org.u_group13.rbmksim.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jetbrains.annotations.NotNull;
import org.u_group13.rbmksim.simulation.GridLocation;

import javax.annotation.Nonnull;
import java.io.IOException;

public class GridLocationSerializer extends JsonSerializer<GridLocation>
{
	@Override
	public void serialize(@Nonnull GridLocation value, @NotNull JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		final long result = ((long) value.x() << 32L) | value.y();
		gen.writeNumber(result);
	}
}
