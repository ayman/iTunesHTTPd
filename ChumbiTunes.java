//  ChumbiTunes.java
//
//  Created by David Ayman Shamma on 6/7/09.
//  Copyright (cc) 2009 shamurai.com. 

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.MediaTracker;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.BorderFactory;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;

public class ChumbiTunes extends JFrame {
  private Font font = new Font("sanserif", Font.ITALIC, 24);
  private Font smallFont = new Font("sanserif", Font.PLAIN, 14);
  protected ResourceBundle resbundle;
  protected AboutBox aboutBox;
  protected PrefPane prefs;
  protected HttpServerThread server;
  private Application fApplication = Application.getApplication();
  private int port = 5050;
  private boolean newerVersion = false;
  private String newVersionString = "";
  
  public ChumbiTunes() {
    super("ChumbiTunes");
    resbundle = ResourceBundle.getBundle("strings", Locale.getDefault());
    setTitle(resbundle.getString("frameConstructor"));
    newerVersion = checkForNewerVersion();
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
    this.getContentPane().setLayout(new BorderLayout());
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
    JLabel iconLabel = new JLabel(s, icon, JLabel.LEFT);
    iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    panel.add(iconLabel);
    this.getContentPane().add(panel, BorderLayout.CENTER);
    if (newerVersion) {
      String l = "New Version " + newVersionString + " Available";
      JLabel newerLabel = new JLabel(l, JLabel.RIGHT);
      newerLabel.setForeground(Color.BLUE);
      newerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
      newerLabel.addMouseListener(new UrlMouseAdapter("http://shamurai.com/bin/chumbiTunes/"));
      this.getContentPane().add(newerLabel,
                                BorderLayout.PAGE_END);      
    }
    this.pack();    
  }
  
  private boolean checkForNewerVersion() {
    String u = "http://shamurai.com/bin/chumbiTunes/latest.php?v=";
    u += resbundle.getString("appVersion");
    try {
      URL url = new URL(u);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      InputStreamReader isr;
      isr = new InputStreamReader(con.getInputStream());
      BufferedReader reader = new BufferedReader(isr);
      String line = null;
      while((line = reader.readLine()) != null) {
        if (line.trim().equals("0")) {
          return false;
        } else {
          newVersionString = line.trim();
          return true;
        }
      }    
    } catch (Exception e) {
      return false;
    }
    return true;
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