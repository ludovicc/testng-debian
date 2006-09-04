package org.testng.reporters;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.SuiteResult;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

/**
 * This class implements an HTML reporter for suites.
 * 
 * @author cbeust
 */
public class SuiteHTMLReporter implements IReporter {
  public static final String METHODS_CHRONOLOGICAL = "methods.html";
  public static final String METHODS_ALPHABETICAL = "methods-alphabetical.html";
  public static final String GROUPS = "groups.html";
  public static final String CLASSES = "classes.html";
  public static final String REPORTER_OUTPUT = "reporter-output.html";
  public static final String METHODS_NOT_RUN = "methods-not-run.html";
  public static final String TESTNG_XML = "testng.xml.html";
  
  private Map<String, ITestClass> m_classes = new HashMap<String, ITestClass>();
  private String m_outputDirectory;
  
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    m_outputDirectory = outputDirectory;
    
    try {
      HtmlHelper.generateStylesheet(outputDirectory);
    } catch (IOException e) {
      //  TODO Propagate the exception properly.
      e.printStackTrace();
    }
    
    for (int i = 0; i < xmlSuites.size(); i++) {
      
      //
      // Generate the various reports
      //
      XmlSuite xmlSuite = xmlSuites.get(i);
      ISuite suite = suites.get(i);
      generateTableOfContents(xmlSuite, suite);
      generateSuites(xmlSuite, suite);
      generateIndex(xmlSuite, suite);
      generateMain(xmlSuite, suite);
      generateMethodsAndGroups(xmlSuite, suite);
      generateMethodsChronologically(xmlSuite, suite, METHODS_CHRONOLOGICAL, false);
      generateMethodsChronologically(xmlSuite, suite, METHODS_ALPHABETICAL, true);
      generateClasses(xmlSuite, suite);
      generateReporterOutput(xmlSuite, suite);
      generateExcludedMethodsReport(xmlSuite, suite);
      generateXmlFile(xmlSuite, suite);
    }

    generateIndex(suites);
  }
  
  private void generateXmlFile(XmlSuite xmlSuite, ISuite suite) {
    String content = 
      new String(xmlSuite.toXml().replaceAll("<", "&lt;").replaceAll(">", "&gt;"))
          .replaceAll(" ", "&nbsp;").replaceAll("\n", "<br/>");
    
    StringBuffer sb = new StringBuffer("<html>");
    
    sb.append("<head><title>").append("testng.xml for ").append(xmlSuite.getName()).append("</title></head><body><tt>")
      .append(content)
      .append("</tt></body></html>");
    
    Utils.writeFile(getOutputDirectory(xmlSuite), TESTNG_XML, sb);
  }

  /**
   * Generate the main index.html file that lists all the suites
   * and their result
   */
  private void generateIndex(List<ISuite> suites) {
    StringBuffer sb = new StringBuffer();
    String title = "Test results";
    sb.append("<html>\n<head><title>" + title + "</title>")
      .append(HtmlHelper.getCssString("."))
      .append("</head><body>\n")
      .append("<h2><p align='center'>").append(title).append("</p></h2>\n")
      .append("<table border='1' width='80%' class='main-page'>")
      .append("<tr><th>Suite</th><th>Passed</th><th>Failed</th><th>Skipped</th><th>testng.xml</th></tr>\n");
    
    int failedTests = 0;
    int passedTests = 0;
    int skippedTests = 0;
    
    for (ISuite suite : suites) {
      String name = suite.getName();
    
      Map<String, ISuiteResult> results = suite.getResults();
      for (String suiteName : results.keySet()) {
        ISuiteResult result = results.get(suiteName);
        ITestContext context = result.getTestContext();
        failedTests += context.getFailedTests().size();
        passedTests += context.getPassedTests().size();
        skippedTests += context.getSkippedTests().size();
      }
      
      String cls = failedTests > 0 ? "invocation-failed" : (passedTests > 0  ? "invocation-passed" : "invocation-failed");
      sb.append("<tr align='center' class='" + cls + "'>")
        .append("<td><a href='").append(name).append("/index.html'>")
        .append(name).append("</a></td>\n");
      sb.append("<td>" + passedTests + "</td>")
        .append("<td>" + failedTests + "</td>")
        .append("<td>" + skippedTests + "</td>")
        .append("<td><a href='").append(name).append("/").append(TESTNG_XML).append("'>Link").append("</a></td>")
        .append("</tr>");
      
    }
    
    sb.append("</table>")
    .append("</body></html>\n");
  
    File outputFile = new File(m_outputDirectory + File.separatorChar + "index.html");
    Utils.writeFile(outputFile, sb.toString());
  }

  private void generateExcludedMethodsReport(XmlSuite xmlSuite, ISuite suite) {
      Collection<ITestNGMethod> excluded = suite.getExcludedMethods(); 
      StringBuffer sb2 = new StringBuffer("<h2>Methods that were not run</h2><table>\n");
      for (ITestNGMethod method : excluded) {
        Method m = method.getMethod();
        if (m != null) {
          sb2.append("<tr><td>")
          .append(m.getDeclaringClass().getName() + "." + m.getName());
          if(null != method.getDescription() && !"".equals(method.getDescription())) {
            sb2.append("<br/>").append(SP2).append("<i>").append(method.getDescription()).append("</i>");
          }
          sb2.append("</td></tr>\n");
        }
      }
      sb2.append("</table>");
      
      Utils.writeFile(getOutputDirectory(xmlSuite), METHODS_NOT_RUN, sb2);
  }

  private void generateReporterOutput(XmlSuite xmlSuite, ISuite suite) {
    StringBuffer sb = new StringBuffer();
    List<ITestNGMethod> methodsNotRun = new ArrayList<ITestNGMethod>();
    
    //
    // Reporter output
    //
    sb.append("<h2>Reporter output</h2>")
      .append("<table>");
    List<String> output = Reporter.getOutput();
    for (String line : output) {
      sb.append("<tr><td>").append(line).append("</td></tr>\n");
    }
    
    sb.append("</table>");
    
    Utils.writeFile(getOutputDirectory(xmlSuite), REPORTER_OUTPUT, sb);    
  }
