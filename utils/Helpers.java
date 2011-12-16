package utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import peers.Peer;
import peers.Block;

public final class Helpers {
	public static void printBoolArray(boolean[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == false) {
				System.out.print("0");
			} else {
				System.out.print("1");
			}
		}
		System.out.println("");
	}

	/**
	 * Method to verify the hash for a piece
	 * 
	 * @param piece
	 *            the piece number
	 * @param pieceHash
	 *            the hash for the piece from the torrentinfo
	 * @return boolean if its verified or not
	 */
	public static boolean verifyHash(byte[] piece, byte[] pieceHash) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			if (!Arrays.equals(digest.digest(piece), pieceHash)) {
				throw new Exception("Piece hash does not match.");
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Changes the torrentfile into a bytearray
	 * 
	 * @param torrentFile
	 *            the file to be read
	 * @return byte array from the file
	 */
	public static byte[] readTorrent(String torrentFile) {
		try {
			RandomAccessFile rFile = new RandomAccessFile(torrentFile, "rw");
			byte[] fileBytes = new byte[(int) rFile.length()];
			rFile.read(fileBytes);
			return fileBytes;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * returns a byte[] consisting of the contents at the given url
	 * 
	 * @param string_url
	 *            url to be queried
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

			while ((n = is.read(byteChunk)) > 0) {
				baos.write(byteChunk, 0, n);
			}

			is.close();
		} catch (IOException e) {
			System.out.println("URL failure with: " + string_url);
		}

		return baos.toByteArray();
	}

	/**
	 * changes a bytebuffer into a string
	 * 
	 * @param buffer
	 *            buffer to be turned into a string
	 * @return String from bytebuffer
	 */
	public static String bufferToString(ByteBuffer buffer) {
		byte[] bufferBytes = new byte[buffer.capacity()];
		buffer.get(bufferBytes, 0, bufferBytes.length);
		String value = new String(bufferBytes);
		return value;
	}

	/**
	 * changes a bytebuffer into an int
	 * 
	 * @param buffer
	 *            buffer to be changed
	 * @return the int from the buffer
	 */
	public static int bufferToInt(ByteBuffer buffer) {
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
	 * 
	 * @param bytes
	 *            bytes to be printed out
	 */
	public static void printBytes(byte[] bytes) {
		String value = new String(bytes);
		System.out.print(value);
	}

	public static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * Changes a byte array to hexidecimal characters
	 * 
	 * @param bytes
	 *            the byte buffer to changed
	 * @return the string of hex to be sent
	 */
	public static String toURLHex(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; ++i) {
			sb.append('%').append(HEX_CHARS[(bytes[i] >> 4 & 0x0F)])
					.append(HEX_CHARS[(bytes[i] & 0x0F)]);
		}
		return sb.toString();
	}

	/**
	 * Returns the piece in a byte array if we have it, returns null otherwise
	 * 
	 * @param i
	 *            the zero based piece index
	 * @return the piece, or null
	 */
	public static byte[] getPiece(int i) {
		byte[] piece = new byte[Manager.blocksPerPiece * Manager.block_length];
		byte[] block = new byte[Manager.block_length];
		int check = 0;
		boolean last = false;

		// is it the last piece?
		if (i == Manager.numPieces - 1) {
			last = true;
			int lastPieceSize = ((Manager.blocksInLastPiece - 1) * Manager.block_length)
					+ Manager.leftoverBytes;
			piece = new byte[lastPieceSize];
		} else {
			piece = new byte[Manager.blocksPerPiece * Manager.block_length];
		}

		// get only the blocks in piece i
		File dir = new File("blocks");
		for (File file : dir.listFiles()) {
			StringTokenizer st = new StringTokenizer(file.getName());
			int p = Integer.parseInt(st.nextToken());
			if (p == i) {
				check++;
				int b = Integer.parseInt(st.nextToken());

				if ((i == Manager.numPieces - 1)
						&& b == Manager.blocksInLastPiece - 1) {
					block = new byte[Manager.leftoverBytes];
				}

				try {
					RandomAccessFile r = new RandomAccessFile("blocks/"
							+ file.getName(), "r");
					r.read(block);
					System.arraycopy(block, 0, piece, Manager.block_length * b,
							block.length);
					r.close();
				} catch (Exception e) {
					System.out
							.println("Couldn't read file. This should never happen.");
					e.printStackTrace();
					System.exit(1);
				}
			}
		}

		if (last) {
			if (check == Manager.blocksInLastPiece) {
				return piece;
			} else {
				return null;
			}
		} else if (check == Manager.blocksPerPiece) {
			return piece;
		}

		return null;
	}

	// adds deleted piece to manager as well
	public static void deletePiece(int i) {
		File dir = new File("blocks");
		byte[] data = null;
		for (File file : dir.listFiles()) {
			StringTokenizer st = new StringTokenizer(file.getName());
			int p = Integer.parseInt(st.nextToken());
			if (p == i) {
				file.delete();
				int b = Integer.parseInt(st.nextToken());

				if (i == Manager.numPieces - 1
						&& b == Manager.blocksInLastPiece) {
					data = new byte[Manager.leftoverBytes];
				} else {
					data = new byte[Manager.block_length];
				}

				Block block = new Block(p, b, data);
				Manager.q.add(block);
			}
		}
	}

	/**
	 * Read in bytes from a file
	 * 
	 * @param file
	 *            file
	 * @return byte[] file bytes Taken from
	 *         http://www.exampledepot.com/egs/java.io/file2bytearray.html
	 */
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();

		byte[] bytes = new byte[(int) length];

		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		is.close();
		return bytes;
	}

	/**
	 * Used for the progress bar
	 * 
	 * @param completed
	 *            completed value
	 * @param total
	 *            total value
	 */
	public static void setProgress(double completed, double total) {
		int width = 50;
		double prog = completed / total;
		System.out.print("\r[");
		int i = 0;
		for (; i < prog * width; i++) {
			System.out.print("=");
		}
		System.out.print(">");
		for (; i < width; i++) {
			System.out.print(" ");
		}
		System.out.print("] " + prog * 100 + "%");
	}

	/**
	 * @param data byte[] to be turned into a hex string
	 * @return A hex string of the bytes as two hex characters
	 */
	public static String converToHex(byte[] data) {
		StringBuffer sb = new StringBuffer();
		for (byte b : data) {
			sb.append(Integer.toHexString(b & 0xFF));
		}
		return sb.toString();
	}
}
