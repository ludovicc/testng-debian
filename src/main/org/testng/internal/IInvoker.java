package org.testng.internal;

import java.util.List;
import java.util.Map;

import org.testng.IClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

/**
 * This class defines an invoker.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IInvoker {
  
  /**
   * Invoke configuration methods if they belong to the same TestClass
   * passed in parameter..
   * 
   * TODO:  Calculate ahead of time which methods should be
   * invoked for each class.   Might speed things up for users who invoke the same
   * test class with different parameters in the same suite run.
   * 
   * @param testClass the class whose configuration methods must be run
   */
  public  void invokeConfigurations(IClass testClass,
                                    ITestNGMethod[] allMethods,
                                    XmlSuite suite, 
                                    Map<String, String> parameters,
                                    Object[] parameterValues,
                                    Object instance);
  
  /**
   * Invoke the given method
   * 
   * @param testMethod
   * @param allTestMethods The list of all the test methods
   * @param methodIndex The index of testMethod in the allTestMethods array
   * @param suite
   * @param parameters
   * @param groupMethods
   * 
   * @return a list containing the results of the test methods invocations
   */
  public List<ITestResult> invokeTestMethods(ITestNGMethod testMethod, 
                                             ITestNGMethod[] allTestMethods,
                                             int methodIndex,
                                             XmlSuite suite, 
                                             Map<String, String> parameters, 
                                             ConfigurationGroupMethods groupMethods,
                                             Object[] instances,
                                             ITestContext testContext);

}
