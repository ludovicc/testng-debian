package test.annotationtransformer;

import java.util.List;

import junit.framework.Assert;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import test.BaseTest;

public class AnnotationTransformerTest extends BaseTest {
  
  /**
   * @testng.test
   */
  public void verifyInvocationCount() {
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setAnnotations(TestNG.JAVADOC_ANNOTATION_TYPE);
    tng.setSourcePath("test-14/src");
    tng.setAnnotationTransformer(new MyTransformer());
    tng.setTestClasses(new Class[] { AnnotationTransformerSampleTest.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    
    tng.run();
    
    List passed = tla.getPassedTests();
    Assert.assertEquals(15, passed.size());
  }

}
