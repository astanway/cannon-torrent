package peers;

import utils.*;

/**
 * Block class used for queue to see what we should download
 * 
 */
public class Block {
	private int piece = 0;
	private int block = 0;
	private byte[] data = null;

	public Block(int p, int b, byte[] d) {
		this.piece = p;
		this.block = b;
		this.data = d;
	}

	/**
	 * 
	 * @param d
	 *            data to be set
	 */
	public void setData(byte[] d) {
		this.data = d;
	}

	/**
	 * 
	 * @return block offset
	 */
	public int getBlockOffset() {
		return this.block * Manager.block_length;
	}

	/**
	 * 
	 * @return piece number
	 */
	public int getPiece() {
		return this.piece;
	}

	/**
	 * 
	 * @return block number
	 */
	public int getBlock() {
		return this.block;
	}

	/**
	 * 
	 * @return data in block
	 */
	public byte[] getData() {
		return this.data;
	}

	/**
	 * equals method for sorting
	 */
	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof Block) {
			Block that = (Block) other;
			result = (this.getPiece() == that.getPiece()
					&& this.getBlock() == that.getBlock() && this.getClass()
					.equals(that.getClass()));
		}
		return result;
	}

	/**
	 * hash method override
	 */
	@Override
	public int hashCode() {
		return (41 * (41 + getPiece()) + getBlock());
	}

	// 16384 or the remainder
	/**
	 * 
	 * @return length of data
	 */
	public int getLength() {
		return this.data.length;
	}

	/**
	 * print method for block
	 */
	public void print() {
		// System.out.println("(" + this.piece + ", " + this.block + ")");
	}
}