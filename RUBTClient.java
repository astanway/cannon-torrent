import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import utils.*;
import utils.Shutdown;
import utils.Message.BitfieldMessage;

import peers.Peer;
import peers.PeerListener;

public class RUBTClient {

	public static Manager manager = null;
	public static ReentrantLock arrayLock = null;
	public static ReentrantLock fileLock = null;
	public boolean[][] blockDone = null;

	public static final int NUM_THREADS = 10;
	public static final int MAX_THREADS = 20;
	public static final int TIMEOUT = 600;

	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("USAGE: RUBTClient [torrent-file] [file-name]");
			System.exit(1);
		}
		String torrentFile = args[0];
		String savedFile = args[1];

		// start the manager
		
		//Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));

		//check if a local temp directory exists
		File tempFile = new File("temp/");
		if(tempFile.exists()){
			if(tempFile.isDirectory()){
				System.out.println("yay its a directory");
			}
		}else{
			tempFile.mkdir();
		}
		
		manager = new Manager(torrentFile, savedFile);
		manager.setPeerId();

		manager.setPeerList(getPeers());
		Thread t = new Thread(new PeerListener(Manager.getPort()));
		t.start();
		manager.download();

		// This executes right after the above command, not after we're finished
		// downloading.
		//Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
	}

	// query the tracker and get the initial list of peers
	public static byte[] getPeers() {
		byte[] response = null;
		int i = 0;
		for (i = 6881; i <= 6889;) {
			try {
				response = Helpers.getURL(manager.constructQuery(i, 0, 0,
						manager.getTorrentInfo().file_length, ""));
				manager.setPort(i);
				break;
			} catch (Exception e) {
				System.out.println("Port " + i + " failed");
				i++;
				continue;
			}
		}
		manager.queryTracker();
		
		return response;
		// wait for the manager to be ready
		/*
		 * while (!manager.ready) { } System.out.println("All systems go.");
		 * System.out.println(manager.q.size()); manager.download();
		 */
	}

}
