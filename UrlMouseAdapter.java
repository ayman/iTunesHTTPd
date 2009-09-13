//  UrlMouseAdapter.java
//  ChumbiTunes
//  Copyright (cc) 2009 shamurai.com. 

import java.awt.*;
import java.awt.event.*;
import javax.swing.JLabel;
import java.lang.reflect.Method;

public class UrlMouseAdapter extends java.awt.event.MouseAdapter {
  private String url;
  
  public UrlMouseAdapter(String url) {
    this.url = url;
  }
  
  public void mouseEntered(MouseEvent e) {
    JLabel jl = (JLabel) e.getSource();
    jl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) ;
  }
  
  public void mouseExited(MouseEvent e) {
    JLabel jl = (JLabel) e.getSource();
    jl.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
  }
  
  public void mouseClicked(MouseEvent e) {
    try {
      Class fileMgr = Class.forName("com.apple.eio.FileManager");
      Method openURL = fileMgr.getDeclaredMethod("openURL", 
                                                 new Class[] {String.class});
      openURL.invoke(null, new Object[] {url});
    }
    catch (Exception exception) {}
  }
}
