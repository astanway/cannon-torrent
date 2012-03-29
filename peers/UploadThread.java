package peers;

import utils.Manager;
import utils.Message;
import utils.Message.*;

public class UploadThread implements Runnable {

	Peer peer;

	public UploadThread(Peer p) {
		peer = p;
	}

	public void run() {
		if (!peer.receiveHandshake(Manager.info_hash)) {
			try {
				peer.to_peer_.write(new String("DIAF YOU WHORE").getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		peer.sendHandshake(Manager.peer_id, Manager.info_hash);
		//Message.encode(peer.to_peer_, new BitfieldMessage(Manager.getBitfield()));
		Message temp = listen();
		switch(temp.getId()){
			case Message.TYPE_BITFIELD:
				
			case Message.TYPE_CHOKE:
				
			case Message.TYPE_HAVE:
				
			case Message.TYPE_INTERESTED:
				
			case Message.TYPE_KEEP_ALIVE:
			
			case Message.TYPE_NOT_INTERESTED:
				
			case Message.TYPE_PIECE:
				
			case Message.TYPE_REQUEST:
				
			case Message.TYPE_UNCHOKE:
		}

	}
	
	public Message listen(){
		try{
			return Message.decode(peer.from_peer_);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
				
	}
}
