//
//  File:   AboutBox.java
//  Copyright (cc) 2009 shamurai.com. 

import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;

import java.lang.reflect.Method;

import com.apple.eio.FileManager;

public class AboutBox extends JFrame implements ActionListener {
  protected JLabel titleLabel, aboutLabel[];
  protected static int labelCount = 8;
  protected static int aboutWidth = 280;
  protected static int aboutHeight = 230;
  protected static int aboutTop = 200;
  protected static int aboutLeft = 350;
  protected Font titleFont, bodyFont, bodyUFont;
  protected ResourceBundle resbundle;

  public AboutBox() {
    super("");
    this.setResizable(false);
    resbundle = ResourceBundle.getBundle ("strings", Locale.getDefault());
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow); 
                
    titleFont = new Font("Lucida Grande", Font.BOLD, 14);
    if (titleFont == null) {
      titleFont = new Font("SansSerif", Font.BOLD, 14);
    }
    bodyFont  = new Font("Lucida Grande", Font.PLAIN, 10);
    if (bodyFont == null) {
      bodyFont = new Font("SansSerif", Font.PLAIN, 10);
    }
                
    this.getContentPane().setLayout(new BorderLayout(15, 15));
        
    aboutLabel = new JLabel[labelCount];
    aboutLabel[0] = new JLabel("");
    aboutLabel[1] = new JLabel(resbundle.getString("frameConstructor"));
    aboutLabel[1].setFont(titleFont);
    aboutLabel[2] = new JLabel(resbundle.getString("appVersion"));
    aboutLabel[2].setFont(bodyFont);
    aboutLabel[3] = new JLabel("<html><u>http://tinyurl.com/chumbiTunes</u></html>");
    aboutLabel[3].setForeground(Color.BLUE);
    aboutLabel[3].addMouseListener(new UrlMouseAdapter("http://tinyurl.com/chumbiTunes"));
    aboutLabel[4] = new JLabel("<html><u>http://shamurai.com/</u></html>");
    aboutLabel[4].setForeground(Color.BLUE);
    aboutLabel[4].addMouseListener(new UrlMouseAdapter("http://shamurai.com/"));
    aboutLabel[5] = new JLabel("JDK " + System.getProperty("java.version"));
    aboutLabel[5].setFont(bodyFont);
    aboutLabel[6] = new JLabel(resbundle.getString("copyright"));
    aboutLabel[6].setFont(bodyFont);
    aboutLabel[7] = new JLabel("");             
                
    Panel textPanel2 = new Panel(new GridLayout(labelCount, 1));
    for (int i = 0; i<labelCount; i++) {
      aboutLabel[i].setHorizontalAlignment(JLabel.CENTER);
      textPanel2.add(aboutLabel[i]);
    }
    this.getContentPane().add (textPanel2, BorderLayout.CENTER);
    this.pack();
    this.setLocation(aboutLeft, aboutTop);
    this.setSize(aboutWidth, aboutHeight);
  }


  class SymWindow extends java.awt.event.WindowAdapter {
    public void windowClosing(java.awt.event.WindowEvent event) {
      setVisible(false);
    }
  }

  class UrlMouseAdapter extends java.awt.event.MouseAdapter {
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
    
  public void actionPerformed(ActionEvent newEvent) {
    setVisible(false);
  }             
}