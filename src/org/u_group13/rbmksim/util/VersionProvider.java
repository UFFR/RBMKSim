package org.u_group13.rbmksim.util;

import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider
{
	@Override
	public String[] getVersion() throws Exception
	{
		return new String[] {MiscUtil.VERSION_STRING};
	}
}
