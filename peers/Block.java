package peers;

import utils.*;

public class Block{
  private int piece = 0;
  private int block = 0;
	private byte[] data = null;
	
	public Block(int p, int b, byte[] d){
	  this.piece = p;
		this.block = b;
		this.data = d;
	}

  public void setData(byte[] d){
    this.data = d;
  }
	
	public int getBlockOffset(){
	  return this.block * Manager.block_length;
	}
	
	public int getPiece(){
	  return this.piece;
	}
	
	public int getBlock(){
	  return this.block;
	}
	
	public byte[] getData(){
	  return this.data;
	}
	
	@Override public boolean equals(Object other) {
     boolean result = false;
     if (other instanceof Block) {
         Block that = (Block) other;
         result = (this.getPiece() == that.getPiece() && this.getBlock() == that.getBlock()
          && this.getClass().equals(that.getClass()));
     }
     return result;
 }
 
  @Override public int hashCode() {
    return (41 * (41 + getPiece()) + getBlock());
  }
	
	//16384 or the remainder
	public int getLength(){
	  return this.data.length;
	}
	
	public void print(){
	  System.out.println("("+ this.piece + ", " + this.block + ")");
	}
}