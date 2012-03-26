package utils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import peers.*;

public class Manager {

	private static byte[] peer_id            =	new byte[20];
	private static byte[] info_hash		    	 = new byte[20];
	private static boolean[] have_piece	  	 = null;
	private static TorrentInfo TORRENT_INFO	 = null;
	private static ConcurrentLinkedQueue<Piece> REF = null;
	private static RandomAccessFile file  	 = null;
	private static int port			        		 = 0;
	private static int numPieces			       = 0;
	private static int numBlocks			       = 0;

	private static final int block_length	= 16384;
	public static final String STARTED   = "started";
	public static final String COMPLETED = "completed";
	public static final String STOPPED   = "stopped";
	public static final String EMPTY     = "";

	public static ArrayList<Peer> peerList_  = null;

	public Manager(String torrentFile, String fileName){
		setInfo(torrentFile,fileName);
		
		numPieces = TORRENT_INFO.file_length % TORRENT_INFO.piece_length==0?
				TORRENT_INFO.file_length / TORRENT_INFO.piece_length:
				TORRENT_INFO.file_length / TORRENT_INFO.piece_length+1;
		numBlocks = TORRENT_INFO.piece_length / block_length;

    //set up the reference queue
		REF = new ConcurrentLinkedQueue<Piece>();
	  for(int i = 0; i<numPieces; i++){
	    for(int j = 0; j<numBlocks; j++){
        REF.add(new Piece(i, j));
	    }
	  }
	}

	public boolean download(){
		byte[] response = null;
		for(Peer peer : peerList_){
			DownloadThread p = new DownloadThread(peer, peer_id, info_hash, REF);
			Thread a = new Thread(p);
      a.start();
		}

		response = Helpers.getURL(constructQuery(port, 0, TORRENT_INFO.file_length, 0, STOPPED));
		System.out.println("\nFile finished.");
		return false;
	}

	public static void setInfo(String torrentFile, String savedFile){
		try{
			TORRENT_INFO = new TorrentInfo(Helpers.readTorrent(torrentFile));
			TORRENT_INFO.info_hash.get(info_hash, 0, info_hash.length);
			file = new RandomAccessFile(savedFile,"rws");
		} catch (Exception e){
			System.out.println(e);
			System.out.println("Torrent file could not be loaded.");
			System.exit(1);
		}
	}

	public static byte[] getPeerId() {
		return peer_id;
	}

	public static void setPeerId(byte[] peer_id) {
		Manager.peer_id = peer_id;
	}

	public static byte[] getInfoHash() {
		return info_hash;
	}

	public static void setInfoHash(byte[] info_hash) {
		Manager.info_hash = info_hash;
	}

	public static boolean[] getHavePiece() {
		return have_piece;
	}

	public static void setHavePiece(boolean[] have_piece) {
		Manager.have_piece = have_piece;
	}

	public static TorrentInfo getTorrentInfo() {
		return TORRENT_INFO;
	}

	public static void setTorrentInfo(TorrentInfo torrent_info) {
		Manager.TORRENT_INFO = torrent_info;
	}

	public static RandomAccessFile getFile() {
		return file;
	}

	public static void setFile(RandomAccessFile file) {
		Manager.file = file;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		Manager.port = port;
	}
	
	public static void setPeerList (ArrayList<Peer> _peerList){
		Manager.peerList_ = _peerList;
	}

	public static ArrayList<Peer> getPeerList(){
		return peerList_;
	}

	public static int getNumPieces() {
		return numPieces;
	}

	public static void setNumPieces(int numPieces) {
		Manager.numPieces = numPieces;
	}

	/**	
	 * Downloads the pieces from the peer and stores them in the appropriate file
	 * @param peer the peer object to be downloading from
	 */

  /**
   * Generates our random PeerID
   */
  public static void setPeerId(){
  	Random ran = new Random();
  	int rand_id = ran.nextInt(5555555 - 1000000 + 1) + 1000000;
  	String peer_id_string = "GROUP4AREL33t" + rand_id;
  	peer_id = peer_id_string.getBytes();
  }

	/**
	 * Construct the url for tracker querying
	 * @param port  port to be used
	 * @param uploaded  amount uploaded
	 * @param downloaded  amount downloaded
	 * @param left amount left
	 * @param event event type
	 * @return String to be sent as query
	 */
	public static String constructQuery(int port, int uploaded, int downloaded, int left, String event){
		String url_string = "";
		try{
			String escaped_hash = Helpers.toURLHex(info_hash);
			String escaped_id = Helpers.toURLHex(peer_id);
			String ip = "128.6.5.130";
			url_string =  TORRENT_INFO.announce_url.toString()
			+ "?port=" + port
			+ "&peer_id=" + escaped_id
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

}

