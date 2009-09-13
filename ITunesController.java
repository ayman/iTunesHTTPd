//  ITunesController.java
//  ChumbiTunes
//
//  Created by David Ayman Shamma on 6/8/09.
//  Copyright (cc) 2009 shamurai.com. 

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ITunesController {
  public static final String PLAYING = "playing".intern();
  public static final String CAPABILITIES = "capabilities".intern();
  public static final String ART = "art".intern();
  public static final String SHUTTLE = "shuttle".intern();
  public static final String PLAYPAUSE = "playpause".intern();
  public static final String NEXT = "next".intern();
  public static final String PREVIOUS = "previous".intern();
  public static final String VOLUME = "volume".intern();       

  public static String playing() { 
    String[] args = { "osascript", 
                      "-e", "tell application \"iTunes\"", 
                      "-e", "try",
                      "-e", "set t to current track",
                      "-e", "on error",
                      "-e", "return",
                      "-e", "end try",
                      "-e", "if (exists raw data of artwork 1 of t) then",
                      "-e", "set hasArt to \"1\"",
                      "-e", "else",
                      "-e", "set hasArt to \"0\"",
                      "-e", "end if",                 
                      "-e", "kind of t & \"\n\" & artist of t & \"\n\" &  " + 
                            "album of t & \"\n\" & name of t & \"\n\" & " + 
                            "duration of t & \"\n\" &  player position & " +
                            "\"\n\" & hasArt & \"\n\" &  player state & " +
                            "\"\n\" & sound volume",
                      "-e", "end tell" };
    String status = exec(args);
    if (status.length() == 0) {
        return null;
    }
    String[] split = status.split("[\n]");
    
    String xml = "";
    xml += "<item>";
    xml += "<requestType>/playing/</requestType>";
    xml += "<playbackType>" + split[0] + "</playbackType>";
    xml += "<artist>" + split[1] + "</artist>"; 
    xml += "<album>" + split[2] + "</album>"; 
    xml += "<track>" + split[3] + "</track>"; 
    xml += "<duration>" + split[4] + "</duration>";
    xml += "<cursor>" + split[5] + "</cursor>"; 
    xml += "<hasart>" + split[6] + "</hasart>"; 
    xml += "<status>" + split[7] + "</status>"; 
    xml += "<volume>" + split[8] + "</volume>"; 
    xml += "<stream/>"; 
    xml += "</item>";   
    return xml;
  }
  
  public static String capabilities() {
    String xml = "";
    xml += "<item>";
    xml += "<version>1.0.0.34</version>";
    xml += "<version_name>nebula</version_name>";
    xml += "<version_major>1</version_major>";
    xml += "<version_minor>0</version_minor>";
    xml += "<version_revision>34</version_revision>";
    xml += "<capabilities>trackinfo, basicplayback</capabilities>";
    xml += "</item>";
    return xml;
  }
  
  public static byte[] art() {
    String[] args = { "osascript", 
      "-e", "tell application \"iTunes\"", 
      "-e", "raw data of artwork 1 of current track",
      "-e", "end tell" };
    String sbytes = exec(args);
    return hexToBytes(sbytes.substring(11).toCharArray());
  }
  
  public static String shuttle(String subcommand) {
    String[] args = { "osascript", 
                      "-e", "tell application \"iTunes\"", 
                      "-e", "playpause",
                      "-e", "player state",
                      "-e", "end tell" };
    if (ITunesController.NEXT == subcommand) {
      args[4] ="next track";
    } else if (ITunesController.PREVIOUS == subcommand) {
      args[4] ="previous track";
    }
    
    String status = exec(args);    
    String xml = "";
    xml += "<item>";
    xml += "<response>" + subcommand.toUpperCase() + "</response>";
    xml += "<status>" + status + "</status>";
    xml += "</item>";
    return xml;
  }

  public static String volume() {
    String[] args = { "osascript", 
                      "-e", "tell application \"iTunes\"", 
                      "-e", "sound volume",
                      "-e", "end tell" };    
    return volume(args);
  }     

  public static String volume(String level) {
    String[] args = { "osascript", 
                      "-e", "tell application \"iTunes\"", 
                      "-e", "set sound volume to " + level,
                      "-e", "sound volume",
                      "-e", "end tell" };
    return volume(args);
  }     
               
  protected static String volume(String[] args) {
    String status = exec(args);
    String xml = "";
    xml += "<item>";
    xml += "<response>VOLUME</response>";
    xml += "<status>" + status + "</status>";
    xml += "</item>";
    return xml;
  }
  
  private static String exec(String[] args) {
    String status = "";
    String line = null;
    InputStream is = null;
    try {
      Process process = Runtime.getRuntime().exec(args);
      is = process.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      while((line = reader.readLine()) != null) {
        status += line + "\n";
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try { 
        is.close();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }    
    return status;
  }
    
  private static byte[] hexToBytes(char[] hex) {
    int length = hex.length / 2;
    byte[] raw = new byte[length];
    for (int i = 0; i < length; i++) {
      int high = Character.digit(hex[i * 2], 16);
      int low = Character.digit(hex[i * 2 + 1], 16);
      int value = (high << 4) | low;
      if (value > 127) {
        value -= 256;
      }
      raw[i] = (byte) value;
    }
    return raw;
  }
}
