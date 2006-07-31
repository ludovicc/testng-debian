package org.testng.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Mark a method as supplying data for a test method.
 * The annotated method must return an Object[][] where each
 * Object[] can be assigned the parameter list of the test method.
 * The @Test method that wants to receive data from this DataProvider
 * needs to use a dataProvider name equals to the name of this annotation.
 * 
 * @author cbeust
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface DataProvider {
  
  /**
   * The name of this DataProvider.
   */
  public String name();
}
