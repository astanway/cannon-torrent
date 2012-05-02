package utils;

import java.awt.*;
import javax.swing.*;
import java.io.*;

import peers.Peer;

public class Gooey {

	/**
	 * updates the gui, the labels, the progress bar and the peertable
	 */
	public static void updateGui() {
		updateLabels();
		updateProgressBar();
		updatePeerTable();
	}

	/**
	 * updates the peer table with new values
	 */
	public static void updatePeerTable() {
		try {
			for (int i = 0; i < Manager.peerList_.size(); i++) {
				Manager.peerTable
						.setValueAt(humanReadableByteCount(Manager.peerList_
								.get(i).downloaded.get()), i, 3);
				Manager.peerTable
						.setValueAt(humanReadableByteCount(Manager.peerList_
								.get(i).uploaded.get()), i, 4);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * updates the labels in the peer table
	 */
	public static void updateLabels() {
		try {
			Manager.piecesLabel.setText("Pieces: "
					+ Helpers.stringifyBoolArray(BitToBoolean
							.convert(Manager.have_piece)));
		} catch (Exception e) {
		}
	}

	/**
	 * makes a byte count easier to read
	 * @param bytes the number to make readable
	 * @return the byte count formatted properly
	 */
	public static String humanReadableByteCount(int bytes) {
		int unit = 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = ("kMGTPE").charAt(exp - 1) + "";
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Used for the progress bar
	 * 
	 * @param completed
	 *            completed value
	 * @param total
	 *            total value
	 */
	public static synchronized void updateProgressBar() {
		double completed = (double) Manager.downloaded;
		double total = (double) Manager.torrent_info.file_length;

		double blockTotal = (double) Manager.numBlocks;
		double blockComp = (double) new File("blocks/").listFiles().length;
		double blockProg = blockComp / blockTotal;

		int bar = (int) Math.floor(blockProg * 100);
		try {
			Manager.progress.setValue(bar);
		} catch (Exception e) {
		}

		int width = 50;
		double prog = completed / total;

		System.out.print("\r[");
		int i = 0;
		for (; i < prog * width; i++) {
			System.out.print("=");
		}
		System.out.print(">");
		for (; i < (blockProg * width - 1); i++) {
			System.out.print(".");
		}
		for (; i < width; i++) {
			System.out.print(" ");
		}
		System.out.print("] " + Math.ceil(prog * 100) + "%");
	}
}
