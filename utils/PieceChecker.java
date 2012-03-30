package utils;

import java.io.*;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.StringTokenizer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class PieceChecker extends TimerTask{

	public void run(){
	  int[] pieces = new int[Manager.numPieces];
	  File dir = new File("blocks");
    File files[] = dir.listFiles();
    for (File f : files) {
      StringTokenizer st = new StringTokenizer(f.getName());
      int piece = Integer.parseInt(st.nextToken());
      pieces[piece]++;
    }
    
    for(int i = 0; i < pieces.length; i++){
      if(i == pieces.length - 1 && pieces[i] == Manager.blocksInLastPiece){
        if(verify(i, true)){
          Manager.have_piece.set(i, 1);
          System.out.println("Last piece verified.");
        } else {
          deleteBlocks(i);
          //TODO: add all the block in this piece back onto the junk.
          System.exit(1);
        }
      } else if (pieces[i] == Manager.blocksPerPiece && Manager.have_piece.get(i) == 0){
        if(verify(i, false)){
          Manager.have_piece.set(i, 1);
          System.out.println("Piece " + i + " verified");
        } else {
          deleteBlocks(i);
          //TODO: add all the block in this piece back onto the junk.
          System.exit(1);
        }
      }
    }

    finish();
	}
	
	public boolean verify(int i, boolean last){
    byte[] pieceHash = Manager.torrent_info.piece_hashes[i].array();
    byte[] piece = new byte[Manager.blocksPerPiece * Manager.block_length];
    byte[] block = new byte[Manager.block_length];

    //is it the last piece?
    if(last){
      int lastPieceSize = ((Manager.blocksInLastPiece - 1) * Manager.block_length) + Manager.leftoverBytes;
      piece = new byte[lastPieceSize];
    } else {
      piece = new byte[Manager.blocksPerPiece * Manager.block_length];
    }
    
    //get only the blocks in piece i
    File dir = new File("blocks");
    for(File file : dir.listFiles()) {
      StringTokenizer st = new StringTokenizer(file.getName());
      int p = Integer.parseInt(st.nextToken());
      if(p == i){
        int b = Integer.parseInt(st.nextToken());
        
        if(last == true && b == Manager.blocksInLastPiece - 1){
          block = new byte[Manager.leftoverBytes];
        }

        try{
          RandomAccessFile r = new RandomAccessFile("blocks/" + file.getName(), "r");
          r.read(block);
          System.arraycopy(block, 0, piece, Manager.block_length*b, block.length);
        } catch (Exception e) {
          System.out.println("Couldn't read file. This should never happen.");
          e.printStackTrace();
          System.exit(1);
        }
      }
    }

    if(!Helpers.verifyHash(piece, pieceHash)){
      System.out.println("Verification failed at " + i);
      return false;
    }
    
    return true;
	}
	
	public static void finish(){	  
	  for(int i = 0; i < Manager.have_piece.length(); i++){
      if(!(Manager.have_piece.get(i) == 1)){
        System.out.println("Not finished yet.");
        return;
      }
    }
	  
	  System.out.println("Commencing file write...");
    byte[] piece = null;
    byte[] block = null;
        
    FileOutputStream out = null;
    try{
      out = new FileOutputStream(Manager.file);
    } catch (Exception e){
      System.out.print(e);
    }

    //get rid of the spaces so we can make numbers out of the names
    File dir = new File("blocks");
    Integer[] names = new Integer[Manager.numBlocks];
    int x = 0;
    for(File file : dir.listFiles()) {
      StringTokenizer st = new StringTokenizer(file.getName());
      String newName = st.nextToken() + st.nextToken();
      int n = Integer.parseInt(newName);
      System.out.println(n);
      names[x++] = n;
    }
    
    Arrays.sort(names);
    //sort the bastards
    // AlphanumComparator comparator = new AlphanumComparator();
    // Collections.sort(names, comparator);

    //write 'em in the correct order
    for(Integer name : names) {
      String n = name.toString();
      File file = new File("blocks/" + n);
      int p = Integer.parseInt(file.getName());
      System.out.println(p);
      try{
        byte[] fileBytes = Helpers.getBytesFromFile(file);
        out.write(fileBytes);
      } catch (Exception e){
        System.out.print(e);
      }
    }
    
    try{
      out.close();
    } catch (Exception e){
      System.out.print(e);
    }
    
    //tell the tracker we're done
	  byte[] response = null;
	  response = Helpers.getURL(Manager.constructQuery(Manager.port, 0, 0, Manager.torrent_info.file_length, Manager.COMPLETED));
    response = Helpers.getURL(Manager.constructQuery(Manager.port, 0, 0, Manager.torrent_info.file_length, Manager.STOPPED));
    System.out.println("Bye!");
    System.exit(1);
	}
	
	public static void deleteBlocks(int i){
	  
	}
}
