package org.testng.reporters;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener;
import org.testng.internal.Utils;

/**
 * A JUnit XML report generator (replacing the original JUnitXMLReporter that was
 * based on XML APIs).
 *
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class JUnitXMLReporter implements IResultListener {
  private static final Pattern ENTITY= Pattern.compile("&[a-zA-Z]+;.*");
  private static final Pattern LESS= Pattern.compile("<");
  private static final Pattern GREATER= Pattern.compile(">");
  private static final Pattern SINGLE_QUOTE = Pattern.compile("'");
  private static final Pattern QUOTE = Pattern.compile("\"");
  private static final Map<String, Pattern> ATTR_ESCAPES= new HashMap<String, Pattern>();
  
  static {
    ATTR_ESCAPES.put("&lt;", LESS);
    ATTR_ESCAPES.put("&gt;", GREATER);
    ATTR_ESCAPES.put("&apos;", SINGLE_QUOTE);
    ATTR_ESCAPES.put("&quot;", QUOTE);
  }
  

  /**
   * keep lists of all the results
   */
  private int m_numPassed= 0;
  private int m_numFailed= 0;
  private int m_numSkipped= 0;
  private int m_numFailedButIgnored= 0;
  private List<ITestResult> m_allTests= Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestResult> m_configIssues= Collections.synchronizedList(new ArrayList<ITestResult>());
  private Map<String, String> m_fileNameMap = new HashMap<String, String>();
  private int m_fileNameIncrementer = 0;

  public void onTestStart(ITestResult result) {
  }

  /**
   * Invoked each time a test succeeds.
   */
  public void onTestSuccess(ITestResult tr) {
    m_allTests.add(tr);
    m_numPassed++;
  }

  public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
    m_allTests.add(tr);
    m_numFailedButIgnored++;
  }

  /**
   * Invoked each time a test fails.
   */
  public void onTestFailure(ITestResult tr) {
    m_allTests.add(tr);
    m_numFailed++;
  }

  /**
   * Invoked each time a test is skipped.
   */
  public void onTestSkipped(ITestResult tr) {
    m_allTests.add(tr);
    m_numSkipped++;
  }

  /**
   * Invoked after the test class is instantiated and before
   * any configuration method is called.
   *
   */
  public void onStart(ITestContext context) {
	  
  }

  /**
   * Invoked after all the tests have run and all their
   * Configuration methods have been called.
   *
   */
  public void onFinish(ITestContext context) {
	generateReport(context);
    resetAll();
  }

  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationFailure(org.testng.ITestResult)
   */
  public void onConfigurationFailure(ITestResult itr) {
    m_configIssues.add(itr);
  }

  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationSkip(org.testng.ITestResult)
   */
  public void onConfigurationSkip(ITestResult itr) {
    m_configIssues.add(itr);
  }

  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationSuccess(org.testng.ITestResult)
   */
  public void onConfigurationSuccess(ITestResult itr) {
  }

  /**
   * generate the XML report given what we know from all the test results
   */
  protected void generateReport(ITestContext context) {
	  
      XMLStringBuffer document= new XMLStringBuffer("");
      document.setXmlDetails("1.0", "UTF-8");
      Properties attrs= new Properties();
      attrs.setProperty(XMLConstants.ATTR_NAME, encodeAttr(context.getName())); // ENCODE
      attrs.setProperty(XMLConstants.ATTR_TESTS, "" + m_allTests.size());
      attrs.setProperty(XMLConstants.ATTR_FAILURES, "" + m_numFailed);
      attrs.setProperty(XMLConstants.ATTR_ERRORS, "0");
      attrs.setProperty(XMLConstants.ATTR_TIME, ""
          + ((context.getEndDate().getTime() - context.getStartDate().getTime()) / 1000.0));

      document.push(XMLConstants.TESTSUITE, attrs);
      document.addEmptyElement(XMLConstants.PROPERTIES);

      for(ITestResult tr : m_configIssues) {
        createElement(document, tr);
      }
      for(ITestResult tr : m_allTests) {
        createElement(document, tr);
      }

      document.pop();
      Utils.writeUtf8File(context.getOutputDirectory(),generateFileName(context) + ".xml", document.toXML());
  }

  private void createElement(XMLStringBuffer doc, ITestResult tr) {
    Properties attrs= new Properties();
    long elapsedTimeMillis= tr.getEndMillis() - tr.getStartMillis();
    String name= tr.getMethod().isTest() ? tr.getName() : Utils.detailedMethodName(tr.getMethod(), false);
    attrs.setProperty(XMLConstants.ATTR_NAME, name);
    attrs.setProperty(XMLConstants.ATTR_CLASSNAME, tr.getTestClass().getRealClass().getName());
    attrs.setProperty(XMLConstants.ATTR_TIME, "" + (((double) elapsedTimeMillis) / 1000));

    if((ITestResult.FAILURE == tr.getStatus()) || (ITestResult.SKIP == tr.getStatus())) {
      doc.push(XMLConstants.TESTCASE, attrs);

      if(ITestResult.FAILURE == tr.getStatus()) {
        createFailureElement(doc, tr);
      }
      else if(ITestResult.SKIP == tr.getStatus()) {
        createSkipElement(doc, tr);
      }

      doc.pop();
    }
    else {
      doc.addEmptyElement(XMLConstants.TESTCASE, attrs);
    }
  }

  private void createFailureElement(XMLStringBuffer doc, ITestResult tr) {
    Properties attrs= new Properties();
    Throwable t= tr.getThrowable();
    if(t != null) {
      attrs.setProperty(XMLConstants.ATTR_TYPE, t.getClass().getName());
      String message= t.getMessage();
      if((message != null) && (message.length() > 0)) {
        attrs.setProperty(XMLConstants.ATTR_MESSAGE, encodeAttr(message)); // ENCODE
      }
      doc.push(XMLConstants.FAILURE, attrs);
      doc.addCDATA(Utils.stackTrace(t, false)[0]);
      doc.pop();
    }
    else {
      doc.addEmptyElement(XMLConstants.FAILURE); // THIS IS AN ERROR
    }
  }

  private void createSkipElement(XMLStringBuffer doc, ITestResult tr) {
    doc.addEmptyElement("skipped");
  }
  
  private String encodeAttr(String attr) {
    String result= replaceAmpersand(attr, ENTITY);
    for(Map.Entry<String, Pattern> e: ATTR_ESCAPES.entrySet()) {
      result= e.getValue().matcher(result).replaceAll(e.getKey());
    }

    return result;
  }

  private String replaceAmpersand(String str, Pattern pattern) {
    int start = 0;
    int idx = str.indexOf('&', start);
    if(idx == -1) return str;
    StringBuffer result= new StringBuffer();
    while(idx != -1) {
      result.append(str.substring(start, idx));
      if(pattern.matcher(str.substring(idx)).matches()) {
        // do nothing it is an entity;
        result.append("&");
      }
      else {
        result.append("&amp;");
      }
      start= idx + 1;
      idx= str.indexOf('&', start);
    }
    result.append(str.substring(start));
    
    return result.toString();
  }
  
  
  /**
	 * Reset all member variables for next test.
	 * */
	private void resetAll() {
		m_allTests = Collections.synchronizedList(new ArrayList<ITestResult>());
		m_configIssues = Collections
				.synchronizedList(new ArrayList<ITestResult>());
		m_numFailed = 0;
		m_numFailedButIgnored = 0;
		m_numPassed = 0;
		m_numSkipped = 0;
	}
	
	/**
	 * @author Borojevic Created this method to guarantee unique file names for
	 *         reports.<br>
	 *         Also, this will guarantee that the old reports are overwritten
	 *         when tests are run again.
	 * @param context
	 *            test context
	 * @return unique name for the file associated with this test context.
	 * */
	private String generateFileName(ITestContext context) {
		String fileName = null;
		String keyToSearch = context.getSuite().getName() + context.getName();
		if (m_fileNameMap.get(keyToSearch) == null) {
			fileName = context.getName();
		} else {
			fileName = context.getName() + m_fileNameIncrementer++;
		}

		m_fileNameMap.put(keyToSearch, fileName);
		return fileName;
	}
}
