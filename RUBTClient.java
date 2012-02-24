import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;

import utils.TorrentInfo;
import utils.Bencoder2;
import utils.Helpers;
import utils.ToolKit;

import peers.Peer;

public class RUBTClient {

	public static byte[] PEER_ID             = new byte[20];
	public static byte[] INFO_HASH           = new byte[20];
	public static TorrentInfo TORRENT_INFO   = null;
	public static RandomAccessFile file      = null;
	public static int BLOCK_LENGTH           = 16384;
	public static boolean[] HAVE_PIECE       = null;
	
	public final String STARTED   = "started"
	public final String COMPLETED = "completed"
	public final String STOPPED   = "stopped"
	public final String EMPTY     = ""

	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("USAGE: RUBTClient [torrent-file] [file-name]");
			System.exit(1);      
		}

		String torrentFile = args[0];
		String savedFile   = args[1];

    //set our peer id
		setPeerId();
    
    //set up torrent info
		try{
			TORRENT_INFO = new TorrentInfo(readTorrent(torrentFile));
			TORRENT_INFO.info_hash.get(INFO_HASH, 0, INFO_HASH.length);
			file = new RandomAccessFile(savedFile,"rws");
		} catch (Exception e){
			System.out.println("Torrent file could not be loaded.");
			System.exit(1);
		}

		//query tracker
		byte[] response = null;
		int i = 0;
		for (i=6881; i<=6889;){
			try{
				response = getURL(constructQuery(i, 0, 0, TORRENT_INFO.file_length, EMPTY));
				break;
			} catch (Exception e){
				System.out.println("Port " + i + " failed");
				i++;
				continue;
			}
		}
    
    //loop through peers
		ArrayList<Peer> peerList = getPeers(response);
		for(Peer peer : peerList){
			if (peer.isValid()){
				System.out.println("Peer Found");
				peer.createSocket(peer.ip_, peer.port_);
				peer.establishStreams();
				peer.sendHandshake(PEER_ID, INFO_HASH);
				if(peer.receiveHandshake(INFO_HASH)){
				  
					peer.sendMessage(Peer.INTERESTED);
					
					while(true){ if(peer.listenForUnchoke()){ break; }}
					
					response = getURL(constructQuery(i, TORRENT_INFO.file_length, 0, 0, STARTED));
					
					download(peer);

		      response = getURL(constructQuery(i, 0, TORRENT_INFO.file_length, 0, COMPLETED));

					peer.closeSocket();
				}
			}
		}

		response = getURL(constructQuery(i, 0, TORRENT_INFO.file_length, 0, STOPPED));
		System.out.println("\nFile finished.");
	}

	/**	
	 * Downloads the pieces from the peer and stores them in the appropriate file
	 * @param peer the peer object to be downloading from
	 */
	public static void download (Peer peer){
		int numPieces        = 0;
		int numLeft          = 0;
		int leftoverBytes    = 0;
		int blocksPerPiece   = 0;

		numLeft = numPieces = TORRENT_INFO.file_length / TORRENT_INFO.piece_length + 1;
		leftoverBytes = TORRENT_INFO.file_length % BLOCK_LENGTH;
		blocksPerPiece = TORRENT_INFO.piece_length / BLOCK_LENGTH;
		HAVE_PIECE = new boolean[numPieces];
		double downloaded = 0;
		System.out.print("=>");
		try{
			for(int j=0; j<numPieces; j++){
				byte[] piece;
				if (j == numPieces-1){
					piece = new byte[leftoverBytes + BLOCK_LENGTH];
				} else {
					piece = new byte[TORRENT_INFO.piece_length];
				}

				for(int k=0; k<blocksPerPiece; k++){
					byte[] pieceBytes = null;

					if(j == numPieces - 1 && k == blocksPerPiece - 1){
						peer.sendRequest(j, k*BLOCK_LENGTH, leftoverBytes);
						pieceBytes = new byte[leftoverBytes];
					}
					else{
						peer.sendRequest(j, BLOCK_LENGTH*k, BLOCK_LENGTH);
						pieceBytes = new byte[BLOCK_LENGTH];
					}

					//verify the piece before we play with it
					peer.from_peer_.mark(pieceBytes.length + 13);
					byte[] toVerify = new byte[pieceBytes.length + 13];
					peer.from_peer_.readFully(toVerify);
					byte[] pieceHash = TORRENT_INFO.piece_hashes[j].array();
					Helpers.verifyHash(toVerify, pieceHash);
					peer.from_peer_.reset();

					//TODO: make sure all the headers check out
					int prefix = peer.from_peer_.readInt();
					byte id = peer.from_peer_.readByte();
					int index = peer.from_peer_.readInt();
					int begin = peer.from_peer_.readInt();

					//cop dat data
					peer.from_peer_.readFully(pieceBytes);
					System.arraycopy(pieceBytes, 0, piece, BLOCK_LENGTH*k, pieceBytes.length);
					setProgress(++downloaded, numPieces*blocksPerPiece);
				}
				file.write(piece);
				HAVE_PIECE[j] = true;
			}
		} catch (Exception e){
			System.out.println("Download failure for peer " + peer.peer_id_);
		}
	}

	/**
	 * Used for the progress bar
	 * @param completed	completed value
	 * @param total	  	total value
	 */
	public static void setProgress(double completed, double total) {
		int width = 50;
		double prog = completed/total;
		System.out.print("\r[");
		int i = 0;
		for (; i < prog*width; i++) {
			System.out.print("=");
		}
		System.out.print(">");
		for (; i < width; i++) {
			System.out.print(" ");
		}
		System.out.print("] " + prog*100 + "%");
	}

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
			Object[] responseArray = responseMap.values().toArray();

			int interval = (Integer)responseArray[0];
			Object peers = responseArray[1];

			ArrayList<Object> peerArray = (ArrayList<Object>)peers;

			for (Object peer : peerArray){
				String ip_ = "";
				String peer_id_ = "";
				int port_ = 0;

				Map<ByteBuffer, Object> peerMap = (Map<ByteBuffer, Object>)peer;

				//get all the properties
				for (Map.Entry<ByteBuffer, Object> entry : peerMap.entrySet()){          
					String key = Helpers.bufferToString(entry.getKey());
					Object value = entry.getValue();
					if (key.compareTo("ip") == 0){
						ip_ = Helpers.bufferToString((ByteBuffer)value);
					}
					if (key.compareTo("peer id") == 0){
						peer_id_ = Helpers.bufferToString((ByteBuffer)value);
					}
					if (key.compareTo("port") == 0){
						//TODO: this sometimes throws an error, for god knows why: java.nio.HeapByteBuffer cannot be cast to java.lang.Integer
						port_ = (Integer)value;
					}
				}

				Peer newPeer = new Peer(peer_id_, ip_, port_);
				peerList.add(newPeer);
			}
		} catch (Exception e){
			System.out.print(e);
		}

		return peerList;
	}

	/**
	 * Construct the url for tracker querying
	 * @param port			  port to be used
	 * @param uploaded		amount uploaded
	 * @param downloaded	amount downloaded
	 * @param left			  amount left
 	 * @param event		    event type
	 * @return				    String to be sent as query
	 */
	public static String constructQuery(int port, int uploaded, int downloaded, int left, String event){
		String url_string = "";
		try{
			String escaped_hash = Helpers.toURLHex(INFO_HASH);
			String ip = "128.6.5.130";
			url_string =  TORRENT_INFO.announce_url.toString()
			+ "?port=" + port
			+ "&peer_id=" + PEER_ID
			+ "&info_hash=" + escaped_hash 
			+ "&uploaded=" + uploaded
			+ "&downloaded=" + downloaded
			+ "&left=" + left
			+ "&ip=" +  ip
      + "&event=" + event;

		} catch (Exception e){
			System.out.println(e);
		}

		return url_string;
	}

	/**
	 * Changes the torrentfile into a bytearray
	 * @param torrentFile	the file to be read
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
		PEER_ID = peer_id_string.getBytes();
	}

	/**
	 * returns a byte[] consisting of the contents at the given url
	 * @param string_url 	url to be queried
	 * @return byte[] of contents
	 */
	public static byte[] getURL(String string_url) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			URL url = new URL(string_url);
			is = url.openStream();
			byte[] byteChunk = new byte[4096];
			int n;

			while ( (n = is.read(byteChunk)) > 0 ) {
				baos.write(byteChunk, 0, n);
			}

			is.close();
		}
		catch (IOException e) {
			System.out.println("URL failure with: " + string_url);
		}

		return baos.toByteArray();
	}
}

