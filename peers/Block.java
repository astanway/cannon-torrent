package peers;

public class Block{
  private static int piece = 0;
  private static int block = 0;
	private static byte[] data = null;
	private static boolean isFilled = false;
	
	public Block(int p, int b, byte[] d){
	  piece = p;
		block = b;
		data = d;
	}

  public static void setData(byte[] d){
    data = d;
    isFilled = true;
  }

	public static void print(){
	  System.out.println(piece + " " + block);
	}
	
	public static int getPiece(){
	  return piece;
	}
	
	public static int getBlock(){
	  return block;
	}
	
	//16384 or the remainder
	public static int getLength(){
	  return data.length;
	}
	
	public boolean isFilled(){
	  return isFilled;
	}
}
