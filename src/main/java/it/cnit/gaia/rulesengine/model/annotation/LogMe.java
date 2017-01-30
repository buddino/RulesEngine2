package it.cnit.gaia.rulesengine.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//You need this to make the annotation available at runtime
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LogMe {
	boolean notification() default true;
	boolean event() default true;
}
