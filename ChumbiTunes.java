//
//  ChumbiTunes.java
//
//  Created by David Ayman Shamma on 6/7/09.
//  Copyright (cc) 2009 shamurai.com. 

import java.util.Locale;
import java.util.ResourceBundle;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.*;

import com.apple.eawt.*;

public class ChumbiTunes extends JFrame {
  private Font font = new Font("sanserif", Font.ITALIC, 24);
  private Font smallFont = new Font("sanserif", Font.PLAIN, 14);
  protected ResourceBundle resbundle;
  protected AboutBox aboutBox;
  protected PrefPane prefs;
  protected HttpServerThread server;
  private Application fApplication = Application.getApplication();
  private int port = 5050;
  
  public ChumbiTunes() {
    super("ChumbiTunes");
    resbundle = ResourceBundle.getBundle("strings", Locale.getDefault());
    setTitle(resbundle.getString("frameConstructor"));
    initUI();
    server = new HttpServerThread(port, this);
    server.start();
    
    fApplication.setEnabledPreferencesMenu(true);
    fApplication.addApplicationListener(new com.apple.eawt.ApplicationAdapter() {
        public void handleAbout(ApplicationEvent e) {
          if (aboutBox == null) {
            aboutBox = new AboutBox();
          }
          about(e);
          e.setHandled(true);
        }
        public void handleOpenApplication(ApplicationEvent e) {
        }
//      public void handlePreferences(ApplicationEvent e) {
//        if (prefs == null) {
//          prefs = new PrefPane();
//        }
//        preferences(e);
//      }
        public void handleQuit(ApplicationEvent e) {
          quit(e);
        }
      });
    
    setResizable(false);
    setVisible(true);
  }
  
  protected void initUI() {
    this.setBackground(Color.white);
    this.getContentPane().setLayout(new FlowLayout());
    Panel panel = new Panel(new GridLayout(1, 1));
    URL imgURL = getClass().getResource("chumbitunes.png");
    Toolkit tk = Toolkit.getDefaultToolkit();
    MediaTracker m = new MediaTracker(this);
    Image img = tk.getImage(imgURL);
    this.setIconImage(img);
    ImageIcon icon = new ImageIcon(img);
    
    String ipAddrStr = "";
    try {
      InetAddress addr = InetAddress.getLocalHost();      
      byte[] ipAddr = addr.getAddress();            
      for (int i = 0; i < ipAddr.length; i++) {
        if (i > 0) {
          ipAddrStr += ".";
        }
        ipAddrStr += ipAddr[i]&0xFF;
      }      
    } catch (UnknownHostException e) {}
    String ip = "IP: <i>" + ipAddrStr + "</i><br>Port: <i>" + port + "</i>";
    String s = "<html><b><font size=+1>ChumbiTunes</font></b><br>";
    s += "<font size=-1>" + ip + "</font></html>";
    panel.add(new JLabel(s, icon, JLabel.LEFT));
    this.getContentPane().add(panel, BorderLayout.CENTER);
    this.pack();    
  }
  
  public void about(ApplicationEvent e) {
    aboutBox.setResizable(false);
    aboutBox.setVisible(true);
  }
  
  public void preferences(ApplicationEvent e) {
    prefs.setResizable(false);
    prefs.setVisible(true);
  }
  
  public void quit(ApplicationEvent e) {        
    System.exit(0);
  }
    
  public static void main(String args[]) {
    new ChumbiTunes();
  }
}