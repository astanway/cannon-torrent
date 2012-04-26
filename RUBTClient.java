import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import utils.*;
import peers.PeerListener;
import java.awt.*;
import javax.swing.*;

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
				// //System.out.println("Temp/ exists");
			}
		} else {
			tempFile.mkdir();
		}

		// check if a local temp directory exists
		tempFile = new File("blocks/");
		if (tempFile.exists()) {
			if (tempFile.isDirectory()) {
				// //System.out.println("Blocks/ exists");
			}
		} else {
			tempFile.mkdir();
		}

		manager = new Manager(torrentFile, savedFile);
		manager.setPeerId();
		manager.queryTracker();
		manager.setTimers();

		while (!manager.peersReady) {
		  try {
  			Thread.sleep(50);
  		} catch (InterruptedException e) {
  		}
		}

		while (!manager.piecesReady) {
		  try {
  			Thread.sleep(50);
  		} catch (InterruptedException e) {
  		}
		}

    manager.download();

		Thread t = new Thread(new PeerListener(Manager.getPort()));
		t.start();
		new Thread(new Input()).start();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(manager);
			}
		});
	}

	private static void createAndShowGUI(Manager manager) {
		JFrame frame = new JFrame("Cannon");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container pane = frame.getContentPane();
		JTabbedPane tabbedPane = new JTabbedPane();

		// General Tab
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel nested1 = new JPanel(new BorderLayout());
		JLabel name = new JLabel(manager.torrent_info.file_name);
    name.setFont(new Font("Sans Serif", Font.BOLD, 30));
		nested1.add(name, BorderLayout.NORTH);

		JLabel size = new JLabel(
				"File Size: "
						+ Gooey.humanReadableByteCount(manager.torrent_info.file_length));
		nested1.add(size, BorderLayout.CENTER);

		manager.downloadedLabel = new JLabel("");
		nested1.add(manager.downloadedLabel, BorderLayout.EAST);

		manager.uploadedLabel = new JLabel("");
		nested1.add(manager.uploadedLabel, BorderLayout.WEST);
    
    JPanel bottom = new JPanel();
		manager.progress = new JProgressBar();
		manager.progress.setStringPainted(true);
		bottom.add(manager.progress);
		tabbedPane.addTab("General", panel);
		
		panel.add(nested1, BorderLayout.NORTH);
		panel.add(bottom, BorderLayout.SOUTH);

		// Peers Tab
		JPanel peers = new JPanel();
		String[] columnNames = { "Id", "IP", "Port", "Downloaded", "Uploaded"};

		Object[][] data = Manager.getPeerList();
		manager.peerTable = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(manager.peerTable);
		peers.add(scrollPane);
		tabbedPane.addTab("Peers", peers);

		pane.add(tabbedPane);
		frame.setPreferredSize(new Dimension(800, 400));
		frame.setResizable(true);
		frame.pack();
		frame.setVisible(true);
	}

	public static void printIntro() {
		System.out
				.println(" /¯¯¯¯\\     /¯¯¯¯¯||¯¯¯\\|¯¯¯| |¯¯¯\\|¯¯¯| /¯¯¯¯¯\\ |¯¯¯\\|¯¯¯|");
		System.out
				.println("|  (\\__/| /   !  | |        '|||       '|||  x |'|       '||");
		System.out
				.println(" \\____\\ /___/¯|__'||___|\\___| |___|\\___| \\_____/ |___|\\___|");
		System.out
				.println("                                            Is this legal?\n                                      This better be legal.\n                                              -Oscar Wilde\n");
		System.out.println("Type 'exit' to quit at any time.");
	}

}
