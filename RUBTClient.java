import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import utils.*;
import peers.PeerListener;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class RUBTClient {

	public static Manager manager = null;
	public static ReentrantLock arrayLock = null;
	public static ReentrantLock fileLock = null;
	public boolean[][] blockDone = null;
	public static JButton quit = new JButton ("Quit");

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
		JPanel inner = new JPanel();

		// General
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel nested1 = new JPanel(new BorderLayout());
		JLabel name = new JLabel(manager.torrent_info.file_name);
    name.setFont(new Font("Sans Serif", Font.BOLD, 30));
		nested1.add(name, BorderLayout.NORTH);
    
    JPanel bottom = new JPanel(new BorderLayout());
		manager.progress = new JProgressBar();
		Border paddingBorder = BorderFactory.createEmptyBorder(10,10,10,10);
    manager.progress.setBorder(paddingBorder);
		manager.progress.setStringPainted(true);
		bottom.add(manager.progress);
		manager.piecesLabel = new JLabel();
		bottom.add(manager.piecesLabel, BorderLayout.SOUTH);
    inner.add("General", panel);
		
		panel.add(nested1, BorderLayout.NORTH);
		panel.add(bottom, BorderLayout.SOUTH);

    // Peers
		JPanel peers = new JPanel(new BorderLayout());
		peers.setPreferredSize(new Dimension(700, 250));
		String[] columnNames = { "Id", "IP", "Port", "Downloaded", "Uploaded"};

		Object[][] data = Manager.getPeerList();
		manager.peerTable = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(manager.peerTable);
		peers.add(scrollPane, BorderLayout.NORTH);


		JPanel quitPanel = new JPanel(new BorderLayout());
    ActionListener al = new ActionListener() {
    	public void actionPerformed(ActionEvent e){
    	  if(e.getSource() == quit){
      		new Thread(new Input()).start();
    	  }
    	}
    };
    
    quit.addActionListener(al);
    quitPanel.add(quit);
  	inner.add(quitPanel);
		inner.add(peers);

    pane.add(inner);
    
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
