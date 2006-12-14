package org.testng.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.testng.TestNGException;
import org.testng.TestRunner;
import org.testng.internal.ClassHelper;
import org.testng.internal.Utils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * <code>Parser</code> is a parser for a TestNG XML test suite file.   
 */
public class Parser {
  
  /** The name of the TestNG DTD. */
  public static final String TESTNG_DTD = "testng-1.0.dtd";
  
  /** The URL to the deprecated TestNG DTD. */
  public static final String DEPRECATED_TESTNG_DTD_URL = "http://beust.com/testng/" + TESTNG_DTD;
  
  /** The URL to the TestNG DTD. */
  public static final String TESTNG_DTD_URL = "http://testng.org/" + TESTNG_DTD;
  
  /** The default file name for the TestNG test suite if none is specified (testng.xml). */
  public static final String DEFAULT_FILENAME = "testng.xml";
  
  /** The file name of the xml suite being parsed. This may be null if the Parser
   * has not been initialized with a file name. TODO CQ This member is never used. */
  private final String m_fileName;
  
  /** */
  private final InputStream m_inputStream;

  /** Look in Jar for the file instead? TODO CQ this member is never used. */
  private boolean m_lookInJar = false;

  /**
   * Constructs a <code>Parser</code> to use the inputStream as the source of
   * the xml test suite to parse. 
   * @param filename the filename corresponding to the inputStream or null if
   * unknown.
   * @param inputStream the xml test suite input stream. 
   */
  private Parser(String filename, InputStream inputStream) {
    m_fileName = filename;
    m_inputStream = inputStream;
  }
  
  /**
   * create a parser that works on a given file.
   * @param fileName the filename of the xml suite to parse.
   * @throws FileNotFoundException if the fileName is not found.
   */
  public Parser(String fileName) throws FileNotFoundException {
    this(fileName, new FileInputStream(new File(fileName)));
  }
  
  /**
   * Constructs a <code>Parser</code> to use the inputStream as the source of
   * the xml test suite to parse. 
   *
   * @param inputStream the xml test suite input stream. 
   */
  public Parser(InputStream inputStream) {
    this(null, inputStream);
  }
  
  /**
   * Creates a parser that will try to find the DEFAULT_FILENAME from the jar.
   * @throws FileNotFoundException if the DEFAULT_FILENAME resource is not
   * found in the classpath.
   */
  public Parser() throws FileNotFoundException {
    this(DEFAULT_FILENAME, getDefault());
  }

  /**
   * Returns an input stream on the resource named DEFAULT_FILENAME.
   *
   * @return an input stream on the resource named DEFAULT_FILENAME.
   * @throws FileNotFoundException if the DEFAULT_FILENAME resource is not
   * found in the classpath.
   */
  private static InputStream getDefault() throws FileNotFoundException {
    // Try to look for the DEFAULT_FILENAME from the jar
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream in;
    // TODO CQ is this OK? should we fall back to the default classloader if the
    // context classloader fails.
    if (classLoader != null) {
      in = classLoader.getResourceAsStream(DEFAULT_FILENAME);
    }
    else {
      in = Parser.class.getResourceAsStream(DEFAULT_FILENAME);
    }
    if (in == null) {
      throw new FileNotFoundException("Default property file of " + DEFAULT_FILENAME
          + " was not found");
    }
    return in;
  }
  
