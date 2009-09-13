//  HttpServerThread.java
//  ChumbiTunes
//
//  Created by David Ayman Shamma on 6/7/09.
//  Portions taken from Jon Berg - http://turtlemeat.com
//  Copyright (cc) 2009 shamurai.com. 

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.cert.*;

public class HttpWorker implements Runnable {
  private HttpServerThread parent;
  private Socket connectionsocket;
  
  final static int METHOD_NA = 0;
  final static int METHOD_GET = 1;
  final static int METHOD_HEAD = 2;
  
  final static int IMAGE_JPEG = 1;
  final static int IMAGE_GIF = 2;
  final static int IMAGE_PNG = 3;
  final static int TEXT_XML = 4;
  final static int TEXT_HTML = 5;  
  final static int TEXT_PLAIN = 6;
  
  final static String CROSSDOMAIN_XML = "<?xml version=\"1.0\"?>" +
    "<!DOCTYPE cross-domain-policy SYSTEM " +
    "\"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">" +
    "<cross-domain-policy>" +
    "<site-control permitted-cross-domain-policies=\"master-only\" />"+
    "<allow-access-from domain=\"*\" />" +
    "</cross-domain-policy>";

  public HttpWorker(HttpServerThread parent, 
                    Socket connectionsocket) {
    this.parent = parent;
    this.connectionsocket = connectionsocket;
  }
  
