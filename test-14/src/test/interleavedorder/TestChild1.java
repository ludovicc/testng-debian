package test.interleavedorder;


public class TestChild1 extends BaseTestClass {
  /**
   * @testng.before-class
   */
  public void beforeTestChildOneClass() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".beforeTestChildOneClass");
  }
  
  /**
   * @testng.after-class
   */
  public void afterTestChildOneClass() {
    InterleavedInvocationTest.LOG.append(getClass().getName() + ".afterTestChildOneClass");
  }
}
