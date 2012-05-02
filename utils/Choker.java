package utils;

import java.util.TimerTask;

import peers.Peer;

public class Choker extends TimerTask {
	/**
	 * runs the choker thread
	 * optimistically chokes and unchokes peers based on their performance
	 */
	public void run() {
		double rand = Math.random() * 3;
		Math.round(rand);
		int totalMin = Integer.MAX_VALUE;
		int totalMinIndex = -1;
		int totalMax = Integer.MIN_VALUE;
		int totalMaxIndex = -1;
		int total;
		for (int i = 0; i < Manager.unchokedPeers.size(); i++) {
			total = Manager.unchokedPeers.get(i).lastDownloaded.get()
					+ Manager.unchokedPeers.get(i).lastUploaded.get();
			Manager.unchokedPeers.get(i).lastDownloaded.set(0);
			Manager.unchokedPeers.get(i).lastUploaded.set(0);
			if (total < totalMin) {
				totalMinIndex = i;
			}
		}
		for (int i = 0; i < Manager.wantUnchokePeers.size(); i++) {
			total = Manager.wantUnchokePeers.get(i).lastDownloaded.get()
					+ Manager.wantUnchokePeers.get(i).lastUploaded.get();
			Manager.unchokedPeers.get(i).lastDownloaded.set(0);
			Manager.unchokedPeers.get(i).lastUploaded.set(0);
			if (total > totalMax) {
				totalMaxIndex = i;
			}
		}
		Peer toAdd = null;
		Peer toRemove = null;
		if (totalMaxIndex != -1) {
			toAdd = Manager.wantUnchokePeers.get(totalMaxIndex);
		}
		if (totalMinIndex != -1) {
			toRemove = Manager.unchokedPeers.get(totalMinIndex);
		}
		if (toAdd == null || toRemove == null) {
			return;
		} else {
			try {
				Message.encode(toAdd.to_peer_, Message.UNCHOKE);
				toAdd.peerChoked = false;
			} catch (Exception e) {
        // e.printStackTrace();
			}
			Manager.unchokedPeers.add(toAdd);
			Manager.wantUnchokePeers.remove(toAdd);
			try {
				Message.encode(toRemove.to_peer_, Message.CHOKE);
				toRemove.peerChoked = true;
			} catch (Exception e) {
        // e.printStackTrace();
			}
			Manager.wantUnchokePeers.add(toRemove);
			Manager.unchokedPeers.remove(toRemove);
		}
	}

}
