package org.testng;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Assertion tool class. Presents assertion methods with a more natural parameter order.
 * The order is always <B>actualValue</B>, <B>expectedValue</B> [, message].
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class Assert {
  
  /**
   * Protect constructor since it is a static only class
   */
  protected Assert() {
    // hide constructor
  }
  
  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionError with the given message.
   * @param condition the condition to evaluate 
   * @param message the assertion error message 
   */
  static public void assertTrue(boolean condition, String message) {
    if(!condition) {
      failNotEquals(new Boolean(condition), new Boolean(true), message);
    }
  }
  
  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionError.
   * @param condition the condition to evaluate 
   */
  static public void assertTrue(boolean condition) {
    assertTrue(condition, null);
  }
  
  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionError with the given message.
   * @param condition the condition to evaluate 
   * @param message the assertion error message 
   */
  static public void assertFalse(boolean condition, String message) {
    if(condition) {
      failNotEquals(new Boolean(condition), new Boolean(false), message); // TESTNG-81
    }
  }
  
  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionError.
   * @param condition the condition to evaluate 
   */
  static public void assertFalse(boolean condition) {
    assertFalse(condition, null);
  }
  
  /**
   * Fails a test with the given message and wrapping the original exception.
   * 
   * @param message the assertion error message
   * @param realCause the original exception
   */
  static public void fail(String message, Throwable realCause) {
    AssertionError ae = new AssertionError(message);
    ae.initCause(realCause);
    
    throw ae;
  }
  
  /**
   * Fails a test with the given message.
   * @param message the assertion error message 
   */
  static public void fail(String message) {
    throw new AssertionError(message);
  }
  
  /**
   * Fails a test with no message.
   */
  static public void fail() {
    fail(null);
  }
  
  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionError is thrown with the given message.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param message the assertion error message 
   */
  static public void assertEquals(Object actual, Object expected, String message) {
    if((expected == null) && (actual == null)) {
      return;
    }
    if((expected != null) && expected.equals(actual)) {
      return;
    }
    failNotEquals(actual, expected, message);
  }
  
  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionError is thrown.
   * @param actual the actual value 
   * @param expected the expected value 
   */
  static public void assertEquals(Object actual, Object expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   * Asserts that two Strings are equal.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param message the assertion error message 
   */
  static public void assertEquals(String actual, String expected, String message) {
    assertEquals((Object) actual, (Object) expected, message);
  }
  
  /**
   * Asserts that two Strings are equal.
   * @param actual the actual value 
   * @param expected the expected value 
   */
  static public void assertEquals(String actual, String expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   * Asserts that two doubles are equal concerning a delta.  If they are not
   * an AssertionError is thrown with the given message.  If the expected
   * value is infinity then the delta value is ignored.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param delta the absolute tolerate value value between the actual and expected value 
   * @param message the assertion error message 
   */
  static public void assertEquals(double actual, double expected, double delta, String message) {
    // handle infinity specially since subtracting to infinite values gives NaN and the
    // the following test fails
    if(Double.isInfinite(expected)) {
      if(!(expected == actual)) {
        failNotEquals(new Double(actual), new Double(expected), message);
      }
    }
    else if(!(Math.abs(expected - actual) <= delta)) { // Because comparison with NaN always returns false
      failNotEquals(new Double(actual), new Double(expected), message);
    }
  }
  
  /**
   * Asserts that two doubles are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param delta the absolute tolerate value value between the actual and expected value 
   */
  static public void assertEquals(double actual, double expected, double delta) {
    assertEquals(actual, expected, delta, null);
  }
  
  /**
   * Asserts that two floats are equal concerning a delta. If they are not
   * an AssertionError is thrown with the given message.  If the expected
   * value is infinity then the delta value is ignored.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param delta the absolute tolerate value value between the actual and expected value 
   * @param message the assertion error message 
   */
  static public void assertEquals(float actual, float expected, float delta, String message) {
    // handle infinity specially since subtracting to infinite values gives NaN and the
    // the following test fails
    if(Float.isInfinite(expected)) {
      if(!(expected == actual)) {
        failNotEquals(new Float(actual), new Float(expected), message);
      }
    }
    else if(!(Math.abs(expected - actual) <= delta)) {
      failNotEquals(new Float(actual), new Float(expected), message);
    }
  }
  
  /**
   * Asserts that two floats are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param delta the absolute tolerate value value between the actual and expected value 
   */
  static public void assertEquals(float actual, float expected, float delta) {
    assertEquals(actual, expected, delta, null);
  }
  
  /**
   * Asserts that two longs are equal. If they are not
   * an AssertionError is thrown with the given message.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param message the assertion error message 
   */
  static public void assertEquals(long actual, long expected, String message) {
    assertEquals(new Long(actual), new Long(expected), message);
  }
  
  /**
   * Asserts that two longs are equal.
   * @param actual the actual value 
   * @param expected the expected value 
   */
  static public void assertEquals(long actual, long expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   * Asserts that two booleans are equal. If they are not
   * an AssertionError is thrown with the given message.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param message the assertion error message 
   */
  static public void assertEquals(boolean actual, boolean expected, String message) {
    assertEquals(new Boolean(actual), new Boolean(expected), message);
  }
  
  /**
   * Asserts that two booleans are equal.
   * @param actual the actual value 
   * @param expected the expected value 
   */
  static public void assertEquals(boolean actual, boolean expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   * Asserts that two bytes are equal. If they are not
   * an AssertionError is thrown with the given message.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param message the assertion error message 
   */
  static public void assertEquals(byte actual, byte expected, String message) {
    assertEquals(new Byte(actual), new Byte(expected), message);
  }
  
  /**
   * Asserts that two bytes are equal.
   * @param actual the actual value 
   * @param expected the expected value 
   */
  static public void assertEquals(byte actual, byte expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   * Asserts that two chars are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param message the assertion error message 
   */
  static public void assertEquals(char actual, char expected, String message) {
    assertEquals(new Character(actual), new Character(expected), message);
  }
  
  /**
   * Asserts that two chars are equal.
   * @param actual the actual value 
   * @param expected the expected value 
   */
  static public void assertEquals(char actual, char expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   * Asserts that two shorts are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param message the assertion error message 
   */
  static public void assertEquals(short actual, short expected, String message) {
    assertEquals(new Short(actual), new Short(expected), message);
  }
  
  /**
   * Asserts that two shorts are equal.
   * @param actual the actual value 
   * @param expected the expected value 
   */
  static public void assertEquals(short actual, short expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   * Asserts that two ints are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param message the assertion error message 
   */
  static public void assertEquals(int actual,  int expected, String message) {
    assertEquals(new Integer(actual), new Integer(expected), message);
  }
  
  /**
   * Asserts that two ints are equal.
   * @param actual the actual value 
   * @param expected the expected value 
   */
  static public void assertEquals(int actual, int expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   * Asserts that an object isn't null.
   * @param object the assertion object
   */
  static public void assertNotNull(Object object) {
    assertNotNull(object, null);
  }
  
  /**
   * Asserts that an object isn't null. If it is
   * an AssertionFailedError is thrown with the given message.
   * @param object the assertion object
   * @param message the assertion error message 
   */
  static public void assertNotNull(Object object, String message) {
    assertTrue(object != null, message);
  }
  
  /**
   * Asserts that an object is null.
   * @param object the assertion object
   */
  static public void assertNull(Object object) {
    assertNull(object, null);
  }
  
  /**
   * Asserts that an object is null.  If it is not
   * an AssertionFailedError is thrown with the given message.
   * @param object the assertion object 
   * @param message the assertion error message 
   */
  static public void assertNull(Object object, String message) {
    assertTrue(object == null, message);
  }
  
  /**
   * Asserts that two objects refer to the same object. If they are not
   * an AssertionFailedError is thrown with the given message.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param message the assertion error message 
   */
  static public void assertSame(Object actual, Object expected, String message) {
    if(expected == actual) {
      return;
    }
    failNotSame(actual, expected, message);
  }
  
  /**
   * Asserts that two objects refer to the same object. If they are not
   * the same an AssertionError is thrown.
   * @param actual the actual value 
   * @param expected the expected value 
   */
  static public void assertSame(Object actual, Object expected) {
    assertSame(actual, expected, null);
  }
  
  /**
   * Asserts that two objects refer to the same object. If they are not
   * an AssertionError is thrown with the given message.
   * @param actual the actual value 
   * @param expected the expected value 
   * @param message the assertion error message 
   */
  static public void assertNotSame(Object actual, Object expected, String message) {
    if(expected == actual) {
      failSame(actual, expected, message);
    }
  }
  
  /**
   * Asserts that two objects refer to the same object. If they are not
   * the same an AssertionError is thrown.
   * @param actual the actual value 
   * @param expected the expected value 
   */
  static public void assertNotSame(Object actual, Object expected) {
    assertNotSame(actual, expected, null);
  }
  
  static private void failSame(Object actual, Object expected, String message) {
    String formatted = "";
    if(message != null) {
      formatted = message + " ";
    }
    fail(formatted + "expected not same with:<" + expected +"> but was same:<" + actual + ">");
  }
  
  static private void failNotSame(Object actual, Object expected, String message) {
    String formatted = "";
    if(message != null) {
      formatted = message + " ";
    }
    fail(formatted + "expected same with:<" + expected + "> but was:<" + actual + ">");
  }
  
  static private void failNotEquals(Object actual , Object expected, String message ) {
    fail(format(actual, expected, message));
  }
  
  static String format(Object actual, Object expected, String message) {
    String formatted = "";
    if (null != message) {
      formatted = message + " ";
    }
    
    return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
  }
  
  /**
   * Asserts that two collections contain the same elements. If they are not
   * the same an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  static public void assertEquals(Collection actual, Collection expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   * Asserts that two collections contain the same elements in the same order. If they are not
   * the same an AssertionError is thrown.
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  static public void assertEquals(Collection actual, Collection expected, String message) {
    if (null == message) message = "";
    assertEquals(actual.size(), expected.size(), message + ": lists don't have the same size");
    
    Iterator actIt = actual.iterator();
    Iterator expIt = expected.iterator();
    int i = -1;
    while(actIt.hasNext() && expIt.hasNext()) {
      i++;
      Object e = expIt.next();
      Object a = actIt.next();
      String errorMessage = message == null 
      ? "Lists differ at element [" + i + "]: " + e + " != " + a
          : message + ": Lists differ at element [" + i + "]: " + e + " != " + a;
      
      assertEquals(a, e, errorMessage);
    }
  }
  
  /**
   * Asserts that two arrays contain the same elements in the same order. If they are not
   * the same an AssertionError is thrown.
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  static public void assertEquals(Object[] actual, Object[] expected, String message) {
    if ((actual == null && expected != null) || (actual != null && expected == null)) {
      if (message != null) fail(message);
      else fail("Arrays not equal: " + expected + " and " + actual);
    }
    assertEquals(Arrays.asList(actual), Arrays.asList(expected), message);
  }
  
  /**
   * Asserts that two arrays contain the same elements in no particular order. If they are not
   * the same an AssertionError is thrown.
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  static public void assertEqualsNoOrder(Object[] actual, Object[] expected, String message) {
    if ((actual == null && expected != null) || (actual != null && expected == null)) {
      if (message != null) fail(message);
      else fail("Arrays not equal: " + expected + " and " + actual);
    }
    
    Collection actualCollection = new HashSet();
    for (Object a : actual) {
      actualCollection.add(a);
    }
    
    Collection expectedCollection = new HashSet();
    for (Object a : expected) {
      expectedCollection.add(a);
    }
    
    assertEquals(actualCollection, expectedCollection, message);
  }
  
  /**
   * Asserts that two arrays contain the same elements in the same order. If they are not
   * the same an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  static public void assertEquals(Object[] actual, Object[] expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   Asserts that two arrays contain the same elements in no particular order. If they are not
   * the same an AssertionError is thrown.
   * @param actual the actual value
   * @param expected the expected value
   */
  static public void assertEqualsNoOrder(Object[] actual, Object[] expected) {
    assertEqualsNoOrder(actual, expected, null);
  }
  
  /**
   * Asserts that two arrays contain the same elements in the same order. If they are not
   * the same an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  static public void assertEquals(final byte[] actual, final byte[] expected) {
    assertEquals(actual, expected, "");
  }
  
  /**
   * Asserts that two arrays contain the same elements in the same order. If they are not
   * the same an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  static public void assertEquals(final byte[] actual, final byte[] expected, final String message) {
    if(expected == actual) {
      return;
    }
    if(null == expected) {
      fail("expected a null array, but not null found. " + message);
    }
    if(null == actual) {
      fail("expected not null array, but null found. " + message);
    }
    
    assertEquals(actual.length, expected.length, "arrays don't have the same size. " + message);
    
    for(int i= 0; i < expected.length; i++) {
      if(expected[i] != actual[i]) {
        fail("arrays differ firstly at element [" + i +"]; "
            + "expected value is <" + expected[i] +"> but was <"
            + actual[i] + ">. "
            + message);
      }
    }
  }
}
