package utils;

import java.nio.ByteBuffer;


/*
 * Used to interface with the  tracker and use the torrent info
 * that is received
 */

public class Tracker {
	private TorrentInfo torrentInfo = null;
	private int numPieces;
	private int fileLength;
	private int pieceLength;
	private ByteBuffer[] pieceHashes;
	private String fileName;
	
	
	
	
	public void setTorrentInfo(String torrentFile){
		try{
			torrentInfo = new TorrentInfo(Helpers.readTorrent(torrentFile));
			fileLength = torrentInfo.file_length;
			pieceLength = torrentInfo.piece_length;
			numPieces = fileLength%pieceLength == 0?fileLength/pieceLength 
					: fileLength/pieceLength+1;
			pieceHashes = torrentInfo.piece_hashes;
		}catch(Exception e){
			e.printStackTrace();
		}
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
			String escaped_hash = Helpers.toURLHex(RUBTClient);
			String escaped_id = Helpers.toURLHex(PEER_ID);
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
