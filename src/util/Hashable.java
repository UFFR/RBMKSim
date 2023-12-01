package util;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.hash.PrimitiveSink;

@FunctionalInterface
public interface Hashable
{
	public void funnelInto(PrimitiveSink sink);
	
	public default HashCode calculateHashCode()
	{
		final Hasher hasher = provideHashFunction();
		funnelInto(hasher);
		return hasher.hash();
	}
	
	public default Hasher provideHashFunction()
	{
		return Hashing.murmur3_128().newHasher();
	}
}
