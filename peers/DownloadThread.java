package peers;

import java.io.*;
import java.util.*;

import utils.*;
import utils.Message.*;

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
      Message m = peer.listen();
      byte[] bf = null;
      
      while(m.getId() == Message.TYPE_KEEP_ALIVE){
        m = peer.listen();
      }
      
      //only download if we get a bitfield
      if(m.getId() == Message.TYPE_BITFIELD){
        BitfieldMessage bfm = (BitfieldMessage) m;
        bf = bfm.getData();
        boolean[] bfb = BitToBoolean.convert(bf);
        
        //need have array

        //send bitfield

        //if we're interested:
        while(!Manager.q.isEmpty()){
          Block p = Manager.q.poll();

          if(bfb[p.getPiece()] == true && Manager.have_piece.get(p.getPiece()) == 0){
            peer.sendMessage(Peer.INTERESTED);
            if(peer.choked == false){
              downloadBlock(p);
            } else {
              m = peer.listen();
              if(m.getId() == Message.TYPE_UNCHOKE){
                System.out.println("Peer " + peer.peer_id_ + " unchoked us");
                downloadBlock(p);
                peer.choked = false;
              }
            }
          }


            // while(true){ if(peer.listenForUnchoke()){ break; }}


            //listen
            //if we have a full piece, broadcast
        }
        System.out.println("done.");
      }
      
      System.out.println("q empty");
      //   response = Helpers.getURL(constructQuery(PORT, TORRENT_INFO.file_length, 0, 0, STARTED));
      //
      // downloadPieces(peer);
      //     
      //   response = Helpers.getURL(constructQuery(PORT, 0, TORRENT_INFO.file_length, 0, COMPLETED));
      //
      // peer.closeSocket();
    }
  }
  
  
  public void downloadBlock(Block b){
    int p = b.getPiece(); 
	  int i = b.getBlockIndex();
	  int l = b.getLength();
	  byte[] data = b.getData();

    try{
      peer.sendRequest(b);
      Message m = peer.listen();
      
      if (m == null){
        Manager.q.add(b);
        return;
      } else if(m.getId() == Message.TYPE_UNCHOKE){
        System.out.println("Peer " + peer.peer_id_ + " unchoked us");
        Manager.q.add(b);
        peer.choked = false;
        return;
      } else if (m.getId() == Message.TYPE_CHOKE){
        System.out.println("Peer " + peer.peer_id_ + " choked us");
        peer.choked = true;
        Manager.q.add(b);
        return;
      } else if (m.getId() == Message.TYPE_PIECE){
        PieceMessage pm = (PieceMessage) m; 
        byte[] piece_data = pm.getData();
        

        //         //verify the data
        //         peer.from_peer_.mark(l + 13);
        // byte[] toVerify = new byte[l + 13];
        // peer.from_peer_.readFully(toVerify);
        // byte[] pieceHash = Manager.torrent_info.piece_hashes[p].array();
        // Helpers.verifyHash(toVerify, pieceHash);
        // peer.from_peer_.reset();
        // 
        // //cop dat data
        // peer.from_peer_.readFully(data);
			
  			String name = "blocks/" + p + " " + b.getBlock();
  			RandomAccessFile file = new RandomAccessFile(name,"rws");
  			file.write(piece_data);
  			file.close();
        // System.arraycopy(data, 0, piece, b, l);
        System.out.print("Got from " + peer.peer_id_);
        b.print();
      }
    } catch (Exception e){
      Manager.q.add(b);
      System.out.println(peer.peer_id_ + " " + e);
      System.exit(1);
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
