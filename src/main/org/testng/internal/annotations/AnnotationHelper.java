package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.testng.ITestNGMethod;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IExpectedExceptionsAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IParametersAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.TestNGMethod;
import org.testng.internal.Utils;

/**
 * Helper methods to find @Test and @Configuration tags.  They minimize
 * the amount of casting we need to do.
 * 
 * Created on Dec 20, 2005
 * @author cbeust
 */
public class AnnotationHelper {

  public static ITestAnnotation findTest(IAnnotationFinder finder, Class cls) {
    return (ITestAnnotation) finder.findAnnotation(cls, ITestAnnotation.class);
  }
  
  public static ITestAnnotation findTest(IAnnotationFinder finder, Method m) {
    return (ITestAnnotation) finder.findAnnotation(m, ITestAnnotation.class);
  }
  
  public static IFactoryAnnotation findFactory(IAnnotationFinder finder, Method m) {
    return (IFactoryAnnotation) finder.findAnnotation(m, IFactoryAnnotation.class);
  }

  public static ITestAnnotation findTest(IAnnotationFinder finder, Constructor ctor) {
    return (ITestAnnotation) finder.findAnnotation(ctor, ITestAnnotation.class);
  }

  public static IConfigurationAnnotation findConfiguration(IAnnotationFinder finder, Constructor ctor) {
    IConfigurationAnnotation result = (IConfigurationAnnotation) finder.findAnnotation(ctor, IConfigurationAnnotation.class);
    if (result == null) {
      IConfigurationAnnotation bs = (IConfigurationAnnotation) finder.findAnnotation(ctor, IBeforeSuite.class);
      IConfigurationAnnotation as = (IConfigurationAnnotation) finder.findAnnotation(ctor, IAfterSuite.class);
      IConfigurationAnnotation bt = (IConfigurationAnnotation) finder.findAnnotation(ctor, IBeforeTest.class);
      IConfigurationAnnotation at = (IConfigurationAnnotation) finder.findAnnotation(ctor, IAfterTest.class);
      IConfigurationAnnotation bg = (IConfigurationAnnotation) finder.findAnnotation(ctor, IBeforeGroups.class);
      IConfigurationAnnotation ag = (IConfigurationAnnotation) finder.findAnnotation(ctor, IAfterGroups.class);
      IConfigurationAnnotation bc = (IConfigurationAnnotation) finder.findAnnotation(ctor, IBeforeClass.class);
      IConfigurationAnnotation ac = (IConfigurationAnnotation) finder.findAnnotation(ctor, IAfterClass.class);
      IConfigurationAnnotation bm = (IConfigurationAnnotation) finder.findAnnotation(ctor, IBeforeMethod.class);
      IConfigurationAnnotation am = (IConfigurationAnnotation) finder.findAnnotation(ctor, IAfterMethod.class);
      
      if (bs != null || as != null || bt != null || at != null || bg != null || ag != null
          || bc != null || ac != null || bm != null || am != null) 
      {
        result = createConfiguration(bs, as, bt, at, bg, ag, bc, ac, bm, am);
      }    
    }
    
    return result;
  }

  public static IConfigurationAnnotation findConfiguration(IAnnotationFinder finder, Method m) {
    IConfigurationAnnotation result = (IConfigurationAnnotation) finder.findAnnotation(m, IConfigurationAnnotation.class);
    if (result == null) {
      IConfigurationAnnotation bs = (IConfigurationAnnotation) finder.findAnnotation(m, IBeforeSuite.class);
      IConfigurationAnnotation as = (IConfigurationAnnotation) finder.findAnnotation(m, IAfterSuite.class);
      IConfigurationAnnotation bt = (IConfigurationAnnotation) finder.findAnnotation(m, IBeforeTest.class);
      IConfigurationAnnotation at = (IConfigurationAnnotation) finder.findAnnotation(m, IAfterTest.class);
      IConfigurationAnnotation bg = (IConfigurationAnnotation) finder.findAnnotation(m, IBeforeGroups.class);
      IConfigurationAnnotation ag = (IConfigurationAnnotation) finder.findAnnotation(m, IAfterGroups.class);
      IConfigurationAnnotation bc = (IConfigurationAnnotation) finder.findAnnotation(m, IBeforeClass.class);
      IConfigurationAnnotation ac = (IConfigurationAnnotation) finder.findAnnotation(m, IAfterClass.class);
      IConfigurationAnnotation bm = (IConfigurationAnnotation) finder.findAnnotation(m, IBeforeMethod.class);
      IConfigurationAnnotation am = (IConfigurationAnnotation) finder.findAnnotation(m, IAfterMethod.class);
      
      if (bs != null || as != null || bt != null || at != null || bg != null || ag != null
          || bc != null || ac != null || bm != null || am != null) 
      {
        result = createConfiguration(bs, as, bt, at, bg, ag, bc, ac, bm, am);
      }    
    }
    
    return result;
  }
  
