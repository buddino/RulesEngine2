package it.cnit.gaia.rulesengine.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//You need this to make the annotation available at runtime
@Retention(RetentionPolicy.RUNTIME)
public @interface ToBeLogged {
}
