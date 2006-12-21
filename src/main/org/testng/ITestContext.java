package org.testng;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;


/**
 * This class defines a test context which contains all the information
 * for a given test run.  An instance of this context is passed to the
 * test listeners so they can query information about their
 * environment.
 *
 * @author Cedric Beust, Aug 6, 2004
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public interface ITestContext extends Serializable {
  
  /**
   * The name of this test.
   */
  public String getName();

  /**
   * When this test started running.
   */
  public Date getStartDate();

  /**
   * When this test stopped running.
   */
  public Date getEndDate();

  /**
   * @return A list of all the tests that run successfully.
   */
  public IResultMap getPassedTests();
  
  /**
   * @return A list of all the tests that were skipped
   */
  public IResultMap  getSkippedTests();
  
  /**
   * @return A list of all the tests that failed but are being ignored because
   * annotated with a successPercentage.
   */
  public IResultMap  getFailedButWithinSuccessPercentageTests();
  
  /**
   * @return A map of all the tests that passed, indexed by
   * their ITextMethor.
   * 
   * @see org.testng.ITestNGMethod
   */
  public IResultMap getFailedTests();

  /**
   * @return All the groups that are included for this test run.
   */
  public String[] getIncludedGroups();
  
  /**
   * @return All the groups that are excluded for this test run.
   */
  public String[] getExcludedGroups();
  
  /**
   * @return Where the reports will be generated.
   */
  public String getOutputDirectory();
  
  /** 
   * @return The Suite object that was passed to the runner
   * at start-up.
   */
  public ISuite getSuite();
  
  /**
   * @return All the test methods that were run.
   */
  public ITestNGMethod[] getAllTestMethods();

  /**
   * @return The host where this test was run, or null if it was run locally.  The
   * returned string has the form:  host:port
   */
  public String getHost();

  /**
   * @return All the methods that were not included in this test run.
   */
  public Collection<ITestNGMethod> getExcludedMethods();
  
  /**
   * Retrieves information about the successful configuration method invocations.
   * @return
   */
  IResultMap getPassedConfigurations();
  
  /**
   * Retrieves information about the skipped configuration method invocations.
   * @return
   */
  IResultMap getSkippedConfigurations();
  
  /**
   * Retrieves information about the failed configuration method invocations.
   * @return
   */
  IResultMap getFailedConfigurations();
}
