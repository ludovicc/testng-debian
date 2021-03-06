package org.testng.internal;

import bsh.EvalError;
import bsh.Interpreter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.xml.XmlClass;

/**
 * This class is the default method selector used by TestNG to determine
 * which methods need to be included and excluded based on the specification
 * given in testng.xml.
 * 
 * Created on Sep 30, 2005
 * @author cbeust
 */
public class XmlMethodSelector implements IMethodSelector {
  private static Interpreter s_interpreter;
  
  // Groups included and excluded for this run
  private Map<String, String> m_includedGroups= new HashMap<String, String>();
  private Map<String, String> m_excludedGroups= new HashMap<String, String>();
  private List<XmlClass> m_classes = null;
  // The BeanShell expression for this test, if any
  private String m_expression = null;
  // List of methods included implicitly
  private Set<String> m_includedMethods = new HashSet<String>();
  
  public boolean  includeMethod(IMethodSelectorContext context,
      ITestNGMethod tm, boolean isTestMethod) 
  {
//    ppp("XML METHOD SELECTOR " + tm + " " + m_isInitialized);
    
    if (! m_isInitialized) {
      m_isInitialized = true;
      init(context);
    }
    
    boolean result = false;
    if (null != m_expression) {
      result = includeMethodFromExpression(tm, isTestMethod);
    }
    else {
      result = includeMethodFromIncludeExclude(tm, isTestMethod);
    }
    
    return result;
  }
  
  private static Interpreter getInterpreter() {
    if(null == s_interpreter) {
      s_interpreter= new Interpreter();
    }
    
    return s_interpreter;
  }
  
  private boolean includeMethodFromExpression(ITestNGMethod tm,  boolean isTestMethod) {
    boolean result = false;

    Interpreter interpreter= getInterpreter();
    try {
      Map<String, String> groups = new HashMap<String, String>();
      for (String group : tm.getGroups()) {
        groups.put(group, group);
      }
      setContext(interpreter, tm.getMethod(), groups, tm);
      Object evalResult = interpreter.eval(m_expression);
      result = ((Boolean) evalResult).booleanValue();
    }
    catch (EvalError evalError) {
      Utils.log("bsh.Interpreter", 2, "Cannot evaluate expression:" + m_expression + ":" + evalError.getMessage());
    }
    finally {
      resetContext(interpreter);
    }

    return result;
    
  }
  
  private void resetContext(Interpreter interpreter) {
    try {
      interpreter.unset("method");
      interpreter.unset("groups");
      interpreter.unset("testngMethod");
    }
    catch(EvalError evalError) {
      Utils.log("bsh.Interpreter", 2, "Cannot reset interpreter:" + evalError.getMessage());
    }
  }
  
  private void setContext(Interpreter interpreter, Method method, Map<String, String> groups, ITestNGMethod tm) {
    try {
      interpreter.set("method", method);
      interpreter.set("groups", groups);
      interpreter.set("testngMethod", tm);
    }
    catch(EvalError evalError) {
      throw new TestNGException("Cannot set BSH interpreter", evalError);
    }
  }
  
