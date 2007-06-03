package org.testng.reporters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.internal.MethodHelper;
import org.testng.internal.Utils;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * This reporter is responsible for creating testng-failed.xml
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class FailedReporter extends TestListenerAdapter implements IReporter {
  public static final String TESTNG_FAILED_XML = "testng-failed.xml";
  
  private XmlSuite m_xmlSuite;

  public FailedReporter() {
  }

  public FailedReporter(XmlSuite xmlSuite) {
    m_xmlSuite = xmlSuite;
  }

  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    for(int i= 0; i < xmlSuites.size(); i++) {
      generateFailureSuite(xmlSuites.get(i), suites.get(i), outputDirectory);
    }
  }

  protected void generateFailureSuite(XmlSuite xmlSuite, ISuite suite, String outputDir) {
    XmlSuite failedSuite = (XmlSuite) xmlSuite.clone();
    failedSuite.setName("Failed suite [" + xmlSuite.getName() + "]");
    m_xmlSuite= failedSuite;

    Map<String, XmlTest> xmlTests= new HashMap<String, XmlTest>();
    for(XmlTest xmlT: xmlSuite.getTests()) {
      xmlTests.put(xmlT.getName(), xmlT);
    }
    
    Map<String, ISuiteResult> results= suite.getResults();
    
    for(String name: results.keySet()) {
      ISuiteResult suiteResult= results.get(name);
      ITestContext testContext= suiteResult.getTestContext();

      generateXmlTest(suite, 
                      xmlTests.get(testContext.getName()), 
                      testContext, 
                      testContext.getFailedTests().getAllResults(), 
                      testContext.getSkippedTests().getAllResults());
    }

    if(null != failedSuite.getTests() && failedSuite.getTests().size() > 0) {
      Utils.writeUtf8File(outputDir, TESTNG_FAILED_XML, failedSuite.toXml());
      Utils.writeUtf8File(suite.getOutputDirectory(), TESTNG_FAILED_XML, failedSuite.toXml());
    }
  }

  /**
   * Do not rely on this method. The class is used as <code>IReporter</code>.
   * 
   * @see org.testng.TestListenerAdapter#onFinish(org.testng.ITestContext)
   * @deprecated this class is used now as IReporter
   */
  @Override
  public void onFinish(ITestContext context) {
    // Delete the previous file
//    File f = new File(context.getOutputDirectory(), getFileName(context));
//    f.delete();
    
    // Calculate the methods we need to rerun :  failed tests and
    // their dependents
//    List<ITestResult> failedTests = getFailedTests();
//    List<ITestResult> skippedTests = getSkippedTests();
  }
  
  private void generateXmlTest(ISuite suite, 
                               XmlTest xmlTest, 
                               ITestContext context, 
                               Collection<ITestResult> failedTests, 
                               Collection<ITestResult> skippedTests) {
    // Note:  we can have skipped tests and no failed tests
    // if a method depends on nonexistent groups
    if (skippedTests.size() > 0 || failedTests.size() > 0) {
      Map<ITestNGMethod, ITestNGMethod> methodsToReRun = new HashMap<ITestNGMethod, ITestNGMethod>();
      
      // Get the transitive closure of all the failed methods and the methods
      // they depend on
      Collection<ITestResult> tests = failedTests.isEmpty() ? skippedTests : failedTests;
      for (ITestResult failedTest : tests) {
        ITestNGMethod current = failedTest.getMethod();
        if (current.isTest()) {
          methodsToReRun.put(current, current);
          ITestNGMethod method = failedTest.getMethod();
          // Don't count configuration methods
          if (method.isTest()) {
            List<ITestNGMethod> methodsDependedUpon = MethodHelper.getMethodsDependedUpon(method, context.getAllTestMethods());
            
            for (ITestNGMethod m : methodsDependedUpon) {
              if (m.isTest()) {
                methodsToReRun.put(m, m);
              }
            }
          }
        }
      }
      
      //
      // Now we have all the right methods.  Go through the list of
      // all the methods that were run and only pick those that are
      // in the methodToReRun map.  Since the methods are already
      // sorted, we don't need to sort them again.
      //
      List<ITestNGMethod> result = new ArrayList<ITestNGMethod>();
      for (ITestNGMethod m : context.getAllTestMethods()) {
        if (null != methodsToReRun.get(m)) {
          result.add(m);
        }
      }

      methodsToReRun.clear();
      Collection<ITestNGMethod> invoked= suite.getInvokedMethods();
      for(ITestNGMethod tm: invoked) {
        if(!tm.isTest()) {
          methodsToReRun.put(tm, tm);
        }
      }
      
      result.addAll(methodsToReRun.keySet());
      createXmlTest(context, result, xmlTest);
    }
  }
  
  private void addMethods(Map<ITestNGMethod, ITestNGMethod> map, ITestNGMethod[] methods) {
    if(null == methods) {
      return;
    }
    for(ITestNGMethod tm: methods) {
      map.put(tm, tm);
    }
  }
  
  /**
   * Generate testng-failed.xml
   */
  private void createXmlTest(ITestContext context, List<ITestNGMethod> methods, XmlTest srcXmlTest) {
    XmlTest xmlTest = new XmlTest(m_xmlSuite);
    xmlTest.setName(context.getName() + "(failed)");
    xmlTest.setAnnotations(srcXmlTest.getAnnotations());
    xmlTest.setBeanShellExpression(srcXmlTest.getExpression());
    xmlTest.setIncludedGroups(srcXmlTest.getIncludedGroups());
    xmlTest.setExcludedGroups(srcXmlTest.getExcludedGroups());
    xmlTest.setParallel(srcXmlTest.getParallel());
    xmlTest.setParameters(srcXmlTest.getParameters());
    xmlTest.setJUnit(srcXmlTest.isJUnit());
    List<XmlClass> xmlClasses = createXmlClasses(methods);
    xmlTest.setXmlClasses(xmlClasses);
  }
    
  /**
   * @param methods The methods we want to represent
   * @return A list of XmlClass objects (each representing a <class> tag) based
   * on the parameter methods
   */
  private List<XmlClass> createXmlClasses(List<ITestNGMethod> methods) {
    List<XmlClass> result = new ArrayList<XmlClass>();
    Map<Class, Set<ITestNGMethod>> methodsMap= new HashMap<Class, Set<ITestNGMethod>>(); 
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    
    for (ITestNGMethod m : methods) {
      Object[] instances= m.getInstances();
      Class clazz= instances == null || instances.length == 0 ? m.getRealClass() : instances[0].getClass();
      Set<ITestNGMethod> methodList= methodsMap.get(clazz);
      if(null == methodList) {
        methodList= new HashSet<ITestNGMethod>();
        methodsMap.put(clazz, methodList);
      }
      methodList.add(m);
    }
    
    for(Map.Entry<Class, Set<ITestNGMethod>> entry: methodsMap.entrySet()) {
      Class clazz= entry.getKey();
      Set<ITestNGMethod> methodList= entry.getValue();
      if(hasTestMethod(methodList)) {
        XmlClass xmlClass= new XmlClass(clazz.getName());
        List<String> methodNames= new ArrayList<String>(methodList.size());
        for(ITestNGMethod m: methodList) {
          methodNames.add(m.getMethod().getName());
        }
        xmlClass.setIncludedMethods(methodNames);
        result.add(xmlClass);
      }
    }
        
    return result;
  }

  private boolean hasTestMethod(Collection<ITestNGMethod> methods) {
    for(ITestNGMethod m: methods) {
      if(m.isTest()) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * TODO:  we might want to make that more flexible in the future, but for
   * now, hardcode the file name
   */
  private String getFileName(ITestContext context) {
    return TESTNG_FAILED_XML;
  }
  
  private static void ppp(String s) {
    System.out.println("[FailedReporter] " + s);
  }
}
