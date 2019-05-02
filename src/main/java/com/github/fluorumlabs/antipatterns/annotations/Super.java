package com.github.fluorumlabs.antipatterns.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark mirrored method as an accessor to some super method of target class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Super {
	/**
	 * Super class. Target class must be instance of super class.
	 *
	 * @return target super class
	 */
	Class<?> value();
}
