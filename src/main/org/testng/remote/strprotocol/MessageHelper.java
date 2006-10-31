package org.testng.remote.strprotocol;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.ITestResult;


/**
 * Marshal/unmarshal tool for <code>IStringMessage</code>s.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class MessageHelper {
  public static final char DELIMITER = '\u0001';
  public static final char PARAM_DELIMITER = '\u0004';
  private static final char LINE_SEP_DELIMITER_1 = '\u0002';
  private static final char LINE_SEP_DELIMITER_2 = '\u0003';
  
  public static final int GENERIC_SUITE_COUNT = 1;
  
  public static final int SUITE = 10;
  public static final int SUITE_START = 11;
  public static final int SUITE_FINISH = 12;
  
  public static final int TEST = 100;
  public static final int TEST_START = 101;
  public static final int TEST_FINISH = 102;
  
  public static final int TEST_RESULT = 1000;
  public static final int PASSED_TEST = TEST_RESULT + ITestResult.SUCCESS;
  public static final int FAILED_TEST = TEST_RESULT + ITestResult.FAILURE;
  public static final int SKIPPED_TEST = TEST_RESULT + ITestResult.SKIP;
  public static final int FAILED_ON_PERCENTAGE_TEST = TEST_RESULT + ITestResult.SUCCESS_PERCENTAGE_FAILURE;
  public static final int TEST_STARTED = TEST_RESULT + ITestResult.STARTED;
  
  public static final String STOP_MSG = ">STOP";
  public static final String ACK_MSG = ">ACK";
  
  public static int getMessageType(final String message) {
    int idx = message.indexOf(DELIMITER);
    
    return idx == -1 ? Integer.parseInt(message) : Integer.parseInt(message.substring(0, idx));
  }
  
  public static GenericMessage unmarshallGenericMessage(final String message) {
    String[] messageParts = parseMessage(message);
    if(messageParts.length == 1) {
      return new GenericMessage(Integer.parseInt(messageParts[0]));
    }
    else {
      Map props = new HashMap();
      
      for(int i = 1; i < messageParts.length; i+=2) {
        props.put(messageParts[i], messageParts[i + 1]);
      }
      
      return new GenericMessage(Integer.parseInt(messageParts[0]), props);
    }
  }
  
  public static SuiteMessage createSuiteMessage(final String message) {
    int type = getMessageType(message);
    String[] messageParts = parseMessage(message);
    
    return new SuiteMessage(messageParts[1],
                            MessageHelper.SUITE_START == type,
                            Integer.parseInt(messageParts[2]));
  }
  
  public static TestMessage createTestMessage(final String message) {
    int type = getMessageType(message);
    String[] messageParts = parseMessage(message);
    
    return new TestMessage(MessageHelper.TEST_START == type,
                           messageParts[1],
                           messageParts[2],
                           Integer.parseInt(messageParts[3]),
                           Integer.parseInt(messageParts[4]),
                           Integer.parseInt(messageParts[5]),
                           Integer.parseInt(messageParts[6]),
                           Integer.parseInt(messageParts[7]));
  }
  
  public static TestResultMessage unmarshallTestResultMessage(final String message) {
    String[] messageParts = parseMessage(message);
    
    String parametersFragment= null;
    String startTimestampFragment= null;
    String stopTimestampFragment= null;
    String stackTraceFragment= null;
    if(messageParts.length == 9) {
      parametersFragment= messageParts[5];
      startTimestampFragment= messageParts[6];
      stopTimestampFragment= messageParts[7];
      stackTraceFragment= messageParts[8];
    }
    else {
      // HINT: old protocol without parameters
      parametersFragment= null;
      startTimestampFragment= messageParts[5];
      stopTimestampFragment= messageParts[6];
      stackTraceFragment= messageParts[7];
    }
    return new TestResultMessage(Integer.parseInt(messageParts[0]),
                                 messageParts[1],
                                 messageParts[2],
                                 messageParts[3],
                                 messageParts[4],
                                 parseParameters(parametersFragment),
                                 Long.parseLong(startTimestampFragment),
                                 Long.parseLong(stopTimestampFragment),
                                 replaceNewLineReplacer(stackTraceFragment)
            );
  }
  
  public static String replaceNewLine(String message) {
    if(null == message) {
      return message;
    }
    
    return message.replace('\n', LINE_SEP_DELIMITER_1).replace('\r', LINE_SEP_DELIMITER_2);
  }
  
  public static String replaceNewLineReplacer(String message) {
    if(null == message) {
      return message;
    }
    
    return message.replace(LINE_SEP_DELIMITER_1, '\n').replace(LINE_SEP_DELIMITER_2, '\r');
  }
  
  private static String[] parseParameters(final String messagePart) {
    return tokenize(messagePart, PARAM_DELIMITER);
  }
  
  private static String[] parseMessage(final String message) {
    return tokenize(message, DELIMITER);
  }
  
  private static String[] tokenize(final String message, final char separator) {
    if(null == message) {
      return new String[0];
    }
    
    List<String> tokens = new ArrayList<String>();
    int start = 0;
    for(int i = 0; i < message.length(); i++) {
      if(separator == message.charAt(i)) {
        tokens.add(message.substring(start, i));
        start = i + 1;
      }
    }
    if(start < message.length()) {
      tokens.add(message.substring(start, message.length()));
    }
    
    return tokens.toArray(new String[tokens.size()]);
  }
}
