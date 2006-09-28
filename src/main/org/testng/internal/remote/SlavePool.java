package org.testng.internal.remote;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.testng.remote.ConnectionInfo;


/**
 * This class maintains a pool of slaves (represented by sockets).
 * 
 * @author cbeust
 */
public class SlavePool {
  private static SocketLinkedBlockingQueue m_hosts = new SocketLinkedBlockingQueue();
  private static Map<Socket, ConnectionInfo> m_connectionInfos =
    new HashMap<Socket, ConnectionInfo>();
  
  public void addSlaves(Socket[] slaves) throws IOException {
    for (Socket s : slaves) {
      addSlave(s);
    }
  }
  
  public void addSlave(Socket s) {
    ConnectionInfo ci = new ConnectionInfo();
    ci.setSocket(s);
    addSlave(s, ci);
  }
  
  private void addSlave(Socket s, ConnectionInfo ci) {
    m_hosts.add(s);
    m_connectionInfos.put(s, ci);
  }
  
  public ConnectionInfo getSlave() {
    ConnectionInfo result = null;
    Socket host = null;
    
    try {
      host = m_hosts.take();
      result = m_connectionInfos.get(host);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    return result;
  }
  
  public void returnSlave(ConnectionInfo slave) throws IOException {
    m_hosts.add(slave.getSocket());
//    ConnectionInfo ci = m_connectionInfos.remove(slave.socket);
//    ci.oos.close();
//    ci.ois.close();
//    addSlave(slave.socket);
  }

}
