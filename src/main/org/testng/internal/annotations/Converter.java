package org.testng.internal.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.testng.internal.Utils;

/**
 * Convert a string values into primitive types.
 * 
 * Created on Dec 20, 2005
 * @author cbeust
 */
public class Converter {

  public static boolean  getBoolean(String tagValue, boolean def) {
    boolean result = def;
    if (tagValue != null) result = new Boolean(tagValue).booleanValue();
    return result;
  }

  public static int getInt(String tagValue, int def) {
    int result = def;
    if (tagValue != null) result = new Integer(tagValue).intValue();
    return result;
  }

  public static String getString(String tagValue, String def) {
    String result = def;
    if (tagValue != null) result = tagValue;
    return result;
  }

  public static long getLong(String tagValue, long def) {
    long result = def;
    if (tagValue != null) result = new Long(tagValue).longValue();
    return result;
  }

  public static String[] getStringArray(String tagValue, String[] def) {
    String[] result = def;
    if (tagValue != null) {
      result = Utils.stringToArray(tagValue);
    }

    return result;
  }

  public static Class[] getClassArray(String tagValue, Class[] def) {
    Class[] result = def;
    List vResult = new ArrayList();
    if (tagValue != null) {
      StringTokenizer st = new StringTokenizer(tagValue, " ,");
      while (st.hasMoreElements()) {
        String className = (String) st.nextElement();
        try {
          Class cls = Class.forName(className);
          vResult.add(cls);
        }
        catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }
      result = (Class[]) vResult.toArray(new Class[vResult.size()]);
    }

    return result;
  }

}
