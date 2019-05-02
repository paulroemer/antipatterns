package com.github.fluorumlabs.antipatterns.annotations;

import com.github.fluorumlabs.antipatterns.AntiPatterns;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify target class for {@link AntiPatterns#attach(Class, Object)} and {@link AntiPatterns#attachStatic(Class)}
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
     * Specify target class in via target instance in {@link AntiPatterns#attach(Class, Object)}
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Instance {

	}
}
