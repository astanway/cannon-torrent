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
      byte[] escaped = new byte[20];
      decoded.info_hash.get(escaped, 0, escaped.length);
      String url = toURLHex(escaped);
      //get list of peers from this decoded response
      
      byte[] response = getURL(decoded.announce_url.toString());
      Object decoded_response = Bencoder2.decode(response);
      
    } catch (Exception e){
      System.out.println(e);
    }
	}
	
	public static final char[] HEX_CHARS = 
   {'0','1','2','3','4','5','6','7',
    '8','9','A','B','C','D','E','F'};

  public static String toURLHex(byte[] bytes){
     StringBuffer sb = new StringBuffer();
     for(int i = 0; i < bytes.length; ++i){
      sb.append('%')
       .append(HEX_CHARS[(bytes[i]>>4&0x0F)])
       .append(HEX_CHARS[(bytes[i]&0x0F)]);
     }
     return sb.toString();
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

