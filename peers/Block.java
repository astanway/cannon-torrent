package peers;

import utils.*;

public class Block{
  private int piece = 0;
  private int block = 0;
	private byte[] data = null;
	private boolean isFilled = false;
	
	public Block(int p, int b, byte[] d){
	  this.piece = p;
		this.block = b;
		this.data = d;
	}

  public void setData(byte[] d){
    this.data = d;
    this.isFilled = true;
  }
	
	public int getBlockIndex(){
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
	
	//16384 or the remainder
	public int getLength(){
	  return this.data.length;
	}
	
	public boolean isFilled(){
	  return this.isFilled;
	}
	
	public void print(){
	  System.out.println("("+ this.piece + ", " + this.block + ", " + this.isFilled + ")");
	}
}