//  BoxeeController.java
//  ChumbiTunes
//
//  Created by David Ayman Shamma on 9/7/09.
//  Copyright (cc) 2009 shamurai.com. 

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

public class BoxeeController {
  public static final String BOXEE = "boxee".intern();

  public static int BOXEE_DEFAULT_PORT = 8800;
  
  public static void next(String ip) {
    next(ip, BOXEE_DEFAULT_PORT);
  }

  public static void next(String ip, int port) {
    httpGet("http://" + ip + ":" + port + 
	    "/xbmcCmds/xbmcForm?command=next");  
  }

  public static void previous(String ip) {
    previous(ip, BOXEE_DEFAULT_PORT);
  }

  public static void previous(String ip, int port) {
    httpGet("http://" + ip + ":" + port + 
	    "/xbmcCmds/xbmcForm?command=previous");
  }

  public static void play(String ip) {
    play(ip, BOXEE_DEFAULT_PORT);
  }

  public static void play(String ip, int port) {
    httpGet("http://" + ip + ":" + port + 
	    "/xbmcCmds/xbmcForm?command=play");  
  }

  public static void pause(String ip) {
    pause(ip, BOXEE_DEFAULT_PORT);
  }

  public static void pause(String ip, int port) {
    httpGet("http://" + ip + ":" + port +
	    "/xbmcCmds/xbmcForm?command=pause");
  }

  public static void stop(String ip) {
    stop(ip, BOXEE_DEFAULT_PORT);
  }

  public static void stop(String ip, int port) {
    httpGet("http://" + ip + ":" + port + 
	    "/xbmcCmds/xbmcForm?command=stop");  
  }

  private static void httpGet(String u) {
    try {
      HttpURLConnection con = (HttpURLConnection) new URL(u).openConnection();
      con.setRequestMethod("GET");
      con.getOutputStream().write("LOGIN".getBytes("UTF-8"));
      con.getInputStream();
    } catch (Exception e) {
    } 
  }
}
