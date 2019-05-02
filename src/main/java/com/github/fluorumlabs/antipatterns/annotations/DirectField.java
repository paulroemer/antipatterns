package com.github.fluorumlabs.antipatterns.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark mirror interface method as a direct accessor for the target field.
 * <p>
 * This means that getters/setters will be bypassed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DirectField {
}
