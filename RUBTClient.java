import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import utils.*;
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
		
		printIntro();
		
		String torrentFile = args[0];
		String savedFile = args[1];

		// check if a local temp directory exists
		File tempFile = new File("temp/");
		if (tempFile.exists()) {
			if (tempFile.isDirectory()) {
				// System.out.println("Temp/ exists");
			}
		} else {
			tempFile.mkdir();
		}

		// check if a local temp directory exists
		tempFile = new File("blocks/");
		if (tempFile.exists()) {
			if (tempFile.isDirectory()) {
        // System.out.println("Blocks/ exists");
			}
		} else {
			tempFile.mkdir();
		}

		manager = new Manager(torrentFile, savedFile);
		manager.setPeerId();
		manager.setPeerList(getPeers());
		while(!manager.peersReady){
		  continue;
		}
		
		manager.setTimers();
		
		while(!manager.piecesReady){
		  continue;
		}

		manager.download();

		Thread t = new Thread(new PeerListener(Manager.getPort()));
		t.start();
		new Thread(new Input()).start();
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
	}
	
	public static void printIntro(){
	  System.out.println(" /¯¯¯¯\\     /¯¯¯¯¯||¯¯¯\\|¯¯¯| |¯¯¯\\|¯¯¯| /¯¯¯¯¯\\ |¯¯¯\\|¯¯¯|");
    System.out.println("|  (\\__/| /   !  | |        '|||       '|||  x |'|       '||");
    System.out.println(" \\____\\ /___/¯|__'||___|\\___| |___|\\___| \\_____/ |___|\\___|");
    
    System.out.println("                                            Is this legal?\n                                      This better be legal.\n                                              -Oscar Wilde\n");
	}

}
