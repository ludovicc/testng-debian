package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestHelper {

  public static XmlSuite createSuite(String cls, String suiteName) {
    XmlSuite result = new XmlSuite();
    result.setName(suiteName);
    
    XmlTest test = new XmlTest(result);
    test.setName("TmpTest");
    List<XmlClass> classes = new ArrayList<XmlClass>();
    classes.add(new XmlClass(cls));
    test.setXmlClasses(classes);
    
    return result;
  }
  
  public static TestNG createTestNG(String outputDir) {
    return createTestNG(null, outputDir);
  }

  public static TestNG createTestNG() {
    return createTestNG(null, null);
  }

  public static TestNG createTestNG(XmlSuite suite) {
    return createTestNG(suite, System.getProperty("java.io.tmpdir"));
  }

  public static TestNG createTestNG(XmlSuite suite, String outputDir) {
    TestNG result = new TestNG();
    if (suite != null) {
      List<XmlSuite> suites = new ArrayList<XmlSuite>();
      suites.add(suite);
      result.setXmlSuites(suites);
    }
    if (outputDir == null) {
      outputDir = createRandomDirectory().getAbsolutePath();
    }
    result.setOutputDirectory(outputDir);
    result.setVerbose(-1);
    
    return result;
  }
  
  public static File createRandomDirectory() {
    String dir = System.getProperty("java.io.tmpdir");
    String name = "testng-tmp-" + new Random(System.currentTimeMillis()).nextInt();
    File result = new File(dir + File.separatorChar + name);
    result.deleteOnExit();
    result.mkdirs();
    
    return result;
  }

  private static void ppp(String string) {
    System.out.println("[TestHelper] " + string);
  }

}
