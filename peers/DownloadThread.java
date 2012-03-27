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
        
        //TODO: need have array

        //TODO: send bitfield

        while(!Manager.q.isEmpty()){
          Block b = Manager.q.poll();
          System.out.print("Trying ");
          b.print();
          if(bfb[b.getPiece()] == true){
            if(Manager.have_piece.get(b.getPiece()) == 0){
              peer.sendMessage(Peer.INTERESTED);
              if(peer.choked == false){
                if(!downloadBlock(b)){
                  run();
                  return;
                }
              } else {
                  m = peer.listen();
                  if(m.getId() == Message.TYPE_UNCHOKE){
                    System.out.println("Peer " + peer.peer_id_ + " unchoked us");
                    if(!downloadBlock(b)){
                      run();
                      return;
                    }
                    peer.choked = false;
                }
              }
            } else {
              peer.sendMessage(Peer.UNINTERESTED);
              if(!Manager.q.add(b)){
                System.out.println("couldn't add it ");
                b.print();
              }
            }
          } 
        }

        //TODO: if we have a full piece, broadcast
      } else if (m.getId() == Message.TYPE_HAVE){
        System.out.println("They want something from us.");
      }
      
      System.out.println("q empty");
      System.out.println(Manager.q.size());
      // downloadPieces(peer);
      //     
      //   response = Helpers.getURL(constructQuery(PORT, 0, TORRENT_INFO.file_length, 0, COMPLETED));
      //
      // peer.closeSocket();
    }
  }
  
  
  public boolean downloadBlock(Block b){
    int p = b.getPiece(); 
	  int i = b.getBlockIndex();
	  int l = b.getLength();
	  byte[] data = b.getData();

    try{
      peer.requestBlock(b);
      Message m = peer.listen();
      
      if (m == null){
        if(!Manager.q.add(b)){
          System.out.println("couldn't add it ");
          b.print();
        }
        System.out.println("restarting");
        return false;
      } else if(m.getId() == Message.TYPE_UNCHOKE){
        System.out.println("Peer " + peer.peer_id_ + " unchoked us");
        peer.choked = false;
        if(!Manager.q.add(b)){
          System.out.println("couldn't add it ");
          b.print();
        }
        return true;
      } else if (m.getId() == Message.TYPE_CHOKE){
        System.out.println("Peer " + peer.peer_id_ + " choked us");
        peer.choked = true;
        if(!Manager.q.add(b)){
          System.out.println("couldn't add it ");
          b.print();
        }
        return true;
      } else if (m.getId() == Message.TYPE_PIECE){
        PieceMessage pm = (PieceMessage) m;
        byte[] piece_data = pm.getData();

        // verify the data
        byte[] pieceHash = Manager.torrent_info.piece_hashes[p].array();
        Helpers.verifyHash(piece_data, pieceHash);
			
  			String name = "blocks/" + p + " " + b.getBlock();
  			RandomAccessFile file = new RandomAccessFile(name,"rws");
  			file.write(piece_data);
  			file.close();
        // System.arraycopy(data, 0, piece, b, l);
        System.out.print("Got from " + peer.peer_id_);
        b.print();
        return true;
      }
    } catch (Exception e){
      if(!Manager.q.add(b)){
        System.out.println("couldn't add it ");
        b.print();
      }
      System.out.println(peer.peer_id_ + " " + e);
      return true;
    }
    return true;
  }
}
