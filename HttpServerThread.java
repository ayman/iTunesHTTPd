//
//  HttpServerThread.java
//  ChumbiTunes
//
//  Created by David Ayman Shamma on 6/7/09.
//  Portions taken from Jon Berg - http://turtlemeat.com
//  Copyright (cc) 2009 shamurai.com. 

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.util.Date;

public class HttpServerThread extends Thread {  
  private ThreadGroup threads;
  private Object context; 
  private int port; 
    
  public HttpServerThread(int port, Object context) {
    this.setName("ChumbiTunesThread");
    this.threads = new ThreadGroup("ChumbiTunesWorkers");
    this.context = context;
    this.port = port;    
  }
  
  public static void out(String s) { 
    System.out.println(s);
  }
  
  public void run() {
    ServerSocket serversocket = null;
    out("ChumbiTunes httpserver v1.0.0.34\n");
    try {
      out("Trying to bind to localhost on port " + Integer.toString(port) + "...");
      serversocket = new ServerSocket(port);
    }
    catch (Exception e) { 
      out("\nFatal Error:" + e.getMessage());
      return;
    }
    
    while (true) {
      out("\nReady, Waiting for requests...\n");
      try {
        Socket connectionsocket = serversocket.accept();
        // hand this to a thread...
        HttpWorker w = new HttpWorker(this, connectionsocket);
        new Thread(this.threads, w).start();
      }
      catch (Exception e) { 
        out("\nError:" + e.getMessage());
      }         
    } 
  }
}
