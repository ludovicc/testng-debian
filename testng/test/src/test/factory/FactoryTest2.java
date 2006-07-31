package test.factory;

import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This class
 * 
 * @author cbeust
 */
public class FactoryTest2 {
  private static Map<Integer, Integer> m_numbers = new HashMap<Integer, Integer>();
  private int m_number;
  
  public FactoryTest2() {
    throw new RuntimeException("Shouldn't be invoked");    
  }
  
  public static Map<Integer, Integer> getNumbers() {
    return m_numbers;
  }
  
  public FactoryTest2(int n) {
    m_number = n;
  }
  
  @Test(groups = { "first" })
  public void testInt() {
    Integer n = new Integer(m_number);
    m_numbers.put(n, n);
  }
  
  private static void ppp(String s) {
    System.out.println("[FactoryTest2] " + s);
  }
  
}
