//  TivoController.java
//  ChumbiTunes
//
//  Created by David Ayman Shamma on 9/7/09.
//  Copyright (cc) 2009 shamurai.com. 

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.Socket;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TivoController implements HostnameVerifier {
  public static final String TIVO = "tivo".intern();
  /* This port is hard coded on the Tivo Series 3 v9.1 or greater */
  public static final int TIVO_REMOTE_PORT = 31339;
  public static final String IRCODE = "IRCODE";
  public static final String TELEPORT = "TELEPORT";
  public static final String IRCODE_PAUSE = "PAUSE";
  public static final String IRCODE_PLAY = "PLAY";
  public static final String TELEPORT_NOWPLAYING = "NOWPLAYING";  
  public static final String TIVO_OK = "OK";
  public static final String TIVO_FAIL = "FAIL";
  
  public static String nowPlaying(String ip, String key) 
    throws NoSuchAlgorithmException, 
           KeyManagementException,
           MalformedURLException, 
           IOException, 
           ProtocolException {
    final TrustManager[] trustAllCerts = new TrustManager[] { 
      new X509TrustManager() {
        public void checkClientTrusted(final X509Certificate[] chain, 
                                       final String authType ) 
          throws CertificateException {}
        public void checkServerTrusted(final X509Certificate[] chain, 
                                       final String authType ) 
          throws CertificateException {}
        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }
      } 
    };
    
    final SSLContext sslContext = SSLContext.getInstance("SSL");
    sslContext.init(null, 
                    trustAllCerts, 
                    new java.security.SecureRandom());
    final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
    final String login = "tivo";
    final String password = key;
    Authenticator.setDefault(new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(login, 
                                            password.toCharArray());
        }
      });    
    String tivoUrl = "https://" + ip;
    tivoUrl += "/TiVoConnect?Command=QueryContainer";
    tivoUrl += "&Container=%2FNowPlaying&Recurse=Yes";
    System.out.println(tivoUrl);
    URL url = new URL(tivoUrl);
    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
    con.setHostnameVerifier(new TivoController());
    con.setSSLSocketFactory(sslSocketFactory);
    con.setRequestMethod("GET");
    InputStreamReader isr;
    isr = new InputStreamReader(con.getInputStream());
    BufferedReader reader = new BufferedReader(isr);
    String line = null;
    String result = "";
    while((line = reader.readLine()) != null) {
      result += line;
    }
    return result;
  }
  
  public static String pause(String ip) {
    return ircode(ip, IRCODE_PAUSE);
  }

  public static String play(String ip) {
    return ircode(ip, IRCODE_PLAY);
  }
  
  private static String ircode(String ip, String cmd) {
    return sendMsg(ip, IRCODE + " " + cmd);
  }
  
  private static String sendMsg(String ip, String cmd) {
    Socket so = null;
    DataOutputStream out = null;
    BufferedReader in = null;
    try { 
      so = new Socket(InetAddress.getByName(ip), TIVO_REMOTE_PORT);
      out = new DataOutputStream(so.getOutputStream());
      in = new BufferedReader(new InputStreamReader(so.getInputStream()));
      String s = in.readLine();
      out.writeBytes(cmd + "\r\n");
      out.flush();
      out.close();
      in.close();
      so.close();
    } catch (Exception e) {
      return recieve(cmd, TIVO_FAIL);
    }
    return recieve(cmd, TIVO_OK);
  }
  
  private static String recieve(String cmd, String status) {
    String xml = "";
    xml += "<item>";
    xml += "<response>" + cmd.toUpperCase() + "</response>";
    xml += "<status>" + status + "</status>";
    xml += "</item>";
    return xml;    
  }
  
  public boolean verify(String hostname, SSLSession session) {
    return true;
  } 
}