  public void run() {
    try {
      InetAddress client = connectionsocket.getInetAddress();
      parent.out(client.getHostName() + " connected to server.\n");
      InputStreamReader isr;
      isr = new InputStreamReader(connectionsocket.getInputStream());
      BufferedReader input = new BufferedReader(isr);
      DataOutputStream output;
      output = new DataOutputStream(connectionsocket.getOutputStream());
      http_handler(input, output);    
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
    
  private void http_handler(BufferedReader input, 
                            DataOutputStream output) {
    int method = HttpWorker.METHOD_NA; 
    String http = new String(); 
    String path = new String(); 
    String file = new String(); 
    String user_agent = new String(); 
    String[] request = null;
    try {
      String lineIn = input.readLine(); 
      request = lineIn.split("[ ]");
      parent.out(lineIn);
      if (request.length < 3 ||
          !request[2].startsWith("HTTP/")) {
        output.writeBytes(makeHttpHeader(HttpURLConnection.HTTP_BAD_REQUEST));
        output.writeBytes("<h2>400 Bad Request</h2>");
        output.close();
        return;
      }
      
      request[0] = request[0].intern();
      if (request[0] == "GET".intern()) { 
        method = HttpWorker.METHOD_GET;
      } else if (request[0] == "HEAD".intern()) { 
        method = HttpWorker.METHOD_HEAD;
      } else { 
        String ni = makeHttpHeader(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
        output.writeBytes(ni);
        output.writeBytes("<h2>501 Not Implemented</h2>");
        output.close();
        return;
      }
    } catch (Exception e3) {
      parent.out("error:" + e3.getMessage());
    } 
    
    path = request[1].substring(1);
    String[] commandParam = path.split("[?]");
    String[] commandSplit = commandParam[0].split("[/]");
    String command = commandSplit[0].intern();
    String arg0 = (commandSplit.length > 1) ? commandSplit[1].intern() : null;
    String arg1 = (commandSplit.length > 2) ? commandSplit[2].intern() : null;
    String param = (commandParam.length > 1) ? commandParam[1].intern() : null;
    parent.out("\nClient requested:" + path);
    parent.out("\nCommand:" + command);
    parent.out("\nParam:" + param + "\n");
    int code = 200;
    
    String header = "";
    String httpOut = "";
    byte[] artOut = null;
    try {
      if (command == "robots.txt".intern()) {
        header = makeHttpHeader(HttpURLConnection.HTTP_OK, 
                                HttpWorker.TEXT_PLAIN);
        httpOut = "User-agent: *\n";
        httpOut += "Disallow: *";
      } else if (command == "crossdomain.xml".intern()) {
        header = makeHttpHeader(HttpURLConnection.HTTP_OK, 
                                HttpWorker.TEXT_XML);
        httpOut = CROSSDOMAIN_XML;
      } else if (ITunesController.PLAYING == command) {
        httpOut = ITunesController.playing();
        if (null != httpOut) {
          header = makeHttpHeader(HttpURLConnection.HTTP_OK, 
                                  HttpWorker.TEXT_XML);
        } else {
          header = makeHttpHeader(HttpURLConnection.HTTP_INTERNAL_ERROR, 
                                  HttpWorker.TEXT_HTML);
          httpOut = "<H2>iTunes is not running/playing.</H2>";
        }
      } else if (ITunesController.CAPABILITIES == command) {
        header = makeHttpHeader(HttpURLConnection.HTTP_OK, 
                                HttpWorker.TEXT_XML);
        httpOut = ITunesController.capabilities();
      } else if (ITunesController.ART == command) {
        header = makeHttpHeader(HttpURLConnection.HTTP_OK, 
                                HttpWorker.IMAGE_JPEG);
        artOut = ITunesController.art();
      } else if (ITunesController.SHUTTLE == command) {
        if (ITunesController.PLAYPAUSE == arg0 ||
            ITunesController.NEXT == arg0 ||
            ITunesController.PREVIOUS == arg0) {
          header = makeHttpHeader(HttpURLConnection.HTTP_OK, 
                                  HttpWorker.TEXT_XML);
          httpOut = ITunesController.shuttle(arg0);
        } else {
          throw new Exception();
        }
      } else if (ITunesController.VOLUME == command) {
        header = makeHttpHeader(HttpURLConnection.HTTP_OK, 
                                HttpWorker.TEXT_XML);
        if (null == arg0) {
          httpOut = ITunesController.volume();
        } else {
          httpOut = ITunesController.volume(arg0);
        }
      } else if (TivoController.TIVO == command) {
        if (null == arg0 ||
            !arg0.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
          code = HttpURLConnection.HTTP_NOT_FOUND;
          header = makeHttpHeader(HttpURLConnection.HTTP_NOT_FOUND); 
          httpOut = "<H2>Error, Unknown Request</H2>";
        } else {
          if (arg1.matches("[\\d]+")) {            
            header = makeHttpHeader(HttpURLConnection.HTTP_OK,
                                    HttpWorker.TEXT_XML);            
            httpOut = TivoController.nowPlaying(arg0, arg1);
          } else if (arg1.matches("pause")) {
            TivoController.pause(arg0);
          } else if (arg1.matches("play")) {
            TivoController.pause(arg0);
          }
        }
      } else if (BoxeeController.BOXEE == command) {
        if (arg1.matches("pause")) {
          BoxeeController.pause("127.0.0.1");
        } else if (arg1.matches("play")) {
          BoxeeController.play("127.0.0.1");
        } else if (arg1.matches("next")) {
          BoxeeController.next("127.0.0.1");
        } else if (arg1.matches("previous")) {
          BoxeeController.previous("127.0.0.1");
        } else if (arg1.matches("stop")) {
          BoxeeController.stop("127.0.0.1");
        }
      } else {
        code = 404;
        header = makeHttpHeader(HttpURLConnection.HTTP_NOT_FOUND); 
        httpOut = "<H2>Error, Unknown Request</H2>";
      }
    } catch (Exception e) {
      parent.out("error: " + e.getMessage());
      try { 
        output.writeBytes(makeHttpHeader(HttpURLConnection.HTTP_NOT_FOUND)); 
        output.close();
      } catch (IOException ioe) {}
      return;
    } 
    
    try {
      output.writeBytes(header);
      if (method == HttpWorker.METHOD_GET) { 
        if (ITunesController.ART == command) {
          output.write(artOut, 0, artOut.length);
        } else {
          output.writeBytes(httpOut);
        }
      }
      output.close();
    } catch (Exception e) {}                           
  }
    
  private String makeHttpHeader(int return_code) {
    return makeHttpHeader(return_code, 0);
  }
  
  private String makeHttpHeader(int return_code, int file_type) {
    String s = "HTTP/1.0 ";
    switch (return_code) {
      case 200:
        s = s + "200 OK";
        break;
      case 400:
        s = s + "400 Bad Request";
        break;
      case 403:
        s = s + "403 Forbidden";
        break;
      case 404:
        s = s + "404 Not Found";
        break;
      case 500:
        s = s + "500 Internal Server Error";
        break;
      case 501:
        s = s + "501 Not Implemented";
        break;
    }
    
    s = s + "\r\n"; 
    s = s + "Connection: close\r\n"; 
    s = s + "Server: ChumbiTunes v1\r\n";
    
    switch (file_type) {
      case HttpWorker.IMAGE_JPEG:
        s = s + "Content-Type: image/jpeg\r\n";
        break;
      case HttpWorker.IMAGE_GIF:
        s = s + "Content-Type: image/gif\r\n";
        break;
      case HttpWorker.TEXT_XML: 
        s = s + "Content-Type: text/xml\r\n";
        break;
      case HttpWorker.TEXT_PLAIN: 
        s = s + "Content-Type: text/plain\r\n";
        break;
      case HttpWorker.TEXT_HTML: 
      default:
        s = s + "Content-Type: text/html\r\n";
        break;
    }
    s = s + "\r\n";
    return s;
  }                       
}
