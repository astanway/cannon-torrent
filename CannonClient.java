import java.io.*;
import java.util.*;
import java.net.*;

import utils.TorrentInfo;
import utils.Bencoder2;

public class CannonClient {
	
	public static void main(String[] args) {
    
    String torrentFile = args[0];
    String savedFile = args[1];
    byte[] torrentArray = readTorrent(torrentFile);

    try{
      TorrentInfo decoded = new TorrentInfo(torrentArray);
      byte[] response = getURL(decoded.announce_url.toString());
      Object decoded_response = Bencoder2.decode(response);
      //get list of peers from this decoded response
      
    } catch (Exception e){
      System.out.println(e);
    }
	}
	
	public static byte[] readTorrent(String torrentFile) {
    StringBuffer buffer = new StringBuffer();
    try {
        FileInputStream fis = new FileInputStream(torrentFile);
        InputStreamReader isr = new InputStreamReader(fis);
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