  private boolean includeMethodFromIncludeExclude(ITestNGMethod tm, boolean isTestMethod) {
    boolean result = false;
    Method m = tm.getMethod();
    String[] groups = tm.getGroups();
    Map<String, String> includedGroups = m_includedGroups;
    Map<String, String> excludedGroups = m_excludedGroups;
    
    //
    // No groups were specified:
    //
    if (includedGroups.size() == 0 && excludedGroups.size() == 0 
        && ! hasIncludedMethods() && ! hasExcludedMethods())
    //
    // If we don't include or exclude any methods, method is in
    //
    {
      result = true;
    } 
    //
    // If it's a configuration method and no groups were requested, we want it in
    //
    else if (includedGroups.size() == 0 && excludedGroups.size() == 0 && ! isTestMethod) 
    {
      result = true;
    }
    
    //
    // Is this method included implicitly?
    //
    else if (m_includedMethods.contains(MethodHelper.calculateMethodCanonicalName(tm))) {
      result = true;
    }
    
    //
    // Include or Exclude groups were specified:
    //
    else {
      //
      // Only add this method if it belongs to an included group and not
      // to an excluded group
      //
      {
        boolean isIncludedInGroups = isIncluded(groups, m_includedGroups.values());
        boolean isExcludedInGroups = isExcluded(groups, m_excludedGroups.values());
  
        //
        // Calculate the run methods by groups first
        //
        if (isIncludedInGroups && !isExcludedInGroups) {
          result = true;
        }
        else if (isExcludedInGroups) {
          result = false;
        }
      }
      
      if(isTestMethod) {
        //
        // Now filter by method name
        //
        Method method = tm.getMethod();
        Class methodClass= method.getDeclaringClass();
        String fullMethodName =  methodClass.getName() 
                + "."
                + method.getName();

        String[] fullyQualifiedMethodName = new String[] { fullMethodName };
        
        //
        // Iterate through all the classes so we can gather all the included and
        // excluded methods
        //
        for (XmlClass xmlClass : m_classes) {
          // Only consider included/excluded methods that belong to the same class
          // we are looking at
          Class cls= ClassHelper.forName(xmlClass.getName());
          if(null == cls) {
            Utils.log("XmlMethodSelector", 1, "Cannot find class in classpath " + xmlClass.getName());
            continue;
          }
          if(!assignable(methodClass, cls)) {
            continue;
          }
          
          List<String> includedMethods = createQualifiedMethodNames(xmlClass, xmlClass.getIncludedMethods());
          boolean isIncludedInMethods = isIncluded(fullyQualifiedMethodName, includedMethods);
          List<String> excludedMethods = createQualifiedMethodNames(xmlClass, xmlClass.getExcludedMethods());
          boolean isExcludedInMethods = isExcluded(fullyQualifiedMethodName, excludedMethods);
          if (result) {
            // If we're about to include this method by group, make sure
            // it's included by method and not excluded by method
            result = isIncludedInMethods && ! isExcludedInMethods;
          }
          // otherwise it's already excluded and nothing will bring it back,
          // since exclusions preempt inclusions
        }
      }      
    }    
    
    Package pkg = m.getDeclaringClass().getPackage();
    String methodName = pkg != null ? pkg.getName() + "." + m.getName() : m.getName();
    
    logInclusion(result ? "Including" : "Excluding", "method", methodName + "()");

    return result;
  }
  
  @SuppressWarnings({"unchecked"})
  private boolean assignable(Class sourceClass, Class targetClass) {
    return sourceClass.isAssignableFrom(targetClass) || targetClass.isAssignableFrom(sourceClass);
  }
  
  private Map<String, String> m_logged = new HashMap<String, String>();
  private void logInclusion(String including, String type, String name) {
    if (! m_logged.containsKey(name)) {
      log(4, including + " " + type + " " + name);
      m_logged.put(name, name);
    }
  }
  
  private boolean hasIncludedMethods() {
    for (XmlClass xmlClass : m_classes) {
      if (xmlClass.getIncludedMethods().size() > 0) {
        return true;
      }
    }
    
    return false;
  }
  
  private boolean hasExcludedMethods() {
    for (XmlClass xmlClass : m_classes) {
      if (xmlClass.getExcludedMethods().size() > 0) {
        return true;
      }
    }
    
    return false;
  }

  private List<String> createQualifiedMethodNames(XmlClass xmlClass, List<String> methods) {
    List<String> vResult = new ArrayList<String>();
    Class cls = xmlClass.getSupportClass();

    while (null != cls) {
      for (String methodName : methods) {
        Method[] allMethods = cls.getDeclaredMethods();
        Pattern pattern = Pattern.compile(methodName);
        for (Method m : allMethods) {
          if (pattern.matcher(m.getName()).matches()) {
            vResult.add(makeMethodName(cls.getName(), m.getName()));
          }
        }
      }
      cls = cls.getSuperclass();
    }
    
    return vResult;
  }
  
