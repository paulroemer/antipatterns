package com.github.fluorumlabs.antipatterns.annotations;

import com.github.fluorumlabs.antipatterns.Classes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify target class for {@link Classes#attach(Class, Object)} and {@link Classes#attachStatic(Class)}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TargetClass {
	/**
	 * Target class for mirror
	 *
	 * @return target class
	 */
	Class<?> value();

	/**
     * Specify target class in via target instance in {@link Classes#attach(Class, Object)}
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Instance {

	}
}
