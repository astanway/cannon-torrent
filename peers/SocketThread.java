package peers;

import java.util.*;

public class SocketThread implements Runnable {
  
  public Peer peer_ = null;
  byte[] peer_id_ = null;
  byte[] info_hash_ = null;
  
  public SocketThread(Peer _peer, byte[] _peer_id, byte[] _info_hash){
    peer_ = _peer;
    peer_id_ = _peer_id;
    info_hash_ = _info_hash;
  }
  
  public void run() {
    peer_.createSocket(peer_.ip_, peer_.port_);
    peer_.establishStreams();
    peer_.sendHandshake(peer_id_, info_hash_);
    
    if(peer_.receiveHandshake(info_hash_)){
        
      peer_.sendMessage(Peer.INTERESTED);
        
      while(true){ if(peer_.listenForUnchoke()){ break; }}
    //     
    //   response = Helpers.getURL(constructQuery(PORT, TORRENT_INFO.file_length, 0, 0, STARTED));
    //     
    //   downloadPieces(peer);
    //     
    //   response = Helpers.getURL(constructQuery(PORT, 0, TORRENT_INFO.file_length, 0, COMPLETED));
    //     
      peer_.closeSocket();
    }
  }
}
