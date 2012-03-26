package peers;

public class Piece{
	
	public static int piece;
	public static int block;
	
	public Piece(int p, int b){
		piece = p;
		block = b;
	}

	public static void print(){
	  System.out.println(piece + " " + block);
	}
}
