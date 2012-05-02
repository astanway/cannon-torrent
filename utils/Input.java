package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Input implements Runnable{

	public void run(){		
    System.out.println("Closing sockets...");
		for(int i = 0; i <Manager.activePeerList.size(); i++){
			Manager.activePeerList.get(i).closeSocket();
		}
		Manager.activePeerList.clear();
		
    System.out.println("Notifying tracker...");
		byte[] response = null;
    response = Helpers.getURL(Manager.constructQuery(Manager.port, Manager.uploaded, Manager.downloaded,
				Manager.torrent_info.file_length, Manager.STOPPED));
				
		while(response == null){
		  continue;
		}
		
    System.out.println("Shutting down...");
				
		Runtime.getRuntime().exit(1);
	}
}
