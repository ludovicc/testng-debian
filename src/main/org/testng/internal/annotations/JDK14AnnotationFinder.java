package org.testng.internal.annotations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.testng.TestRunner;
import org.testng.internal.Utils;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.directorywalker.DirectoryScanner;
import com.thoughtworks.qdox.directorywalker.FileVisitor;
import com.thoughtworks.qdox.directorywalker.SuffixFilter;
import com.thoughtworks.qdox.model.AbstractInheritableJavaEntity;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

/**
 * This class implements IAnnotationFinder with QDox for JDK 1.4
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class JDK14AnnotationFinder implements IAnnotationFinder {
  private JDK14TagFactory m_tagFactory = new JDK14TagFactory();
  private JavaDocBuilder m_docBuilder;
  private String[] m_dirPaths;
  private IAnnotationTransformer m_annotationTransformer;

  public JDK14AnnotationFinder(IAnnotationTransformer transformer) {
    m_docBuilder = new JavaDocBuilder();
    m_annotationTransformer = transformer;
  }
  
  void addSources(String[] filePaths) {
    if(filePaths == null) {
      if (TestRunner.getVerbose() > 1) {
        ppp("Array of source paths is null");
      }

      return;
    }
    for(int i = 0; i < filePaths.length; i++) {
      try {
        m_docBuilder.addSource(new FileReader(filePaths[i]));
      }
      catch(FileNotFoundException fnfe) {
        ppp("File does not exist [" + filePaths[i] + "]");
      }
      catch(Throwable t) {
        Utils.log(getClass().getName(), 1, "[WARNING] cannot parse source: " + filePaths[i] + "\n    " + t.getMessage());
      }
    }
  }

  public void addSourceDirs(String[] dirPaths) {
    if(dirPaths == null) {
      if (TestRunner.getVerbose() > 1) {
        ppp("Array of source directory paths is null");
      }
      return;
    }

    m_dirPaths = dirPaths;
    
    for (int i = 0; i < m_dirPaths.length; i++) {
      File dir = new File(m_dirPaths[i]);
      DirectoryScanner scanner = new DirectoryScanner(dir);
      scanner.addFilter(new SuffixFilter(".java"));
      scanner.scan(new FileVisitor() {
        public void visitFile(File currentFile) {
          addSources(new String[] { currentFile.getAbsolutePath() });
        }
      });
    }
  }

  public IAnnotation findAnnotation(Class cls, Class annotationClass) {
    return m_tagFactory.createTag(annotationClass, 
        m_docBuilder.getClassByName(cls.getName()));
  }

  public IAnnotation findAnnotation(Method m, Class annotationClass) {
    return findMethodAnnotation(m.getName(), m.getParameterTypes(), 
        m.getDeclaringClass(), annotationClass);
  }
  
  public IAnnotation findAnnotation(Constructor m, Class annotationClass) {
    String name = stripPackage(m.getName());
    return findMethodAnnotation(name, m.getParameterTypes(), m.getDeclaringClass(), 
        annotationClass);
  }
  
  private String stripPackage(String name) {
    String result = name;
    int index = result.lastIndexOf(".");
    if (index > 0) {
      result = result.substring(index + 1);
    }
    
    return result;
  }
  
  private IAnnotation findMethodAnnotation(String methodName, Class[] parameterTypes, 
      Class methodClass, Class annotationClass) 
  {
    IAnnotation result = null;
    JavaClass jc = m_docBuilder.getClassByName(methodClass.getName());
    if (jc != null) {
      List methods = new ArrayList();
      JavaMethod[] allMethods = jc.getMethods();
      for (int i = 0; i < allMethods.length; i++) {
        JavaMethod jm = allMethods[i];
        if (methodsAreEqual(jm, methodName, parameterTypes)) {
          methods.add(jm);
        }
      }
      
      JavaMethod method =null;
//      if (methods.size() > 1) {
//        ppp("WARNING:  method " + methodName + " is overloaded, only considering the first one");
//      }
      
      if (methods.size() > 0) {
        method = (JavaMethod) methods.get(0);
        result = findTag(annotationClass, result, method);
      }
      
    }
    else {
      ppp("COULDN'T RESOLVE CLASS " + methodClass.getName());
    }
    
    return result;
  }

  private boolean methodsAreEqual(JavaMethod jm, String methodName, Class[] parameterTypes) {
    boolean result = jm.getName().equals(methodName) && 
      jm.getParameters().length == parameterTypes.length;
    
    return result;
  }

  private IAnnotation findTag(Class annotationClass, IAnnotation result, 
      AbstractInheritableJavaEntity entity) 
  {
    return m_tagFactory.createTag(annotationClass, entity);
  }
  
  private static void ppp(String s) {
    System.out.println("[JDK14AnnotationFinder] " + s);
  }
}
