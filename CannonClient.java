import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;

import utils.TorrentInfo;
import utils.Bencoder2;
import utils.Helpers;
import utils.ToolKit;

import peers.Peer;

public class CannonClient {
  
  public static byte[] PEER_ID = new byte[20];
  public static byte[] INFO_HASH = new byte[20];
	
	public static void main(String[] args) {
    
    String torrentFile = args[0];
    String savedFile = args[1];
    byte[] torrentArray = readTorrent(torrentFile);
    String url = constructQuery(torrentArray); 
    byte[] response = getURL(url);
    ArrayList<Peer> peerList = getPeers(response);
    Peer.sendHandshake(PEER_ID, INFO_HASH);
	}
	
	public static ArrayList<Peer> getPeers(byte[] response){
	  ArrayList<Peer> peerList = new ArrayList<Peer>(10);

	  try{
      //decode the response and slap it in an array for perusal
      Object decodedResponse = Bencoder2.decode(response);
      // ToolKit.print(decodedResponse, 1);
      
      Map<ByteBuffer, Object> responseMap = (Map<ByteBuffer, Object>)decodedResponse;
      Object[] responseArray = responseMap.values().toArray();
      
      //the interval returned by the tracker
      int interval = (Integer)responseArray[0];
      
      //the list of peers returned by the tracker
      Object peers = responseArray[1];

      ArrayList<Object> peerArray = (ArrayList<Object>)peers;
      
      //iterate through the list
      for (Object peer : peerArray){
        Map<ByteBuffer, Object> peerMap = (Map<ByteBuffer, Object>)peer;
        peers.Peer newPeer = new Peer();
        
        //get all the properties
        for (Map.Entry<ByteBuffer, Object> entry : peerMap.entrySet()){          
           String key = Helpers.bufferToString(entry.getKey());
           Object value = entry.getValue();
           if (key.compareTo("ip") == 0){
             //TODO: substitute this method for a method in ToolKit
             String ip = Helpers.bufferToString((ByteBuffer)value);
             Peer.ip_ = ip;
           }
           if (key.compareTo("peer id") == 0){
             String peer_id = Helpers.bufferToString((ByteBuffer)value);
             Peer.peer_id_ = peer_id;
           }
           if (key.compareTo("port") == 0){
             int port = (Integer)value;
             Peer.port_ = port;
          }
        }
        
        //add the fleshed out peer to the peerList
        peerList.add(newPeer);
      }
    } catch (Exception e){
      System.out.print(e);
    }

    return peerList;
	}
	
	//construct the url for initially querying the tracker
	public static String constructQuery(byte[] torrentArray){
	  String url_string = "";
	  try{
      //get initial decoded torrent inf0rz
      TorrentInfo decoded = new TorrentInfo(torrentArray);
      
      //get info hash
      decoded.info_hash.get(INFO_HASH, 0, INFO_HASH.length);
      String escaped_hash = Helpers.toURLHex(INFO_HASH);
      
      //generate random id
      Random ran = new Random();
      int rand_id = ran.nextInt(5555555 - 1000000 + 1) + 1000000;
      String peer_id_string = "GROUP4AREL33t" + rand_id;
      PEER_ID = peer_id_string.getBytes();
      
      //set the port
      //TODO: add cycle logic
      int port = 6881;
      
      //uploaded
      int uploaded = 0;
      
      //downloaded
      int downloaded = 0;
      
      //left
      int left = 0;
      
      //ip address
      String ip = "128.6.5.130";
      
      url_string = decoded.announce_url.toString() 
                    + "?port=" + port
                    + "&peer_id=" + peer_id_string
                    + "&info_hash=" + escaped_hash 
                    + "&uploaded=" + uploaded
                    + "&downloaded=" + downloaded
                    + "&left=" + left
                    + "&ip=" +  ip;

    } catch (Exception e){
      System.out.println(e);
    }
    
    return url_string;
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
  
  
  //returns a byte[] consisting of the contents at the given url
  public static byte[] getURL(String string_url) {
    ByteArrayOutputStream bais = new ByteArrayOutputStream();
    InputStream is = null;
    try {
      URL url = new URL(string_url);
      is = url.openStream();
      byte[] byteChunk = new byte[4096];
      int n;

      while ( (n = is.read(byteChunk)) > 0 ) {
        bais.write(byteChunk, 0, n);
      }
      
      is.close();
    }
    catch (IOException e) {
    }
    
    return bais.toByteArray();
  }
}

