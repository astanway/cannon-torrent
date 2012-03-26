package peers;

public class Block{

  private static int block = 0;
	private static byte[] data = null;
	private static boolean isFilled = false;
	
	public Block(int b, byte[] d){
		block = b;
		data = d;
	}

  public static void setData(byte[] d){
    data = d;
    isFilled = true;
  }

	public static void print(){
	  System.out.println(block);
	}
	
	public boolean isFilled(){
	  return isFilled;
	}
}
