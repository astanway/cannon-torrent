package utils;

import java.util.TimerTask;
import java.util.StringTokenizer;
import java.io.File;

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
      } else if (pieces[i] == Manager.blocksPerPiece){
        Manager.have_piece.set(i, 1);
      }
    }
    
    for(int i = 0; i < Manager.have_piece.length()){
      if(!Manager.have_piece.get(i) == 1){
        break;
      }
    }
    
    Manager.fileDone = true;
	}
}
