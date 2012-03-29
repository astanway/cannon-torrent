package utils;

import java.io.*;
import java.util.TimerTask;
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
        Manager.have_piece.set(i, 1);
      } else if (pieces[i] == Manager.blocksPerPiece && Manager.have_piece.get(i) == 0){
        if(verify(i)){
          Manager.have_piece.set(i, 1);
          System.out.println("Piece " + i + " verified");
        } else {
          //TODO: add all the block in this piece back onto the junk.
          System.exit(1);
        }
      }
    }
    
    checkDone();
	}
	
	public boolean verify(int i){
    byte[] pieceHash = Manager.torrent_info.piece_hashes[i].array();
    byte[] piece = new byte[Manager.blocksPerPiece * Manager.block_length];
    byte[] block = new byte[Manager.block_length];
    
    File dir = new File("blocks");
    for(File file : dir.listFiles()) {
      StringTokenizer st = new StringTokenizer(file.getName());
      int p = Integer.parseInt(st.nextToken());
      if(p == i){
        int b = Integer.parseInt(st.nextToken());
        try{
          RandomAccessFile r = new RandomAccessFile("blocks/" + file.getName(), "r");
          FileChannel fc = r.getChannel();
          FileLock fileLock = null;
          
          try{
            fileLock = fc.tryLock(0L, Long.MAX_VALUE, true);
          } catch (Exception e){
            System.out.print(e);
          }
          
          if (fileLock != null){
            r.read(block);
            System.arraycopy(block, 0, piece, Manager.block_length*b, block.length);
          } else {
           System.out.println("File is locked"); 
          }
        } catch (Exception e) {
          System.out.println("Couldn't read file. This should never happen.");
          System.out.print(e);
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
	
	public void checkDone(){
	  for(int i = 0; i < Manager.have_piece.length(); i++){
      if(!(Manager.have_piece.get(i) == 1)){
        return;
      }
    }

    Manager.fileDone = true;
	}
}
