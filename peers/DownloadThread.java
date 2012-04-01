package peers;

import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import utils.*;
import utils.Message.*;

public class DownloadThread implements Runnable {
  
  public Peer peer = null;
  
  public DownloadThread(Peer _peer){
    peer = _peer;
  }
  
  public void run() {
    peer.closeSocket();
    peer.createSocket(peer.ip_, peer.port_);
    peer.establishStreams();
    peer.sendHandshake(Manager.peer_id, Manager.info_hash);

    if(peer.receiveHandshake(Manager.info_hash)){
      Message m = peer.listen();

      if(m == null){
        System.out.println("No message here.");
      }
      
      while(m.getId() == Message.TYPE_KEEP_ALIVE){
        m = peer.listen();
      }
      
      byte[] bf = null;
      
      //only download if we get a bitfield
      if(m.getId() == Message.TYPE_BITFIELD){
        BitfieldMessage bfm = (BitfieldMessage) m;
        bf = bfm.getData();
        boolean[] bfb = BitToBoolean.convert(bf);
        
        //TODO: need have array

        //TODO: send bitfield
        
        //TODO: send start message once
        
        //loop as long as there are blocks on the queue
        while(!checkFull()){
          Block b = Manager.q.poll();
          if(b == null){
            continue;
          }

          //do they have what we want?
          if(bfb[b.getPiece()] == true){

            //do we actually want what we're asking for?
            if(Manager.have_piece.get(b.getPiece()) == 0){

              try {
                peer.sendInterested();
              } catch (Exception e) {
                e.printStackTrace();
                Manager.q.add(b);
                System.out.println("restarting");
                run();
                return;
              }
              
              if(peer.choked == false){
                if(!downloadBlock(b)){
                  //restart connection if download fails
                  System.out.println("restarting");
                  run();
                  return;
                }
              } else {
                  m = peer.listen();
                  if(m.getId() == Message.TYPE_UNCHOKE){
                    System.out.println("Peer " + peer.peer_id_ + " unchoked us");
                    if(!downloadBlock(b)){
                      //restart connection if download fails
                      System.out.println("restarting");
                      run();
                      return;
                    }
                  peer.choked = false;
                }
              }
            } else {
                //we don't want it
                try {
                  peer.sendUninterested();
                } catch (Exception e) {
                  Manager.q.add(b);
                  e.printStackTrace();
                  System.out.println("restarting");
                  run();
                  return;
                }
              }
            } else {
              //they don't have it
              try {
                peer.sendUninterested();
              } catch (Exception e) {
                e.printStackTrace();
                System.out.println("restarting");
                Manager.q.add(b);
                run();
                return;
              }
            } 
          }

        //TODO: if we have a full piece, broadcast to tracker.
        
      } else if (m.getId() == Message.TYPE_HAVE){
        
        //TODO: put in upload logic here.
        
        System.out.println("They want something from us.");
      }
    } else {
      System.out.println("No handshake?");
    }
  }
  
  public boolean checkFull(){
    for(int i = 0; i < Manager.have_piece.length(); i++){
      if(Manager.have_piece.get(i) != 1){
        return false;
      }
    }
    return true;
  }
  
  public boolean downloadBlock(Block b){
    int p = b.getPiece(); 
	  int i = b.getBlockOffset();
	  int l = b.getLength();
	  byte[] data = b.getData();

    try{
      peer.requestBlock(b);
      Message m = peer.listen();
      
      if (m == null){
        //add it back onto the queue and restart the connection
        Manager.q.add(b);
        return false;
      } else if(m.getId() == Message.TYPE_UNCHOKE){
        System.out.println("Peer " + peer.peer_id_ + " unchoked us");
        peer.choked = false;
        Manager.q.add(b);
        return true;
      } else if (m.getId() == Message.TYPE_CHOKE){
        System.out.println("Peer " + peer.peer_id_ + " choked us");
        peer.choked = true;
        Manager.q.add(b);
        return true;
      } else if (m.getId() == Message.TYPE_PIECE){
        PieceMessage pm = (PieceMessage) m;
        byte[] piece_data = pm.getData();
        b.setData(piece_data);

        //make all single digits double, so that the sorting will work later on
        String name = "";
        if(b.getBlock() < 10){
          name = p + " 0" + b.getBlock();
        } else {
          name = p + " " + b.getBlock(); 
        }
        
        RandomAccessFile file = new RandomAccessFile("temp/" + name, "rw");
        file.write(piece_data);
        file.close();
        
        File rename = new File("temp/" + name);
        rename.renameTo(new File("blocks/" + name));
        
        System.out.print(peer.peer_id_ + " ");
        b.print();
        return true;
      } else{
        System.out.println("Other : " + m.getId());
        Manager.q.add(b);
      }
    } catch (Exception e){
      e.printStackTrace();
      Manager.q.add(b);
      return true;
    }
    return true;
  }
}