  private static IConfigurationAnnotation createConfiguration(IConfigurationAnnotation bs, IConfigurationAnnotation as, 
      IConfigurationAnnotation bt, IConfigurationAnnotation at, IConfigurationAnnotation bg, IConfigurationAnnotation ag, 
      IConfigurationAnnotation bc, IConfigurationAnnotation ac, IConfigurationAnnotation bm, IConfigurationAnnotation am) 
  {
    ConfigurationAnnotation result = new ConfigurationAnnotation();
    
    if (bs != null) {
      result.setBeforeSuite(true);
      finishInitialize(result, bs);
    }
    if (as != null) {
      result.setAfterSuite(true);
      finishInitialize(result, as);
    }
    if (bt != null) {
      result.setBeforeTest(true);
      finishInitialize(result, bt);
    }
    if (at != null) {
      result.setAfterTest(true);
      finishInitialize(result, at);
    }
    if (bg != null) {
      result.setBeforeGroups(bg.getBeforeGroups());
      finishInitialize(result, bg);
    }
    if (ag != null) {
      result.setAfterGroups(ag.getAfterGroups());
      finishInitialize(result, ag);
    }
    if (bc != null) {
      result.setBeforeTestClass(true);
      finishInitialize(result, bc);
    }
    if (ac != null) {
      result.setAfterTestClass(true);
      finishInitialize(result, ac);
    }
    if (bm != null) {
      result.setBeforeTestMethod(true);
      finishInitialize(result, bm);
    }
    if (am != null) {
      result.setAfterTestMethod(true);
      finishInitialize(result, am);
    }

    return result;
  }
  
  @SuppressWarnings({"deprecation"})
  private static void finishInitialize(ConfigurationAnnotation result, IConfigurationAnnotation bs) {
    result.setFakeConfiguration(true);
    result.setAlwaysRun(bs.getAlwaysRun());
    result.setDependsOnGroups(bs.getDependsOnGroups());
    result.setDependsOnMethods(bs.getDependsOnGroups());
    result.setDescription(bs.getDescription());
    result.setEnabled(bs.getEnabled());
    result.setGroups(bs.getGroups());
    result.setInheritGroups(bs.getInheritGroups());
    result.setParameters(bs.getParameters());
  }

  private static Class[] ALL_ANNOTATIONS = new Class[] { 
    ITestAnnotation.class, IConfigurationAnnotation.class, 
    IBeforeClass.class, IAfterClass.class,
    IBeforeMethod.class, IAfterMethod.class,
    IDataProviderAnnotation.class, IExpectedExceptionsAnnotation.class, 
    IFactoryAnnotation.class, IParametersAnnotation.class, 
    IBeforeSuite.class, IAfterSuite.class,
    IBeforeTest.class, IAfterTest.class,
    IBeforeGroups.class, IAfterGroups.class
  };
  
  public static Class[] CONFIGURATION_CLASSES = new Class[] {
    IConfigurationAnnotation.class,
    IBeforeSuite.class, IAfterSuite.class,   
    IBeforeTest.class, IAfterTest.class,   
    IBeforeGroups.class, IAfterGroups.class,   
    IBeforeClass.class, IAfterClass.class,  
    IBeforeMethod.class, IAfterMethod.class
  };

  public static Class[] getAllAnnotations() {
    return ALL_ANNOTATIONS;
  }

