package com.cognifide.cq.includefilter.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation means that ResourceTypesProvider should not been cached by SDI.
 * 
 * @author tomasz.rekawek
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface NoCache {

}
