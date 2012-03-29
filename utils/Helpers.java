package utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import peers.Peer;

public final class Helpers 
{
  public static void printBoolArray(boolean[] arr){
    for(int i = 0; i < arr.length; i++){
      if(arr[i] == false){
        System.out.print("0");
      } else {
        System.out.print("1");
      }
    }
    System.out.println("");
  }
  
	/**
	 * Method to verify the hash for a piece
	 * @param piece			the piece number
	 * @param pieceHash		the hash for the piece from the torrentinfo
	 * @return boolean if its verified or not
	 */
	public static boolean verifyHash(byte[] piece, byte[] pieceHash){
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			if (!Arrays.equals(digest.digest(piece), pieceHash)){
				throw new Exception ("Piece hash does not match. Exiting now, because we don't fucks around.");
			}
		} catch (Exception e){
		  return false;
		}
		return true;
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
	 * Read in bytes from a file
	 * @param file	file
	 * @return byte[]	file bytes
	 * Taken from http://www.exampledepot.com/egs/java.io/file2bytearray.html
	 */
	public static byte[] getBytesFromFile(File file) throws IOException {
	  
    // FileInputStream in = new FileInputStream(file);
    //     try {
    //         java.nio.channels.FileLock lock = in.getChannel().tryLock();
    //         try {
    //             Reader reader = new InputStreamReader(in, charset);
    //             ...
    //         } finally {
    //             lock.release();
    //         }
    //     } finally {
    //         in.close();
    //     }
    
    
      InputStream is = new FileInputStream(file);
      long length = file.length();
      
      byte[] bytes = new byte[(int)length];

      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length
             && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
          offset += numRead;
      }

      if (offset < bytes.length) {
          throw new IOException("Could not completely read file " + file.getName());
      }
      
      is.close();
      return bytes;
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
