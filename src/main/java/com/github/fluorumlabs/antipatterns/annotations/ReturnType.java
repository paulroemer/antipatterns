package com.github.fluorumlabs.antipatterns.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify original return type for the method/getter in a target class.
 * <p>
 * This is required if mirror method signature differs from target method signature.
 * <p>
 * Use cases: wrapping nullable results in Optional, casting results to super classes or
 * adding fluent API to a third-party library.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReturnType {
	/**
	 * Original return type of a target class method
	 *
	 * @return
	 */
	Class<?> value();
}