  /**
   * Parses the TestNG test suite and returns the corresponding XmlSuite,
   * and possibly, other XmlSuite that are pointed to by <suite-files>
   * tags.
   *
   * @return the parsed TestNG test suite.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException if an I/O error occurs while parsing the test suite file or
   * if the default testng.xml file is not found.
   */
  public Collection<XmlSuite> parse() throws ParserConfigurationException, SAXException, IOException 
  {
    // Each suite found is put in this map, keyed by their canonical
    // path to make sure we don't add a same file twice
    // (e.g. "testng.xml" and "./testng.xml")
    Map<String, XmlSuite> mapResult = new HashMap<String, XmlSuite>();
    
    SAXParserFactory spf= loadSAXParserFactory();
        
    if(supportsValidation(spf)) {
      spf.setValidating(true);
    }
    SAXParser saxParser = spf.newSAXParser();
    
    String mainFilePath = new File(m_fileName).getCanonicalPath();
    
    List<String> toBeParsed = new ArrayList<String>();
    List<String> toBeAdded = new ArrayList<String>();
    List<String> toBeRemoved = new ArrayList<String>();
    toBeParsed.add(mainFilePath);
    
    while (toBeParsed.size() > 0) {
      
      for (String currentFile : toBeParsed) {
        String canonicalPath = new File(currentFile).getCanonicalPath();

        XmlSuite currentXmlSuite = parseOneFile(saxParser, canonicalPath);
        mapResult.put(canonicalPath, currentXmlSuite);
        toBeRemoved.add(canonicalPath);
        
        List<String> suiteFiles = currentXmlSuite.getSuiteFiles();
        if (suiteFiles.size() > 0) {
          for (String path : suiteFiles) {
            canonicalPath = new File(path).getCanonicalPath();
            if (! mapResult.containsKey(canonicalPath)) {
              toBeAdded.add(canonicalPath);
            }
          }
        }
      }
      
      //
      // Add and remove files from toBeParsed before we loop
      //
      for (String s : toBeRemoved) {
        toBeParsed.remove(s);
      }
      toBeRemoved = new ArrayList<String>();
      
      for (String s : toBeAdded) {
        toBeParsed.add(s);
      }
      toBeAdded = new ArrayList<String>();
      
    }
    
    return mapResult.values();

  }
  
  /**
   * Tries to load a <code>SAXParserFactory</code> by trying in order the following:
   * <tt>com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl</tt> (SUN JDK5)
   * <tt>org.apache.crimson.jaxp.SAXParserFactoryImpl</tt> (SUN JDK1.4) and 
   * last <code>SAXParserFactory.newInstance()</code>.
   * 
   * @return a <code>SAXParserFactory</code> implementation
   * @throws TestNGException thrown if no <code>SAXParserFactory</code> can be loaded
   */
  private SAXParserFactory loadSAXParserFactory() {
    SAXParserFactory spf = null;
    
    StringBuffer errorLog= new StringBuffer();
    try {
      Class factoryClass= ClassHelper.forName("com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
      spf = (SAXParserFactory) factoryClass.newInstance();
    }
    catch(Exception ex) {
      errorLog.append("JDK5 SAXParserFactory cannot be loaded: " + ex.getMessage());
    }

    if(null == spf) {
      // If running with JDK 1.4
      try {
        Class factoryClass = ClassHelper.forName("org.apache.crimson.jaxp.SAXParserFactoryImpl");
        spf = (SAXParserFactory) factoryClass.newInstance();
      }
      catch(Exception ex) {
        errorLog.append("\n").append("JDK1.4 SAXParserFactory cannot be loaded: " + ex.getMessage());
      }
    }
    
    Throwable cause= null;
    if(null == spf) {
      try {
        spf= SAXParserFactory.newInstance();
      }
      catch(FactoryConfigurationError fcerr) {
        cause= fcerr;
      }
    }
    
    if(null == spf) {      
      throw new TestNGException("Cannot initialize a SAXParserFactory\n" + errorLog.toString(), cause);
    }
    
    return spf;
  }
  

  /**
   * Tests if the current <code>SAXParserFactory</code> supports DTD validation.
   * @param spf
   * @return
   */
  private boolean supportsValidation(SAXParserFactory spf) {
    try {
      return spf.getFeature("http://xml.org/sax/features/validation");
    }
    catch(Exception ex) { ; }
    
    return false;
  }

  private XmlSuite parseOneFile(SAXParser saxParser, String fileName)
    throws ParserConfigurationException, SAXException, IOException
  {
    TestNGContentHandler ch = new TestNGContentHandler(fileName);
    InputStream inputStream = new FileInputStream(fileName);
    saxParser.parse(inputStream, ch);     
    inputStream.close();
    XmlSuite result = ch.getSuite();
    
    return result;
  }
  
//  private static void ppp(String s) {
//    System.out.println("[Parser] " + s);
//  }
  
//  /**
//   *
//   * @param argv ignored
//   * @throws FileNotFoundException if the 
//   * @throws ParserConfigurationException
//   * @throws SAXException
//   * @throws IOException
//   * @since 1.0
//   */
//  public static void main(String[] argv) 
//    throws FileNotFoundException, ParserConfigurationException, SAXException, IOException 
//  {
//    XmlSuite l = 
//      new Parser("c:/eclipse-workspace/testng/test/testng.xml").parse();
//    
//    System.out.println(l);
//  }
}

