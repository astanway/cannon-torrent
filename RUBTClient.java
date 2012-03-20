import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;

import utils.*;

import peers.Peer;
import peers.PeerManager;

public class RUBTClient {

  public static PeerManager manager = null;

	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("USAGE: RUBTClient [torrent-file] [file-name]");
			System.exit(1);      
		}

		String torrentFile = args[0];
		String savedFile   = args[1];

	  //start the manager
		manager = new PeerManager();
	  manager.setInfo(torrentFile, savedFile);
		manager.setPeerId();
		manager.setPeerList(getPeers());
		manager.download();
	}
	
	//query the tracker and get the initial list of peers
	public static ArrayList<Peer> getPeers(){
  	byte[] response = null;
  	int i = 0;
  	for (i=6881; i<=6889;){
  		try{
  			response = Helpers.getURL(manager.constructQuery(i, 0, 0, manager.TORRENT_INFO.file_length, ""));
  			break;
  		} catch (Exception e){
  			System.out.println("Port " + i + " failed");
  			i++;
  			continue;
  		}
  	}
  	
  	return Helpers.getPeerList(response);
	}
}


