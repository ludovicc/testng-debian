package org.testng.xml;

import java.util.Properties;

import org.testng.reporters.XMLStringBuffer;

/**
 * This class describes the tag <method-selector>  in testng.xml.
 * 
 * Created on Sep 26, 2005
 * @author cbeust
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class XmlMethodSelector {
  // Either this:
  private String m_className;
  private int m_priority;
  
  // Or that:
  private String m_language;
  private String m_expression;
  
  public String getClassName() {
    return m_className;
  }
  
  public void setName(String name) {
    m_className = name;
  }
  
  /**
   * @return Returns the expression.
   */
  public String getExpression() {
    return m_expression;
  }
  
  /**
   * @param expression The expression to set.
   */
  public void setExpression(String expression) {
    m_expression = expression;
  }
  
  /**
   * @return Returns the language.
   */
  public String getLanguage() {
    return m_language;
  }
  
  /**
   * @param language The language to set.
   */
  public void setLanguage(String language) {
    m_language = language;
  }
  
  public int getPriority() {
    return m_priority;
  }
  
  public void setPriority(int priority) {
    m_priority = priority;
  }
  
  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);

    xsb.push("method-selector");
    
    if(null != m_className) {
      Properties clsProp = new Properties();
      clsProp.setProperty("name", getClassName());
      if(getPriority() != -1) {
        clsProp.setProperty("priority", String.valueOf(getPriority()));
      }
      xsb.addEmptyElement("selector-class", clsProp);
    }
    else {
      Properties scriptProp = new Properties();
      scriptProp.setProperty("language", getLanguage());
      xsb.push("script", scriptProp);
      xsb.addCDATA(getExpression());
      xsb.pop("script");
    }
    
    xsb.pop("method-selector");

    return xsb.toXML();
  }
}
