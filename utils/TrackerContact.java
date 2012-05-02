package utils;

import java.util.TimerTask;

public class TrackerContact extends TimerTask {

	int choice = 0;

	/**
	 * Constructor to say what to tell the tracker
	 * @param i the choice of what we want to update
	 */
	public TrackerContact(int i) {
		choice = i;
	}

	/**
	 * runs the update to the tracker
	 */
	public void run() {
		if (choice == 0) {
			// System.out.println("\nUpdating tracker.");
			// System.out.println("Uploaded = " + Manager.uploaded);
			// System.out.println("Downloaded = " + Manager.downloaded);
			Manager.constructQuery(Manager.getPort(), Manager.uploaded,
					Manager.downloaded, Manager.torrent_info.file_length
							- Manager.downloaded, "");
		} else if (choice == 1) {
			// System.out.println("Updating tracker.");
			// System.out.println("Uploaded = " + Manager.uploaded);
			// System.out.println("Downloaded = " + Manager.downloaded);
			Manager.constructQuery(Manager.getPort(), Manager.uploaded,
					Manager.downloaded, 0, Manager.COMPLETED);
		} else {
			Manager.constructQuery(Manager.getPort(), Manager.uploaded,
					Manager.downloaded, Manager.torrent_info.file_length
							- Manager.downloaded, "");
		}
	}
}
