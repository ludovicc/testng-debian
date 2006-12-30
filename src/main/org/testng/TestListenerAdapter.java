package org.testng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.internal.IResultListener;


/**
 * Simple ITestListener adapter.
 *
 * @author Cedric Beust, Aug 6, 2004
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class TestListenerAdapter implements IResultListener {
  private List<ITestNGMethod> m_allTestMethods = Collections.synchronizedList(new ArrayList<ITestNGMethod>());
  private List<ITestResult> m_passedTests = Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestResult> m_failedTests = Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestResult> m_skippedTests = Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestResult> m_failedButWSPerTests = Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestContext> m_testContexts= Collections.synchronizedList(new ArrayList<ITestContext>());
  private List<ITestResult> m_failedConfs= Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestResult> m_skippedConfs= Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestResult> m_passedConfs= Collections.synchronizedList(new ArrayList<ITestResult>());

  public void onTestSuccess(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_passedTests.add(tr);
  }

  public void onTestFailure(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_failedTests.add(tr);
  }

  public void onTestSkipped(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_skippedTests.add(tr);
  }
  
  public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_failedButWSPerTests.add(tr);
  }
  
  protected ITestNGMethod[] getAllTestMethods() {
    return m_allTestMethods.toArray(new ITestNGMethod[m_allTestMethods.size()]);
  }

  public void onStart(ITestContext testContext) {
	  m_testContexts.add(testContext);
  }

  public void onFinish(ITestContext testContext) {
  }
  
  /**
   * @return Returns the failedButWithinSuccessPercentageTests.
   */
  public List<ITestResult> getFailedButWithinSuccessPercentageTests() {
    return m_failedButWSPerTests;
  }
  /**
   * @return Returns the failedTests.
   */
  public List<ITestResult> getFailedTests() {
    return m_failedTests;
  }
  /**
   * @return Returns the passedTests.
   */
  public List<ITestResult> getPassedTests() {
    return m_passedTests;
  }
  /**
   * @return Returns the skippedTests.
   */
  public List<ITestResult> getSkippedTests() {
    return m_skippedTests;
  }
  
  private static void ppp(String s) {
    System.out.println("[TestListenerAdapter] " + s);
  }
  /**
   * @param allTestMethods The allTestMethods to set.
   */
  public void setAllTestMethods(List<ITestNGMethod> allTestMethods) {
    m_allTestMethods = allTestMethods;
  }
  /**
   * @param failedButWithinSuccessPercentageTests The failedButWithinSuccessPercentageTests to set.
   */
  public void setFailedButWithinSuccessPercentageTests(
      List<ITestResult> failedButWithinSuccessPercentageTests) {
    m_failedButWSPerTests = failedButWithinSuccessPercentageTests;
  }
  /**
   * @param failedTests The failedTests to set.
   */
  public void setFailedTests(List<ITestResult> failedTests) {
    m_failedTests = failedTests;
  }
  /**
   * @param passedTests The passedTests to set.
   */
  public void setPassedTests(List<ITestResult> passedTests) {
    m_passedTests = passedTests;
  }
  /**
   * @param skippedTests The skippedTests to set.
   */
  public void setSkippedTests(List<ITestResult> skippedTests) {
    m_skippedTests = skippedTests;
  }

  public void onTestStart(ITestResult result) {
  }

  /**
   * @return
   */
  public List<ITestContext> getTestContexts() {
    return m_testContexts;
  }

  public List<ITestResult> getConfigurationFailures() {
    return m_failedConfs;
  }
  
  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationFailure(org.testng.ITestResult)
   */
  public void onConfigurationFailure(ITestResult itr) {
    m_failedConfs.add(itr);
  }

  public List<ITestResult> getConfigurationSkips() {
    return m_skippedConfs;
  }
  
  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationSkip(org.testng.ITestResult)
   */
  public void onConfigurationSkip(ITestResult itr) {
    m_skippedConfs.add(itr);
  }

  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationSuccess(org.testng.ITestResult)
   */
  public void onConfigurationSuccess(ITestResult itr) {
    m_passedConfs.add(itr);
  }
}
