
import java.io.*;
import java.util.*;

public class CannonClient {
	
	public static void main(String[] args) {
    
    String torrentFile = args[0];
    String savedFile = args[1];
    readTorrent(torrentFile);
	}
	
	public static void readTorrent(String torrentFile){
	  FileInputStream fin;
		try{
		    fin = new FileInputStream (torrentFile);
		    System.out.println( new DataInputStream(fin).readLine() );
		    fin.close();		
		}

		catch (IOException e){
			System.err.println ("Unable to read from file");
			System.exit(-1);
		}
	}
}

