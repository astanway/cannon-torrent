// package cannon;

import java.io.*;
import java.util.*;
import java.net.*;

public class CannonClient {
	
	public static void main(String[] args) {
    
    String torrentFile = args[0];
    String savedFile = args[1];
    byte[] torrentArray = readTorrent(torrentFile);
    // Object decoded = Bencoder2.decode(torrentArray);
    //get url from decoded torrent
    try{ 
      byte[] response = getURL("http://abe.is"); 
    } catch (Exception e){}
	}
	
	public static byte[] readTorrent(String torrentFile) {
    StringBuffer buffer = new StringBuffer();
    try {
        FileInputStream fis = new FileInputStream(torrentFile);
        InputStreamReader isr = new InputStreamReader(fis, "UTF8");
        Reader in = new BufferedReader(isr);
        int ch;
        while ((ch = in.read()) > -1) {
                buffer.append((char)ch);
        }
        in.close();
        return buffer.toString().getBytes();
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
  }
  
  public static byte[] getURL(String url) throws Exception {
    URL destination = new URL(url);
    StringBuffer buffer = new StringBuffer();
    BufferedReader in = new BufferedReader( new InputStreamReader(destination.openStream()));
    int ch;
    while ((ch = in.read()) > -1) {
            buffer.append((char)ch);
    }
    in.close();
    return buffer.toString().getBytes();

    }
}

