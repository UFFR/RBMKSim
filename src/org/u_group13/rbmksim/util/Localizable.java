package org.u_group13.rbmksim.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Localizable
{
	public static final String NULL = "[NULL]";
	String overrideRegistry() default NULL;
}