//      Map<String, ISuiteResult> results = suite.getResults();
//      for (String testName : results.keySet()) {
//        ISuiteResult sr = results.get(testName);
//        ITestContext testContext = sr.getTestContext();
//        Map<ITestNGMethod, ITestResult>[] all = new Map[] {
//            testContext.getPassedTests(), 
//            testContext.getFailedTests(),
//            testContext.getSkippedTests(),
//            testContext.getFailedButWithinSuccessPercentageTests()
//        };
//        
//        //
//        // Iterate through all the methods
//        for (Map<ITestNGMethod, ITestResult> resultSet : all) {
//          for (ITestNGMethod m : resultSet.keySet()) {
//            // NOTE(cbeust)
//            // Should use a map instead of a list for contains()
//            if (invokedMethods.contains(m)) {
//              sb.append("<tr>\n");
//              ITestResult tr = resultSet.get(m);
//              List<String> output = tr.getExtraOutput().getOutput();
//              for (String line : output) {
//                sb.append("<td><em>[").append(m.getMethodName()).append("]</em></td>\n")
//                  .append("<td>").append(line).append("</td></tr>\n");
//              }
//            }
//            else {
//              methodsNotRun.add(m);
//            }
//          }
//        }
        
//      }  // for testName: result
//    }
    
  private void generateClasses(XmlSuite xmlSuite, ISuite suite) {
    StringBuffer sb = new StringBuffer();
    sb.append("<h2>Test classes</h2>");
    for (ITestClass tc : m_classes.values()) {
      sb.append(generateClass(tc));      
    }
    
    Utils.writeFile(getOutputDirectory(xmlSuite), CLASSES, sb);    
  }
  
  private final static String SP = "&nbsp;";
  private final static String SP2 = SP + SP + SP + SP;
  private final static String SP3 = SP2 + SP2;
  private final static String SP4 = SP3 + SP3;
  
  private String generateClass(ITestClass cls) {
    StringBuffer sb = new StringBuffer();
    sb.append("<hr width='100%'/>")
    .append("<h3>").append(cls.getRealClass().getName()).append("</h3>\n");

    sb.append("<div>").append(SP3).append("Test methods\n")
      .append(dumpMethods(cls.getTestMethods())).append("</div>\n")
      .append("<div>").append(SP3).append("@BeforeClass\n") 
      .append(dumpMethods(cls.getBeforeClassMethods())).append("</div>\n")
      .append("<div>").append(SP3).append("@BeforeMethod\n") 
      .append(dumpMethods(cls.getBeforeTestMethods())).append("</div>\n")
      .append("<div>").append(SP3).append("@AfterMethod\n") 
      .append(dumpMethods(cls.getAfterTestMethods())).append("</div>\n")
      .append("<div>").append(SP3).append("@AfterClass\n") 
      .append(dumpMethods(cls.getAfterClassMethods())).append("</div>\n")
     ;
    
    String result = sb.toString();
    return result;
  }
  
  private String dumpMethods(ITestNGMethod[] testMethods) {
    if(null == testMethods || testMethods.length == 0) {
      return "";
    }
    
    StringBuffer sb = new StringBuffer("<br/>");  //"<table bgcolor=\"#c0c0c0\"/>");
    for (ITestNGMethod tm : testMethods) {
      sb
//      .append("<td>")
      .append(SP4).append(tm.getMethodName()).append("()\n")
      .append(dumpGroups(tm.getGroups()))
      .append("<br/>");
      
//      .append("</td>")
      ;
    }
    
    String result = sb.toString();
    return result;
  }
  
  private String dumpGroups(String[] groups) {
    StringBuffer sb = new StringBuffer();
    
    if (null != groups && groups.length > 0) {
      sb.append(SP4).append("<em>[");
      
      for (String g : groups) {
        sb.append(g).append(" ");
      }
      
      sb.append("]</em><br/>\n");
    }
    
    String result = sb.toString();
    return result;
  }
  
  /**
   * Generate information about the methods that were run
   */
  public static final String AFTER= "&lt;&lt;";
  public static final String BEFORE = "&gt;&gt;";
  private void generateMethodsChronologically(XmlSuite xmlSuite, ISuite suite,
      String outputFileName, boolean alphabetical) 
  {
    StringBuffer sb = new StringBuffer();
    
    sb.append("<h2>Methods run, sorted chronologically</h2>");
    sb.append("<h3>" + BEFORE + " means before, " + AFTER + " means after</h3><p/>");
    
    long startDate = -1;
    Map<Long, StringBuffer> tables = new HashMap<Long, StringBuffer>();
    sb.append("<br/><em>").append(suite.getName()).append("</em><p/>");
    sb.append("<small><i>(Hover the method name to see the test class name)</i></small><p/>\n");
    Collection<ITestNGMethod> invokedMethods = suite.getInvokedMethods();
    
    if (alphabetical) {
      Comparator<? super ITestNGMethod>  alphabeticalComparator = new Comparator(){
        public int compare(Object o1, Object o2) {
          ITestNGMethod m1 = (ITestNGMethod) o1;
          ITestNGMethod m2 = (ITestNGMethod) o2;
          return m1.getMethodName().compareTo(m2.getMethodName());
        }
      };
      Collections.sort((List) invokedMethods, alphabeticalComparator);
    }
    
    for (ITestNGMethod tm : invokedMethods) {
      Long id = new Long(0);      
      StringBuffer table = tables.get(id);
      if (null == table) {
        table = new StringBuffer();
        tables.put(id, table);
        table.append("<table border=\"1\">\n")
          .append("<tr>")
          .append("<th>Time</th>")
          .append("<th>Delta (ms)</th>")
          .append("<th>Suite<br>configuration</th>")
          .append("<th>Test<br>configuration</th>")
          .append("<th>Class<br>configuration</th>")
          .append("<th>Groups<br>configuration</th>")
          .append("<th>Method<br>configuration</th>")
          .append("<th>Test<br>method</th>")
          .append("<th>Thread</th>")
          .append("<th>Instances</th>")
          .append("</tr>\n");
      }
      String methodName = tm.toString();
      boolean bc = tm.isBeforeClassConfiguration();
      boolean ac = tm.isAfterClassConfiguration();
      boolean bt = tm.isBeforeTestConfiguration();
      boolean at = tm.isAfterTestConfiguration();
      boolean bs = tm.isBeforeSuiteConfiguration();
      boolean as = tm.isAfterSuiteConfiguration();
      boolean bg = tm.isBeforeGroupsConfiguration();
      boolean ag = tm.isAfterGroupsConfiguration();
      boolean setUp = tm.isBeforeMethodConfiguration();
      boolean tearDown = tm.isAfterMethodConfiguration();
      boolean isClassConfiguration = bc || ac;
      boolean isGroupsConfiguration = bg || ag;
      boolean isTestConfiguration = bt || at;
      boolean isSuiteConfiguration = bs || as;
      boolean isSetupOrTearDown = setUp || tearDown;
      String configurationClassMethod = isClassConfiguration ? (bc ? BEFORE : AFTER) + methodName : SP;
      String configurationTestMethod = isTestConfiguration ? (bt ? BEFORE : AFTER) + methodName : SP;
      String configurationGroupsMethod = isGroupsConfiguration ? (bg ? BEFORE : AFTER) + methodName : SP;
      String configurationSuiteMethod = isSuiteConfiguration ? (bs ? BEFORE : AFTER) + methodName : SP;
      String setUpOrTearDownMethod = isSetupOrTearDown ? (setUp ? BEFORE : AFTER) + methodName : SP;
      String testMethod = tm.isTest() ? methodName : SP;
      
      StringBuffer instances = new StringBuffer();
      for (long o : tm.getInstanceHashCodes()) {
        instances.append(o).append(" ");
      }
      
      if (startDate == -1) startDate = tm.getDate();
      SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
      String date = format.format(tm.getDate());
      table.append("<tr bgcolor=\"" + createColor(tm) + "\">")
        .append("  <td>").append(date).append("</td> ")
        .append("  <td>").append(tm.getDate() - startDate).append("</td> ")
        .append(td(configurationSuiteMethod))
        .append(td(configurationTestMethod))
        .append(td(configurationClassMethod))
        .append(td(configurationGroupsMethod))
        .append(td(setUpOrTearDownMethod))
        .append(td(testMethod))
        .append("  <td>").append(tm.getId()).append("</td> ")
        .append("  <td>").append(instances).append("</td> ")
        .append("</tr>\n")
        ;
    }
    
    /// Close all the tables
    for (StringBuffer table : tables.values()) {
      table.append("</table>\n");
      sb.append(table.toString());
    }
    Utils.writeFile(getOutputDirectory(xmlSuite), outputFileName, sb);    
    
  }
  
  private static String toHex(int n) {
    return Integer.toHexString(0x10 | n).substring(1).toUpperCase();
  }

  /**
   * Generate a HTML color based on the class of the method
   */
  private String createColor(ITestNGMethod tm) {
    // real class can be null if this client is remote (not serializable)
    long color = tm.getRealClass() != null ? tm.getRealClass().hashCode() & 0xffffff: 0xffffff;
    long[] rgb = {
        ((color & 0xff0000) >> 16) & 0xff,
        ((color & 0x00ff00) >> 8) & 0xff,
        color & 0xff
    };
    // Not too dark
    for (int i = 0; i < rgb.length; i++) {
      if (rgb[i] < 0x60) rgb[i] += 0x60;
    }
    long adjustedColor = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    String result = Long.toHexString(adjustedColor);
    
    return result;
  }

  private String td(String s) {
    StringBuffer result = new StringBuffer();
    String prefix = "";
    
    if (s.startsWith(BEFORE)) {
      prefix = BEFORE;
    }
    else if (s.startsWith(AFTER)) {
      prefix = AFTER;
    }
    
    if (! s.equals(SP)) {
      result.append("<td title=\"").append(s).append("\">");
      int open = s.lastIndexOf("(");
      int start = s.substring(0, open).lastIndexOf(".");
      int end = s.lastIndexOf(")");
      if (start >= 0) {
        result.append(prefix + s.substring(start + 1, open));
      }
      else {
        result.append(prefix + s);
      }
      result.append("</td> \n");
    }
    else {
      result.append("<td>").append(SP).append("</td>");
    }
    
    return result.toString();
  }
  
  private void ppp(String s) {
    System.out.println("[SuiteHTMLReporter] " + s);
  }
  
  /**
   * Generate information about methods and groups
   */
  private void generateMethodsAndGroups(XmlSuite xmlSuite, ISuite suite) {
    StringBuffer sb = new StringBuffer();
    
    Map<String, Collection<ITestNGMethod>> groups = suite.getMethodsByGroups();
    
    sb.append("<h2>Groups used for this test run</h2>");
    if (groups.size() > 0) {
      sb.append("<table border=\"1\">\n")
        .append("<tr> <td align=\"center\"><b>Group name</b></td>")
        .append("<td align=\"center\"><b>Methods</b></td></tr>");

      String[] groupNames = groups.keySet().toArray(new String[groups.size()]);
      Arrays.sort(groupNames);
      for (String group : groupNames) {
        Collection<ITestNGMethod> methods = groups.get(group);
        sb.append("<tr><td>").append(group).append("</td>");
        StringBuffer methodNames = new StringBuffer();
        Map<ITestNGMethod, ITestNGMethod> uniqueMethods = new HashMap<ITestNGMethod, ITestNGMethod>();
        for (ITestNGMethod tm : methods) {
          uniqueMethods.put(tm, tm);
        }
        for (ITestNGMethod tm : uniqueMethods.values()) {
          methodNames.append(tm.toString()).append("<br/>");
        }
        sb.append("<td>" + methodNames.toString() + "</td></tr>\n");
      }
  
      sb.append("</table>\n");
    }
    Utils.writeFile(getOutputDirectory(xmlSuite), GROUPS, sb);    
  }
  
  private void generateIndex(XmlSuite xmlSuite, ISuite sr) {
    StringBuffer index = new StringBuffer()
    .append("<html><head><title>Results for " + sr.getName() + "</title></head>\n")
    .append("<frameset cols=\"26%,74%\">\n")
    .append("<frame src=\"toc.html\" name=\"navFrame\">\n")
    .append("<frame src=\"main.html\" name=\"mainFrame\">\n")
    .append("</frameset>\n")
    .append("</html>\n")
    ;
    
    Utils.writeFile(getOutputDirectory(xmlSuite), "index.html", index);    
  }
  
  private String makeTitle(ISuite suite) {
    return "Results for<br/><em>" + suite.getName() + "</em>";
  }
  
  private void generateMain(XmlSuite xmlSuite, ISuite sr) {
    StringBuffer index = new StringBuffer()
    .append("<html><head><title>Results for " + sr.getName() + "</title></head>\n")
    .append("<body>Select a result on the left-hand pane.</body>")
    .append("</html>\n")
    ;
    
    Utils.writeFile(getOutputDirectory(xmlSuite), "main.html", index);    
  }  
  
  /**
   * 
   */
  private void generateTableOfContents(XmlSuite xmlSuite, ISuite suite) {
    StringBuffer tableOfContents = new StringBuffer();
    
    //
    // Generate methods and groups hyperlinks
    //
    Map<String, ISuiteResult> suiteResults = suite.getResults();
    int groupCount = suite.getMethodsByGroups().size();
    int methodCount = 0;
    for (ISuiteResult sr : suiteResults.values()) {
      ITestNGMethod[] methods = sr.getTestContext().getAllTestMethods();
      methodCount += Utils.calculateInvokedMethodCount(methods);
        
      // Collect testClasses
      for (ITestNGMethod tm : methods) {
        ITestClass tc = tm.getTestClass();
        m_classes.put(tc.getRealClass().getName(), tc);
      }
    }
      
    String name = "Results for " + suite.getName();
    tableOfContents
        .append("<html>\n")
        .append("<head>\n")
        .append("<title>" + name + "</title>\n")
        .append(HtmlHelper.getCssString())
        .append("</head>\n")
        ;
    tableOfContents
        .append("<body>\n")
        .append("<h3><p align=\"center\">" + makeTitle(suite) + "</p></h3>\n")
        .append("<table border='1' width='100%'>\n")
        .append("<tr valign='top'>\n")
          .append("<td>")
            .append(suiteResults.size()).append(" ").append(pluralize(suiteResults.size(), "test"))
          .append("</td>\n")
          .append("<td>")
              .append("<a target='mainFrame' href='").append(CLASSES).append("'>")
              .append(m_classes.size() + " " + pluralize(m_classes.size(), "class"))
              .append("</a>")
          .append("</td>\n")
          .append("<td>" + methodCount + " " + pluralize(methodCount, "method") + ":<br/>\n")
            .append("&nbsp;&nbsp;<a target='mainFrame' href='").append(METHODS_CHRONOLOGICAL).append("'>").append("chronological</a><br/>\n")
            .append("&nbsp;&nbsp;<a target='mainFrame' href='").append(METHODS_ALPHABETICAL).append("\'>").append("alphabetical</a><br/>\n")
            .append("&nbsp;&nbsp;<a target='mainFrame' href='").append(METHODS_NOT_RUN).append("'>not run (" + suite.getExcludedMethods().size() + ")</a>")
          .append("</td>\n")
        .append("</tr>\n")

        .append("<tr>\n")
        .append("<td><a target='mainFrame' href='").append(GROUPS).append("'>").append(groupCount + pluralize(groupCount, " group") + "</a></td>\n")
        .append("<td><a target='mainFrame' href='").append(REPORTER_OUTPUT).append("'>reporter output</a></td>\n")
        .append("<td><a target='mainFrame' href='").append(TESTNG_XML).append("'>testng.xml</a></td>\n")
        .append("</tr>")
        .append("</table>");
      
      //
      // Generate results for individual tests
      //
      
      // Order the results so we can show the failures first, then the skip and
      // finally the successes
      Map<String, ISuiteResult> redResults = new HashMap<String, ISuiteResult>();
      Map<String, ISuiteResult> yellowResults = new HashMap<String, ISuiteResult>();
      Map<String, ISuiteResult> greenResults = new HashMap<String, ISuiteResult>();
      
      for (String suiteName : suiteResults.keySet()) {
        ISuiteResult sr = suiteResults.get(suiteName);
        ITestContext tc = sr.getTestContext();
        int failed = tc.getFailedTests().size(); 
        int skipped = tc.getSkippedTests().size();
        int passed = tc.getPassedTests().size();
        
        if (failed > 0) {
          redResults.put(suiteName, sr);
        }
        else if (skipped > 0) {
          yellowResults.put(suiteName, sr);
        }
        else if (passed > 0) {
          greenResults.put(suiteName, sr);
        }
        else {
          redResults.put(suiteName, sr);
        }
      }
  
      
      ISuiteResult[][] results = new ISuiteResult[][] {
        sortResults(redResults.values()), sortResults(yellowResults.values()), sortResults(greenResults.values())
      };
      
      String[] colors = {"failed", "skipped", "passed"};
      for (int i = 0; i < colors.length; i++) {
        ISuiteResult[] r = results[i];
        for (ISuiteResult sr: r) {
          String suiteName = sr.getTestContext().getName();
          generateSuiteResult(suiteName, sr, colors[i], tableOfContents, m_outputDirectory);
        }
      }
    
    tableOfContents.append("</body></html>");
    Utils.writeFile(getOutputDirectory(xmlSuite), "toc.html", tableOfContents);
  }
  
  private String pluralize(int count, String singular) {
    return count > 1 ? (singular.endsWith("s") ? singular + "es" : singular + "s") : singular;
  }

  private String getOutputDirectory(XmlSuite xmlSuite) {
    return m_outputDirectory + File.separatorChar + xmlSuite.getName();
  }

  private ISuiteResult[] sortResults(Collection<ISuiteResult> r) {
    ISuiteResult[] result = r.toArray(new ISuiteResult[r.size()]);
    Arrays.sort(result);
    return result;
  }

  private void generateSuiteResult(String suiteName, 
                                   ISuiteResult sr, 
                                   String cssClass, 
                                   StringBuffer tableOfContents, 
                                   String outputDirectory)
  {
    ITestContext tc = sr.getTestContext();
    int passed = tc.getPassedTests().size();
    int failed = tc.getFailedTests().size(); 
    int skipped = tc.getSkippedTests().size();
    String baseFile = tc.getName();
    tableOfContents
      .append("\n<table width='100%' class='test-").append(cssClass).append("'>\n")
      .append("<tr><td>\n")
      .append("<table style='width: 100%'><tr>")
      .append("<td valign='top'>")
      .append(suiteName).append(" (").append(passed).append("/").append(failed).append("/").append(skipped).append(")")
      .append("</td>")
      .append("<td valign='top' align='right'>\n")
      .append("  <a href='" + baseFile + ".html' target='mainFrame'>Results</a>\n")
//      .append("  <a href=\"" + baseFile + ".out\" target=\"mainFrame\"\">Output</a>\n")
//      .append("&nbsp;&nbsp;<a href=\"file://" + baseFile + ".properties\" target=\"mainFrame\"\">Property file</a><br>\n")
      .append("</td>")
      .append("</tr></table>\n")
      .append("</td></tr><p/>\n")
      ;
    
    tableOfContents.append("</table>\n");
  }
  
  private void generateSuites(XmlSuite xmlSuite, ISuite suite) {
    Map<String, ISuiteResult> suiteResults = suite.getResults();

    for (String propertyFileName : suiteResults.keySet()) {
      SuiteResult sr = (SuiteResult) suiteResults.get(propertyFileName);
      ITestContext testContext = sr.getTestContext();
      
      StringBuffer sb = new StringBuffer();
      for (String name : suiteResults.keySet()) {
        ISuiteResult suiteResult = suiteResults.get(name);
        sb.append(suiteResult.toString());
      }
      Utils.writeFile(getOutputDirectory(xmlSuite), testContext.getName() + ".properties", sb);
    }    
  }
}
