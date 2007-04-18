package org.testng.remote.strprotocol;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.internal.Utils;


/**
 * An <code>IStringMessage</code> implementation for test results events.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class TestResultMessage implements IStringMessage {
  private static final Object[] EMPTY_PARAMS= new Object[0];
  private static final Class[] EMPTY_TYPES= new Class[0];
  
  protected int    m_messageType;
  protected String m_suiteName;
  protected String m_testName;
  protected String m_testClassName;
  protected String m_testMethodName;
  protected String m_stackTrace;
  protected long m_startMillis;
  protected long m_endMillis;
  protected String[] m_parameters= new String[0];
  protected String[] m_paramTypes= new String[0];

  TestResultMessage(final int resultType,
                    final String suiteName,
                    final String testName,
                    final String className,
                    final String methodName,
                    final String[] params,
                    final long startMillis,
                    final long endMillis,
                    final String stackTrace) 
  {
    init(resultType,
         suiteName,
         testName,
         className,
         methodName,
         stackTrace,
         startMillis,
         endMillis,
         extractParams(params),
         extractParamTypes(params)
         
    );
  }

  public TestResultMessage(final String suiteName, 
                           final String testName, 
                           final ITestResult result) 
  {
    String stackTrace = null;

    if((ITestResult.FAILURE == result.getStatus())
      || (ITestResult.SUCCESS_PERCENTAGE_FAILURE == result.getStatus())) {
      StringWriter sw = new StringWriter();
      PrintWriter  pw = new PrintWriter(sw);
      Throwable cause= result.getThrowable();
      if (null != cause) {
        cause.printStackTrace(pw);
        stackTrace = sw.getBuffer().toString();
      }
      else {
        stackTrace= "unknown stack trace";
      }
    }
    else if(ITestResult.SKIP == result.getStatus() 
        && (result.getThrowable() != null && SkipException.class.isAssignableFrom(result.getThrowable().getClass()))) {
      stackTrace= result.getThrowable().getMessage();
    }

    init(MessageHelper.TEST_RESULT + result.getStatus(),
         suiteName,
         testName,
         result.getTestClass().getName(),
         result.getMethod().getMethod().getName(),
         stackTrace,
         result.getStartMillis(),
         result.getEndMillis(),
         toString(result.getParameters(), result.getMethod().getMethod().getParameterTypes()),
         toString(result.getMethod().getMethod().getParameterTypes())
    );
  }
  
  public TestResultMessage(final ITestContext testCtx, final ITestResult result) {
    this(testCtx.getSuite().getName(), testCtx.getName(), result);
  }

  private void init(final int resultType,
                    final String suiteName,
                    final String testName,
                    final String className,
                    final String methodName,
                    final String stackTrace,
                    final long startMillis,
                    final long endMillis,
                    final String[] parameters,
                    final String[] types) {
    m_messageType = resultType;
    m_suiteName = suiteName;
    m_testName = testName;
    m_testClassName = className;
    m_testMethodName = methodName;
    m_stackTrace = stackTrace;
    m_startMillis= startMillis;
    m_endMillis= endMillis;
    m_parameters= parameters;
    m_paramTypes= types;
  }

  public int getResult() {
    return m_messageType;
  }

  public String getMessageAsString() {
    StringBuffer buf = new StringBuffer();
    StringBuffer parambuf = new StringBuffer();
    
    if(null != m_parameters && m_parameters.length > 0) {
      for (int j = 0; j < m_parameters.length; j++) {
        if (j > 0) parambuf.append(MessageHelper.PARAM_DELIMITER);
        parambuf.append(m_paramTypes[j] + ":" + m_parameters[j]);
      }
    }

    buf.append(m_messageType)
       .append(MessageHelper.DELIMITER)
       .append(m_suiteName)
       .append(MessageHelper.DELIMITER)
       .append(m_testName)
       .append(MessageHelper.DELIMITER)
       .append(m_testClassName)
       .append(MessageHelper.DELIMITER)
       .append(m_testMethodName)
       .append(MessageHelper.DELIMITER)
       .append(parambuf)
       .append(MessageHelper.DELIMITER)
       .append(m_startMillis)
       .append(MessageHelper.DELIMITER)
       .append(m_endMillis)
       .append(MessageHelper.DELIMITER)
       .append(MessageHelper.replaceNewLine(m_stackTrace))
       ;

    return buf.toString();
  }

  public String getSuiteName() {
    return m_suiteName;
  }

  public String getTestClass() {
    return m_testClassName;
  }

  public String getMethod() {
    return m_testMethodName;
  }

  public String getName() {
    return m_testName;
  }
  
  public String getStackTrace() {
    return m_stackTrace;
  }
  
  public long getEndMillis() {
    return m_endMillis;
  }
  
  public long getStartMillis() {
    return m_startMillis;
  }

  public String[] getParameters() {
    return m_parameters;
  }
  
  public String[] getParameterTypes() {
    return m_paramTypes;
  }
  
  public String toDisplayString() {
    StringBuffer buf= new StringBuffer(m_testMethodName);
    
    if(null != m_parameters && m_parameters.length > 0) {
      buf.append("(");
      for(int i= 0; i < m_parameters.length; i++) {
        if(i > 0) buf.append(", ");
        if("java.lang.String".equals(m_paramTypes[i]) && !("null".equals(m_parameters[i]) || "\"\"".equals(m_parameters[i]))) {
          buf.append("\"").append(m_parameters[i]).append("\"");
        }
        else {
          buf.append(m_parameters[i]);
        }

      }
      buf.append(")");
    }
    
    return buf.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    final TestResultMessage that = (TestResultMessage) o;

    if(m_suiteName != null ? !m_suiteName.equals(that.m_suiteName) : that.m_suiteName != null) return false;
    if(m_testName != null ? !m_testName.equals(that.m_testName) : that.m_testName != null) return false;
    if(m_testClassName != null ? !m_testClassName.equals(that.m_testClassName) : that.m_testClassName != null) return false;
    String toDisplayString= toDisplayString();
    if(toDisplayString != null ? !toDisplayString.equals(that.toDisplayString()) : that.toDisplayString() != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = (m_suiteName != null ? m_suiteName.hashCode() : 0);
    result = 29 * result + (m_testName != null ? m_testName.hashCode() : 0);
    result = 29 * result + m_testClassName.hashCode();
    result = 29 * result + toDisplayString().hashCode();
    return result;
  }
  
  private String[] toString(Object[] objects, Class[] objectClasses) {
    if(null == objects) return new String[0];
    List<String> result= new ArrayList<String>(objects.length);
    for(Object o: objects) {
      if(null == o) {
        result.add("null");
      }
      else {
        String tostring= o.toString();
        if("".equals(tostring)) {
          result.add("\"\"");
        }
        else {
          result.add(MessageHelper.replaceNewLine(tostring));
        }
      }
    }
    
    return result.toArray(new String[result.size()]);
  }
  
  private String[] toString(Class[] classes) {
    if(null == classes) return new String[0];
    List<String> result= new ArrayList<String>(classes.length);
    for(Class cls: classes) {
      result.add(cls.getName());
    }
    
    return result.toArray(new String[result.size()]);
  }
  
  /**
   * @param params
   * @return
   */
  private String[] extractParamTypes(String[] params) {
    List<String> result= new ArrayList<String>(params.length);
    for(String s: params) {
      result.add(s.substring(0, s.indexOf(':')));
    }
    
    return result.toArray(new String[result.size()]);
  }

  private String[] extractParams(String[] params) {
    List<String> result= new ArrayList<String>(params.length);
    for(String s: params) {
      result.add(MessageHelper.replaceNewLineReplacer(s.substring(s.indexOf(':') + 1)));
    }
    
    return result.toArray(new String[result.size()]);
  }
}
