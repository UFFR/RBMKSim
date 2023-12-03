package org.uffr.rbmksim.config;

import java.io.Serializable;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.uffr.uffrlib.hashing.Hashable;

public interface ConfigNT extends Hashable, Serializable
{
	public Configuration constructDefaults() throws ConfigurationException;
}
