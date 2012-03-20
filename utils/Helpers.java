package utils;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.MessageDigest;

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
}