  /**
   * Delegation method for creating the list of <CODE>ITestMethod</CODE>s to be
   * analysed.
   */
  public static ITestNGMethod[] findMethodsWithAnnotation(Class rootClass, Class annotationClass,
        IAnnotationFinder annotationFinder)
  {
    // Keep a map of the methods we saw so that we ignore a method in a superclass if it's
    // already been seen in a child class
    Map<String, ITestNGMethod> vResult = new HashMap<String, ITestNGMethod>();
    
    try {
      vResult = new HashMap<String, ITestNGMethod>();
//    Class[] classes = rootClass.getTestClasses();
      Class cls = rootClass;
      
      //
      // If the annotation is on the class or superclass, it applies to all public methods
      // except methods marked with @Configuration
      //

      //
      // Otherwise walk through all the methods and keep those
      // that have the annotation
      //
//    for (Class cls : classes) {
        while (null != cls) {
          boolean hasClassAnnotation = isAnnotationPresent(annotationFinder, cls, annotationClass);
          Method[] methods = cls.getDeclaredMethods();
          for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            boolean hasMethodAnnotation = isAnnotationPresent(annotationFinder, m, annotationClass);
            boolean hasTestNGAnnotation =
              isAnnotationPresent(annotationFinder, m, IFactoryAnnotation.class) ||
              isAnnotationPresent(annotationFinder, m, ITestAnnotation.class) ||
              isAnnotationPresent(annotationFinder, m, CONFIGURATION_CLASSES);
            boolean isPublic = Modifier.isPublic(m.getModifiers());
            if ((isPublic && hasClassAnnotation && (! hasTestNGAnnotation)) || hasMethodAnnotation) {     
              
              // Small hack to allow users to specify @Configuration classes even though
              // a class-level @Test annotation is present.  In this case, don't count
              // that method as a @Test
              if (isAnnotationPresent(annotationFinder, m, IConfigurationAnnotation.class) &&
                  isAnnotationPresent(annotationFinder, cls, ITestAnnotation.class)) 
              {
                Utils.log("", 3, "Method " + m + " has a local @Configuration and a class-level @Test." +
                    "This method will only be kept as a @Configuration.");
                    
                continue;
              }
              
              // Skip the method if it has a return type
              if (m.getReturnType() != void.class) {
                Utils.log("", 3, "Method " + m + " has a @Test annotation"
                    + " but also a return value:  ignoring it.");
                continue;
              }
              
              String key = createMethodKey(m);
              if (null == vResult.get(key)) {
                ITestNGMethod tm = new TestNGMethod(/* m.getDeclaringClass(), */ m, annotationFinder);
                vResult.put(key,tm);
              }
            }
          } // for
          // Now explore the superclass
          cls = cls.getSuperclass();
        } // while

    }
    catch (SecurityException e) {
      e.printStackTrace();
    }
    ITestNGMethod[] result = vResult.values().toArray(new ITestNGMethod[vResult.size()]);
      
  //    for (Method m : result) {
  //      ppp("   METHOD FOUND: " + m);
  //    }
      
      return result;
    }

  private static boolean isAnnotationPresent(IAnnotationFinder annotationFinder, 
      Method m, Class[] annotationClasses) 
  {
    for (Class a : annotationClasses) {
      if (annotationFinder.findAnnotation(m, a) != null) {
        return true;
      }
    }
    
    return false;
  }

  private static boolean isAnnotationPresent(IAnnotationFinder annotationFinder, Method m, Class annotationClass) {
    return annotationFinder.findAnnotation(m, annotationClass) != null;
  }

  private static boolean isAnnotationPresent(IAnnotationFinder annotationFinder, Class cls, Class annotationClass) {
    return annotationFinder.findAnnotation(cls, annotationClass) != null;
  }

  /**
   * @return A hashcode representing the name of this method and its parameters,
   * but without its class
   */
  private static String createMethodKey(Method m) {
    StringBuffer result = new StringBuffer(m.getName());
    for (Class paramClass : m.getParameterTypes()) {
      result.append(' ').append(paramClass.toString());
    }
    
    return result.toString();
  }
  
}
