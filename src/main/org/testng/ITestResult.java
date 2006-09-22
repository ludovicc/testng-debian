package org.testng;

import java.io.Serializable;

/**
 * This class describes the result of a test.
 *
 * @author Cedric Beust, May 2, 2004
 * @since May 2, 2004
 * @version $Revision$, $Date$
 * 
 */
public interface ITestResult extends Serializable {
  
  //
  // Test status
  //
  public static final int SUCCESS = 1;
  public static final int FAILURE = 2;
  public static final int SKIP = 3;
  public static final int SUCCESS_PERCENTAGE_FAILURE = 4;
  public static final int STARTED= 16; 
  
  /**
   * @return The status of this result, using one of the constants
   * above.
   */
  public int getStatus();
  public void setStatus(int status);
  
  /**
   * @return The test method this result represents.
   */
  public ITestNGMethod getMethod();
  
  /**
   * @return The parameters this method was invoked with.
   */
  public Object[] getParameters();
  public void setParameters(Object[] parameters);

  /**
   * @return The test class used this object is a result for.
   */
  public IClass getTestClass();
  
  /**
   * @return The throwable that was thrown while running the
   * method, or null if no exception was thrown.
   */  
  public Throwable getThrowable();
  public void setThrowable(Throwable throwable);
  
  /**
   * @return the start date for this test, in milliseconds.
   */
  public long getStartMillis();
  
  /**
   * @return the end date for this test, in milliseconds.
   */
  public long getEndMillis();
  public void setEndMillis(long millis);
  
  /**
   * @return The name of this TestResult, typically identical to the name
   * of the method.
   */
  public String getName();
  
  /**
   * @return true if if this test run is a SUCCESS
   */
  public boolean isSuccess();
  
  /**
   * @return The host where this suite was run, or null if it was run locally.  The
   * returned string has the form:  host:port
   */
  public String getHost();

}
