import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;

import utils.*;

import peers.Peer;
import peers.PeerManager;

public class RUBTClient {

  public static PeerManager manager = null;

	public static final String STARTED   = "started";
	public static final String COMPLETED = "completed";
	public static final String STOPPED   = "stopped";
	public static final String EMPTY     = "";

	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("USAGE: RUBTClient [torrent-file] [file-name]");
			System.exit(1);      
		}

		String torrentFile = args[0];
		String savedFile   = args[1];

	  //start the manager
		manager = new PeerManager();
		
		//set our peer id
		setPeerId();
		
	  getInfo(torrentFile, savedFile, manager);
		
		
		
		//query tracker
  	byte[] response = null;
  	int i = 0;
  	for (i=6881; i<=6889;){
  		try{
  			response = Helpers.getURL(manager.constructQuery(i, 0, 0, manager.TORRENT_INFO.file_length, EMPTY));
  			break;
  		} catch (Exception e){
  			System.out.println("Port " + i + " failed");
  			i++;
  			continue;
  		}
  	}

		//nab the fucker
		ArrayList<Peer> peerList = null;
		peerList = getPeers(response);

		manager.setPeerList(peerList);
		manager.download();		
	}
	
	public static void getInfo(String torrentFile, String savedFile, PeerManager manager){
  	try{
  		manager.TORRENT_INFO = new TorrentInfo(readTorrent(torrentFile));
  		manager.TORRENT_INFO.info_hash.get(manager.INFO_HASH, 0, manager.INFO_HASH.length);
  		manager.file = new RandomAccessFile(savedFile,"rws");
  	} catch (Exception e){
  	  System.out.println(e);
  		System.out.println("Torrent file could not be loaded.");
  		System.exit(1);
  	}
	}

  /**
   * Changes the torrentfile into a bytearray
   * @param torrentFile   the file to be read
   * @return byte array from the file
   */
  public static byte[] readTorrent(String torrentFile) {
    try {
      RandomAccessFile rFile = new RandomAccessFile(torrentFile,"rw");
      byte[] fileBytes = new byte[(int)rFile.length()];
      rFile.read(fileBytes);
      return fileBytes;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Generates our random PeerID
   */
  public static void setPeerId(){
    Random ran = new Random();
    int rand_id = ran.nextInt(5555555 - 1000000 + 1) + 1000000;
    String peer_id_string = "GROUP4AREL33t" + rand_id;
    manager.PEER_ID = peer_id_string.getBytes();
  }

	public static final ByteBuffer intervalKey = ByteBuffer.wrap(new byte[]{'i','n','t','e','r','v','a','l'});
	public static final ByteBuffer peersKey = ByteBuffer.wrap(new byte[]{'p','e','e','r','s'});
	public static final ByteBuffer minIntervalKey = 
		ByteBuffer.wrap(new byte[]{'m','i','n',' ','i','n','t','e','r','v','a','l'});
	public static final ByteBuffer downloadedKey =
		ByteBuffer.wrap(new byte[]{'d','o','w','n','l','o','a','d','e','d'});
	public static final ByteBuffer completeKey =
		ByteBuffer.wrap(new byte[]{'c','o','m','p','l','e','t','e'});
	public static final ByteBuffer ipKey = ByteBuffer.wrap(new byte[]{'i','p'});
	public static final ByteBuffer peerIdKey = ByteBuffer.wrap(new byte[]{'p','e','e','r',' ','i','d'});
	public static final ByteBuffer portKey = ByteBuffer.wrap(new byte[]{'p','o','r','t'});

	/**
	 * Gets the peer list from a response from the tracker
	 * @param response	byte array response from 
	 * @return			    returns the array list of peers
	 */
	public static ArrayList<Peer> getPeers(byte[] response){
		ArrayList<Peer> peerList = new ArrayList<Peer>();
		try{
			Object decodedResponse = Bencoder2.decode(response);
			// ToolKit.print(decodedResponse, 1);

			Map<ByteBuffer, Object> responseMap = (Map<ByteBuffer, Object>)decodedResponse;
			int interval = (Integer)responseMap.get(intervalKey);

			ArrayList<Object> peerArray = (ArrayList<Object>)responseMap.get(peersKey);

			for (int i = 0;i<peerArray.size();i++){
				Object peer = peerArray.get(i);
				String ip_ = "";
				String peer_id_ = "";
				int port_ = 0;

				Map<ByteBuffer, Object> peerMap = (Map<ByteBuffer, Object>)peer;
				ip_ = Helpers.bufferToString((ByteBuffer)peerMap.get(ipKey));
				peer_id_ = Helpers.bufferToString((ByteBuffer)peerMap.get(peerIdKey));
				port_ = (Integer)peerMap.get(portKey);
				System.out.println(ip_ +" " +  peer_id_ +" " +  port_);
				//get all the properties
				Peer newPeer = new Peer(peer_id_, ip_, port_);
				
				if(newPeer.isValid()){
				  peerList.add(newPeer);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}

		return peerList;
	}
}


