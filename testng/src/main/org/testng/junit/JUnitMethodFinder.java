package org.testng.junit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.ITestMethodFinder;
import org.testng.ITestNGMethod;
import org.testng.internal.TestNGMethod;
import org.testng.internal.annotations.IAnnotationFinder;


/**
 * This class locates all test and configuration methods according to JUnit.  
 * It is used to change the strategy used by TestRunner to locate its test
 * methods.
 *
 * @author Cedric Beust, May 3, 2004
 * 
 */
public class JUnitMethodFinder implements ITestMethodFinder {
  private String m_testName = null;
  private IAnnotationFinder m_annotationFinder = null;
  
  public JUnitMethodFinder(String testName, IAnnotationFinder finder) {
    m_testName = testName;
    m_annotationFinder = finder;
  }
  
  private Constructor findConstructor(Class cls, Class[] parameters) {
    Constructor result = null;
    
    try {
      result = cls.getConstructor(parameters);
    } 
    catch (SecurityException ex) {
      // ignore
    } 
    catch (NoSuchMethodException ex) {
      // ignore
    }
    
    return result;
  }

  public ITestNGMethod[] getTestMethods(Class cls) {
    ITestNGMethod[] result = 
      privateFindTestMethods(new INameFilter() {
        public boolean accept(Method method) {
          return method.getName().startsWith("test") &&
            method.getParameterTypes().length == 0;
        }
    }, cls);
    
//    ppp("=====");
//    ppp("FIND TEST METHOD RETURNING ");
//    for (ITestMethod m : result) {
//      ppp("  " + m);
//    }
//    ppp("=====");
    return result;
  }  
  
  private ITestNGMethod[] privateFindTestMethods(INameFilter filter, Class cls) {
    List<ITestNGMethod> vResult = new ArrayList<ITestNGMethod>();
    
    // We do not want to walk up the class hierarchy and accept the
    // same method twice (e.g. setUp) which would lead to double-invocation.
    // All relevant JUnit methods are parameter-less so we store accepted
    // method names in a Set to filter out duplicates.
    Set<String> acceptedMethodNames = new HashSet<String>();
    
    //
    // Collect all methods that start with test
    //
    Class current = cls;
    while(!(current == Object.class)) {
      Method[] allMethods = current.getDeclaredMethods();
      for(Method allMethod : allMethods) {
        ITestNGMethod m = new TestNGMethod(/* allMethods[i].getDeclaringClass(), */ allMethod, m_annotationFinder);
        Method method = m.getMethod();
        String methodName = method.getName();
        if(filter.accept(method) && !acceptedMethodNames.contains(methodName)) {
          //          if (m.getName().startsWith("test")) {
          //            ppp("Found JUnit test method: " + tm);
          vResult.add(m);
          acceptedMethodNames.add(methodName);
        }
      }
      current = current.getSuperclass();
    }

    return vResult.toArray(new ITestNGMethod[vResult.size()]);
  }
  
  private static void ppp(String s) {
    System.out.println("[JUnitMethodFinder] " + s);
  }
  
  private Object instantiate(Class cls) {
    Object result = null;
    
    Constructor ctor = findConstructor(cls, new Class[] { String.class });
    try {
      if (null != ctor) {
        result = ctor.newInstance(new Object[] { m_testName });
      }
      else {
        ctor = cls.getConstructor(new Class[0]);
        result = ctor.newInstance(new Object[0]);
      }
    } 
    catch (IllegalArgumentException ex) {
      ex.printStackTrace();
    }
    catch (SecurityException ex) {
      ex.printStackTrace();
    } 
    catch (InstantiationException ex) {
      System.err.println("Couldn't find a constructor with a String parameter on your JUnit test class.");
      ex.printStackTrace();
    } 
    catch (IllegalAccessException ex) {
      ex.printStackTrace();
    } 
    catch (InvocationTargetException ex) {
      ex.printStackTrace();
    } 
    catch (NoSuchMethodException ex) {
      ex.printStackTrace();
    }
    
    return result;
  }


  public ITestNGMethod[] getBeforeTestMethods(Class cls) {
    ITestNGMethod[] result = privateFindTestMethods(new INameFilter() {
        public boolean accept(Method method) {
          return "setUp".equals(method.getName());
        }
      }, cls);
    
    return result;
  }

  public ITestNGMethod[] getAfterTestMethods(Class cls) {
    ITestNGMethod[] result =  privateFindTestMethods(new INameFilter() {
        public boolean accept(Method method) {
          return "tearDown".equals(method.getName());
        }
      }, cls);
    
    return result;
  }
  
  public ITestNGMethod[] getAfterClassMethods(Class cls) {
    return new ITestNGMethod[0];
  }
  
  public ITestNGMethod[] getBeforeClassMethods(Class cls) {
    return new ITestNGMethod[0];
  }
  
  public ITestNGMethod[] getBeforeSuiteMethods(Class cls) {
    return new ITestNGMethod[0];
  }
  
  public ITestNGMethod[] getAfterSuiteMethods(Class cls) {
    return new ITestNGMethod[0];
  }
  
  public ITestNGMethod[] getBeforeTestConfigurationMethods(Class testClass) {
    return new ITestNGMethod[0];
  }

  public ITestNGMethod[] getAfterTestConfigurationMethods(Class testClass) {
    return new ITestNGMethod[0];
  }

  public ITestNGMethod[] getBeforeGroupsConfigurationMethods(Class testClass) {
    return new ITestNGMethod[0];
  }

  public ITestNGMethod[] getAfterGroupsConfigurationMethods(Class testClass) {
    return new ITestNGMethod[0];
  }
}

/////////////

interface INameFilter {
  public boolean accept(Method method);
}
