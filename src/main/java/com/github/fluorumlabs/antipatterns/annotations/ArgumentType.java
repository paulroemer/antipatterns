package com.github.fluorumlabs.antipatterns.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify original argument type for the method/setter in a target class.
 * <p>
 * This is required if mirror method signature differs from target method signature.
 * <p>
 * Use cases: adding fluent API to a third-party library.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ArgumentType {
	/**
	 * Original return type of a target class method
	 *
	 * @return argument type
	 */
	Class<?> value();
}
