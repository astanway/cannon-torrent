package peers;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Piece{
	
	public static int piece;
	public static ConcurrentLinkedQueue<Block> blocks = null;
	public static byte[] data;
	
	public Piece(int p, byte[] d){
		piece = p;
		data = d;
		blocks = new ConcurrentLinkedQueue<Block>(); 
	}

  public static void setData(byte[] d){
    data = d;
  }
  
  public static void addBlock(Block b){
    blocks.add(b);
  }

	public static void print(){
	  System.out.println(piece);
	}
}