  private String makeMethodName(String className, String methodName) {
    return className + "." + methodName;
  }
  
  public void setXmlClasses(List<XmlClass> classes) {
    m_classes = classes;
    for (XmlClass c : classes) {
      for (String m : c.getIncludedMethods()) {
        m_includedMethods.add(makeMethodName(c.getName(), m));
      }
    }
  }
  
  /**
   * @return Returns the excludedGroups.
   */
  public Map<String, String> getExcludedGroups() {
    return m_excludedGroups;
  }

  /**
   * @return Returns the includedGroups.
   */
  public Map<String, String> getIncludedGroups() {
    return m_includedGroups;
  }

  /**
   * @param excludedGroups The excludedGroups to set.
   */
  public void setExcludedGroups(Map<String, String> excludedGroups) {
    m_excludedGroups = excludedGroups;
  }
  
  /**
   * @param includedGroups The includedGroups to set.
   */
  public void setIncludedGroups(Map<String, String> includedGroups) {
    m_includedGroups = includedGroups;
  }
  
  private static boolean isIncluded(String[] groups, Collection<String> includedGroups) {
    if (includedGroups.size() == 0) {
      return true;
    }
    else {
      return isMemberOf(groups, includedGroups);
    }
  }
  
  private static boolean isExcluded(String[] groups, Collection<String> excludedGroups) {
    return isMemberOf(groups, excludedGroups);
  }
  
  /**
   * 
   * @param groups Array of groups on the method
   * @param list Map of regexps of groups to be run
   */
  private static boolean isMemberOf(String[] groups, Collection<String> list) {
    for (String group : groups) {
      for (Object o : list) {
        String regexpStr = o.toString();
        boolean match = Pattern.matches(regexpStr, group);
        if (match) {
          return true;
        }
      }
    }
    
    return false;
  }
    
  private static void log(int level, String s) {
    Utils.log("XmlMethodSelector", level, s);
  }
  
  private static void ppp(String s) {
    System.out.println("[XmlMethodSelector] " + s);
  }

  public void setExpression(String expression) {
    m_expression = expression;
  }
  
  private boolean m_isInitialized = false;
  private List<ITestNGMethod> m_testMethods = null;

  public void setTestMethods(List<ITestNGMethod> testMethods) {
    m_testMethods = testMethods;
  }
  
  private void init(IMethodSelectorContext context) {
    String[] groups = m_includedGroups.keySet().toArray(new String[m_includedGroups.size()]);
    Set<String> groupClosure = new HashSet<String>();
    Set<ITestNGMethod> methodClosure = new HashSet<ITestNGMethod>();
    
    List<ITestNGMethod> includedMethods = new ArrayList<ITestNGMethod>();
    for (ITestNGMethod m : m_testMethods) {
      if (includeMethod(context, m, true)) includedMethods.add(m);
    }
    MethodHelper.findGroupTransitiveClosure(this, includedMethods, m_testMethods, 
        groups, groupClosure, methodClosure);
    
    // If we are asked to include or exclude specific groups, calculate
    // the transitive closure of all the included groups.  If no include groups
    // were specified, don't do anything.
    // Any group that is part of the transitive closure but not part of
    // m_includedGroups is being added implicitly by TestNG so that if someone
    // includes a group z that depends on a, b and c, they don't need to 
    // include a, b and c explicitly.
    if (m_includedGroups.size() > 0) {
      // Make the transitive closure our new included groups
      for (String g : groupClosure) {
        log(4, "Including group "
            + (m_includedGroups.containsKey(g) ?
                ": " : "(implicitly): ") + g);
        m_includedGroups.put(g, g);
      }
    
      // Make the transitive closure our new included methods
      for (ITestNGMethod m : methodClosure) {
        String methodName = 
         m.getMethod().getDeclaringClass().getName() + "." + m.getMethodName();
        m_includedMethods.add(methodName);
        logInclusion("Including", "method ", methodName);
      }
    }
  }

  private boolean m_verbose = true;
  public void setVerbose(boolean b) {
    m_verbose = b;
  }
  
}
