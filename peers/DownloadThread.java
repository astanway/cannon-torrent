package peers;

import java.io.*;
import java.util.*;

import utils.*;

public class DownloadThread implements Runnable {
  
  public Peer peer = null;
  
  public DownloadThread(Peer _peer){
    peer = _peer;
  }
  
  public void run() {
    peer.createSocket(peer.ip_, peer.port_);
    peer.establishStreams();
    peer.sendHandshake(Manager.peer_id, Manager.info_hash);

    if(peer.receiveHandshake(Manager.info_hash)){
      peer.sendMessage(Peer.INTERESTED);
        
      while(true){ if(peer.listenForUnchoke()){ break; }}
      
      // while(!Manager.q.isEmpty()){
        Block p = Manager.q.poll();
        System.out.println(Manager.q.size());
        p.print();
        downloadBlock(p);
        // System.out.println(peer.peer_id_ + " " + q.size());
        // Piece.print();
      // }
    //   response = Helpers.getURL(constructQuery(PORT, TORRENT_INFO.file_length, 0, 0, STARTED));
    //
    // downloadPieces(peer);
    //     
    //   response = Helpers.getURL(constructQuery(PORT, 0, TORRENT_INFO.file_length, 0, COMPLETED));
    //     
      peer.closeSocket();
    }
  }
  
  
  public void downloadBlock(Block b){
    int p = b.getPiece(); 
	  int i = b.getBlockIndex();
	  int l = b.getLength();
	  byte[] data = b.getData();

    try{
      peer.sendRequest(b);

      //verify the data
      peer.from_peer_.mark(l + 13);
			byte[] toVerify = new byte[l + 13];
			peer.from_peer_.readFully(toVerify);
			byte[] pieceHash = Manager.torrent_info.piece_hashes[p].array();
			Helpers.verifyHash(toVerify, pieceHash);
			peer.from_peer_.reset();
			
			System.out.println("verified");
      

			//TODO: make sure all the headers check out
			int prefix = peer.from_peer_.readInt();
			byte id = peer.from_peer_.readByte();
			int index = peer.from_peer_.readInt();
			int begin = peer.from_peer_.readInt();

			//cop dat data
			peer.from_peer_.readFully(data);
			
			String name = p + " " + b.getBlock();
			RandomAccessFile file = new RandomAccessFile(name,"rws");
			file.write(data);
			file.close();
      // System.arraycopy(data, 0, piece, b, l);
      System.out.println("GOT A BLOCK!");
    } catch (Exception e){
      System.out.println(peer.peer_id_ + " " + e);
    }
  }
  
  
  /*
  public static void downloadPieces(Piece p){
		int numPieces        = 0;
		int numLeft          = 0;
		int leftoverBytes    = 0;
		int blocksPerPiece   = 0;

		numLeft = numPieces = TORRENT_INFO.file_length / TORRENT_INFO.piece_length + 1;
		leftoverBytes = TORRENT_INFO.file_length % block_length;
		blocksPerPiece = TORRENT_INFO.piece_length / block_length;
		have_piece = new boolean[numPieces];
		double downloaded = 0;
		System.out.print("=>");
		try{
			for(int j=0; j<numPieces; j++){
				byte[] piece;
				if (j == numPieces-1){
					piece = new byte[leftoverBytes + block_length];
				} else {
					piece = new byte[TORRENT_INFO.piece_length];
				}

				for(int k=0; k<blocksPerPiece; k++){
					byte[] pieceBytes = null;

					if(j == numPieces - 1 && k == blocksPerPiece - 1){
						peer.sendRequest(j, k*block_length, leftoverBytes);
						pieceBytes = new byte[leftoverBytes];
					}
					else{
						peer.sendRequest(j, block_length*k, block_length);
						pieceBytes = new byte[block_length];
					}

					//verify the piece before we play with it
					peer.from_peer_.mark(pieceBytes.length + 13);
					byte[] toVerify = new byte[pieceBytes.length + 13];
					peer.from_peer_.readFully(toVerify);
					byte[] pieceHash = TORRENT_INFO.piece_hashes[j].array();
					Helpers.verifyHash(toVerify, pieceHash);
					peer.from_peer_.reset();

					//TODO: make sure all the headers check out
					int prefix = peer.from_peer_.readInt();
					byte id = peer.from_peer_.readByte();
					int index = peer.from_peer_.readInt();
					int begin = peer.from_peer_.readInt();

					//cop dat data
					peer.from_peer_.readFully(pieceBytes);
					System.arraycopy(pieceBytes, 0, piece, block_length*k, pieceBytes.length);
					Helpers.setProgress(++downloaded, numPieces*blocksPerPiece);
				}

				file.write(piece);
				have_piece[j] = true;
			}
		} catch (Exception e){
			System.out.println("Download failure for peer " + peer.peer_id_);
		}
	}*/
	
}
