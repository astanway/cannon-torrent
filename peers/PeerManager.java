package peers;

import java.io.*;
import java.util.*;
import utils.*;

public class PeerManager {

	public static byte[] PEER_ID             = new byte[20];
	public static byte[] INFO_HASH           = new byte[20];
	public static int BLOCK_LENGTH           = 16384;
	public static boolean[] HAVE_PIECE       = null;
	public static TorrentInfo TORRENT_INFO   = null;
	public static RandomAccessFile file      = null;

	
	public static ArrayList<Peer> peerList_  = null;
	
	public PeerManager(){
	  
	}

  public PeerManager(ArrayList<Peer> _peerList){
		this.peerList_ = _peerList;
	}
	
	public void setPeerList(ArrayList<Peer> _peerList){
	  this.peerList_ = _peerList;
	}
	

  public boolean download(){
    // for(Peer peer : peerList){
    //  System.out.println("Peer Found");
    //  peer.createSocket(peer.ip_, peer.port_);
    //  peer.establishStreams();
    //  peer.sendHandshake(PEER_ID, INFO_HASH);
    //  if(peer.receiveHandshake(INFO_HASH)){
    // 
    //    peer.sendMessage(Peer.INTERESTED);
    // 
    //    while(true){ if(peer.listenForUnchoke()){ break; }}
    // 
    //    response = getURL(constructQuery(i, TORRENT_INFO.file_length, 0, 0, STARTED));
    // 
    //    downloadPieces(peer);
    // 
    //    response = getURL(constructQuery(i, 0, TORRENT_INFO.file_length, 0, COMPLETED));
    // 
    //    peer.closeSocket();
    //  }
    // }
    // 
    // response = getURL(constructQuery(i, 0, TORRENT_INFO.file_length, 0, STOPPED));
    // System.out.println("\nFile finished.");
    return false;
  }
  
  /**	
	 * Downloads the pieces from the peer and stores them in the appropriate file
	 * @param peer the peer object to be downloading from
	 */
	public static void downloadPieces(Peer peer){
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
   * Construct the url for tracker querying
   * @param port                    port to be used
   * @param uploaded              amount uploaded
   * @param downloaded    amount downloaded
   * @param left                    amount left
   * @param event             event type
   * @return                                  String to be sent as query
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
  
	@SuppressWarnings("rawtypes")
	public static ArrayList getPeerList(){
		return peerList_;
	}	
}

