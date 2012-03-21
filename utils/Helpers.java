package utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import peers.Peer;

public final class Helpers 
{
	/**
	 * Method to verify the hash for a piece
	 * @param piece			the piece number
	 * @param pieceHash		the hash for the piece from the torrentinfo
	 */
	public static void verifyHash(byte[] piece, byte[] pieceHash){
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			if (Arrays.equals(digest.digest(piece), pieceHash)){
				throw new Exception ("Piece hash does not match. Exiting now, because we don't fucks around.");
			}
		} catch (Exception e){
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
  
  public static final ByteBuffer intervalKey = ByteBuffer.wrap(new byte[]{'i','n','t','e','r','v','a','l'});
	public static final ByteBuffer peersKey = ByteBuffer.wrap(new byte[]{'p','e','e','r','s'});
	public static final ByteBuffer minIntervalKey = ByteBuffer.wrap(new byte[]{'m','i','n',' ','i','n','t','e','r','v','a','l'});
	public static final ByteBuffer downloadedKey = ByteBuffer.wrap(new byte[]{'d','o','w','n','l','o','a','d','e','d'});
	public static final ByteBuffer completeKey = ByteBuffer.wrap(new byte[]{'c','o','m','p','l','e','t','e'});
	public static final ByteBuffer ipKey = ByteBuffer.wrap(new byte[]{'i','p'});
	public static final ByteBuffer peerIdKey = ByteBuffer.wrap(new byte[]{'p','e','e','r',' ','i','d'});
	public static final ByteBuffer portKey = ByteBuffer.wrap(new byte[]{'p','o','r','t'});

	/**
	 * Gets the peer list from a response from the tracker
	 * @param response	byte array response from 
	 * @return			    returns the array list of peers
	 */
	public static ArrayList<Peer> getPeerList(byte[] response){	  
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
        // System.out.println(ip_ +" " +  peer_id_ +" " +  port_);
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


	/**
	 * 	changes a bytebuffer into a string
	 * @param buffer	buffer to be turned into a string
	 * @return			String from bytebuffer
	 */
	public static String bufferToString(ByteBuffer buffer)
	{
		byte[] bufferBytes = new byte[buffer.capacity()];
		buffer.get(bufferBytes, 0, bufferBytes.length);
		String value = new String(bufferBytes);
		return value;
	}

	/**
	 * 	changes a bytebuffer into an int
	 * @param buffer	buffer to be changed
	 * @return			the int from the buffer
	 */
	public static int bufferToInt(ByteBuffer buffer)
	{
		byte[] bufferBytes = new byte[buffer.capacity()];
		buffer.get(bufferBytes, 0, bufferBytes.length);
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (bufferBytes[i] & 0x000000FF) << shift;
		}
		return value;
	}

	/**
	 * casts the bytes to a string and prints them
	 * @param bytes	bytes to be printed out
	 */
	public static void printBytes(byte[] bytes){
		String value = new String(bytes);
		System.out.print(value);
	}

	public static final char[] HEX_CHARS = 
	{'0','1','2','3','4','5','6','7',
		'8','9','A','B','C','D','E','F'};

	/**
	 * Changes a byte array to hexidecimal characters
	 * @param bytes	the byte buffer to changed
	 * @return	the string of hex to be sent
	 */
	public static String toURLHex(byte[] bytes){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < bytes.length; ++i){
			sb.append('%')
			.append(HEX_CHARS[(bytes[i]>>4&0x0F)])
			.append(HEX_CHARS[(bytes[i]&0x0F)]);
		}
		return sb.toString();
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
}
