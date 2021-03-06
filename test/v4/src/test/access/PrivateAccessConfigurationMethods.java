package test.access;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * Test that private and protected @Configuration methods are run
 * 
 * @author cbeust
 */
public class PrivateAccessConfigurationMethods 
  extends BasePrivateAccessConfigurationMethods 
{
  private boolean m_private = false;
  private boolean m_default = false;
  private boolean m_protected = false;
  private boolean m_public = false;
  
  @Configuration(beforeTestMethod = true)
  private void privateConfBeforeMethod() {
    m_private = true;
  }

  @Configuration(beforeTestMethod = true)
  void defaultConfBeforeMethod() {
    m_default = true;
  }
  
  @Configuration(beforeTestMethod = true)
  protected void protectedConfBeforeMethod() {
    m_protected = true;
  }

  @Configuration(beforeTestMethod = true)
  public void publicConfBeforeMethod() {
    m_public = true;
  }
  
  @Test
  public void allAccessModifiersConfiguration() {
    Assert.assertTrue(m_private, "private @Configuration should have been run");
    Assert.assertTrue(m_default, "default @Configuration should have been run");
    Assert.assertTrue(m_protected, "protected @Configuration should have been run");
    Assert.assertTrue(m_public, "public @Configuration should have been run");
    
    Assert.assertTrue(m_baseProtected, "protected base @Configuration should have been run"); 
    Assert.assertTrue(m_baseDefault, "default base @Configuration should have been run"); 
    Assert.assertTrue(m_basePrivate, "private base @Configuration should not have been run"); 
    
  }
}
