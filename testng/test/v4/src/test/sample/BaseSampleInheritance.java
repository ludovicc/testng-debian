package test.sample;


import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Cedric Beust, Apr 30, 2004
 * 
 */
public class BaseSampleInheritance {

  protected List<String> m_configurations = new ArrayList<String>();
  
  protected void addConfiguration(String c) {
    m_configurations.add(c);
    }
  
  protected boolean m_invokedBaseMethod = false;
  
  @Test(groups = { "inheritedTestMethod" })
  public void baseMethod() {
    m_invokedBaseMethod = true;
  }  

  protected boolean m_invokedBaseConfiguration = false;
  
  @Configuration(beforeTestClass = true)
  public void baseConfiguration() {
    m_invokedBaseConfiguration = true;
  }

  @Configuration(beforeTestClass = true,
                 groups = { "configuration1" }, 
                 dependsOnGroups = { "configuration0" })
  public void configuration1() {
//    System.out.println("CONFIGURATION 1");
    addConfiguration("configuration1");
  }

  @Test(dependsOnGroups = { "inheritedTestMethod" })
  public void testBooleans() {
    assert m_invokedBaseMethod : "Didn't invoke test method in base class";
    assert m_invokedBaseConfiguration : "Didn't invoke configuration method in base class";
  }
  
}
