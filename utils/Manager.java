package utils;

import java.io.*;
import java.util.*;

import peers.Peer;
import peers.SocketThread;

public class Manager {

	private static byte[] peer_id=			 new byte[20];
	private static byte[] info_hash			= new byte[20];
	private static boolean[] have_piece		= null;
	private static TorrentInfo torrent_info	= null;
	private static int[][] reference		= null;
	private static RandomAccessFile file	= null;
	private static int port					= 0;
	private static int numPieces			= 0;
	private static int numBlocks			= 0;

	private static final int block_length	= 16384;
	public static final String STARTED   = "started";
	public static final String COMPLETED = "completed";
	public static final String STOPPED   = "stopped";
	public static final String EMPTY     = "";

	public static ArrayList<Peer> peerList_  = null;

	public Manager(String torrentFile, String fileName){
		setInfo(torrentFile,fileName);
		numPieces = torrent_info.file_length%torrent_info.piece_length==0?
				torrent_info.file_length/torrent_info.piece_length:
				torrent_info.file_length/torrent_info.piece_length+1;
		numBlocks = torrent_info.piece_length/block_length;
	}




	/*public boolean download(){

		byte[] response = null;
		for(Peer peer : peerList_){
			SocketThread p = new SocketThread(peer, peer_id, info_hash);
			Thread a = new Thread(p);
			a.start();
		}

		response = Helpers.getURL(constructQuery(port, 0, torrent_info.file_length, 0, STOPPED));
		System.out.println("\nFile finished.");
		return false;
	}

	/**	
	 * Downloads the pieces from the peer and stores them in the appropriate file
	 * @param peer the peer object to be downloading from
	 */
	/*public static void downloadPieces(Peer peer){
		int numPieces        = 0;
		int numLeft          = 0;
		int leftoverBytes    = 0;
		int blocksPerPiece   = 0;

		numLeft = numPieces = torrent_info.file_length / torrent_info.piece_length + 1;
		leftoverBytes = torrent_info.file_length % block_length;
		blocksPerPiece = torrent_info.piece_length / block_length;
		have_piece = new boolean[numPieces];
		double downloaded = 0;
		System.out.print("=>");
		try{
			for(int j=0; j<numPieces; j++){
				byte[] piece;
				if (j == numPieces-1){
					piece = new byte[leftoverBytes + block_length];
				} else {
					piece = new byte[torrent_info.piece_length];
				}

				for(int k=0; k<blocksPerPiece; k++){
					byte[] pieceBytes = null;

					if(j == numPieces - 1 && k == blocksPerPiece - 1){
						peer.sendRequest(j, k*block_length, leftoverBytes);
						pieceBytes = new byte[leftoverBytes];
					}
					else{
						peer.sendRequest(j, block_length*k, block_length);
						pieceBytes = new byte[block_length];
					}

					//verify the piece before we play with it
					peer.from_peer_.mark(pieceBytes.length + 13);
					byte[] toVerify = new byte[pieceBytes.length + 13];
					peer.from_peer_.readFully(toVerify);
					byte[] pieceHash = torrent_info.piece_hashes[j].array();
					Helpers.verifyHash(toVerify, pieceHash);
					peer.from_peer_.reset();

					//TODO: make sure all the headers check out
					int prefix = peer.from_peer_.readInt();
					byte id = peer.from_peer_.readByte();
					int index = peer.from_peer_.readInt();
					int begin = peer.from_peer_.readInt();

					//cop dat data
					peer.from_peer_.readFully(pieceBytes);
					System.arraycopy(pieceBytes, 0, piece, block_length*k, pieceBytes.length);
					Helpers.setProgress(++downloaded, numPieces*blocksPerPiece);
				}

				file.write(piece);
				have_piece[j] = true;
			}
		} catch (Exception e){
			System.out.println("Download failure for peer " + peer.peer_id_);
		}
	}*/

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
			url_string =  torrent_info.announce_url.toString()
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

	public static void setInfo(String torrentFile, String savedFile){
		try{
			torrent_info = new TorrentInfo(Helpers.readTorrent(torrentFile));
			torrent_info.info_hash.get(info_hash, 0, info_hash.length);
			file = new RandomAccessFile(savedFile,"rws");
		} catch (Exception e){
			System.out.println(e);
			System.out.println("Torrent file could not be loaded.");
			System.exit(1);
		}
	}

	/**
	 * Generates our random PeerID
	 */
	public static void setPeerId(){
		Random ran = new Random();
		int rand_id = ran.nextInt(5555555 - 1000000 + 1) + 1000000;
		String peer_id_string = "GROUP4AREL33t" + rand_id;
		peer_id = peer_id_string.getBytes();
	}

	public static byte[] getPeer_id() {
		return peer_id;
	}

	public static void setPeer_id(byte[] peer_id) {
		Manager.peer_id = peer_id;
	}

	public static byte[] getInfo_hash() {
		return info_hash;
	}

	public static void setInfo_hash(byte[] info_hash) {
		Manager.info_hash = info_hash;
	}

	public static boolean[] getHave_piece() {
		return have_piece;
	}

	public static void setHave_piece(boolean[] have_piece) {
		Manager.have_piece = have_piece;
	}

	public static TorrentInfo getTorrent_info() {
		return torrent_info;
	}

	public static void setTorrent_info(TorrentInfo torrent_info) {
		Manager.torrent_info = torrent_info;
	}

	public static int[][] getReference() {
		return reference;
	}

	public static void setReference(int[][] reference) {
		Manager.reference = reference;
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


}

