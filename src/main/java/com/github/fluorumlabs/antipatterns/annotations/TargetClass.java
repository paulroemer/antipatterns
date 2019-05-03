package com.github.fluorumlabs.antipatterns.annotations;

import com.github.fluorumlabs.antipatterns.AntiPatterns;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify target class for {@link AntiPatterns#attach(Class, Object)} and {@link AntiPatterns#attachStatic(Class)}
 *
 * If interface is annotated with {@link TargetClass.API}, first argument type of every non-static method will be
 * a target class for this method. Static members should be annotated with {@link TargetClass} annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
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

	/**
	 * Specify separate target class for each method
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface API {

	}
}
