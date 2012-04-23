package utils;

import peers.Peer;

public class Choker implements Runnable {
	public void run() {
		double rand = Math.random() * 3;
		Math.round(rand);
		int totalMin = Integer.MAX_VALUE;
		int totalMinIndex = -1;
		int totalMax = Integer.MIN_VALUE;
		int totalMaxIndex = -1;
		while (Manager.unchokedPeers.size() < 3) {
			double random = Math.random()
					* (Manager.wantUnchokePeers.size() - 1);
			Long blah = Math.round(random);
			int index = blah.intValue();
			Manager.unchokedPeers.add(Manager.wantUnchokePeers.get(index));
			
			try {
				Message.encode(Manager.wantUnchokePeers.get(index).to_peer_,
						Message.UNCHOKE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Manager.wantUnchokePeers.remove(index);
		}
		int total;
		for (int i = 0; i < Manager.unchokedPeers.size(); i++) {
			total = Manager.unchokedPeers.get(i).downloaded.get() + Manager.unchokedPeers.get(i).uploaded.get();
			if(total < totalMin){
				totalMinIndex = i;
			}
		}
		for(int i = 0; i<Manager.wantUnchokePeers.size();i++){
			total = Manager.wantUnchokePeers.get(i).downloaded.get() + Manager.wantUnchokePeers.get(i).uploaded.get();
			if(total>totalMax){
				totalMaxIndex = i;
			}
		}
		Peer toAdd;
		Peer toRemove;
		if(totalMaxIndex !=-1){
			toAdd = Manager.wantUnchokePeers.get(totalMaxIndex);
		}
		if(totalMinIndex != -1){
			toRemove = Manager.unchokedPeers.get(totalMinIndex);
		}
		
		

		
	}

}
