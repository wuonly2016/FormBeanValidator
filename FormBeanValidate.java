/**
 * @author Michael Ma
 * FormBeanValidate
 * annotation
 * mailto:wuonly@outlook.com
 */
package com.***.validater;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FormBeanValidate {

	String[] marcher();
	
	Class<?> type() default String.class;
}
