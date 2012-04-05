package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Input implements Runnable{

	public void run(){
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		while(!input.equalsIgnoreCase("exit")){
			try {
				input = in.readLine();
			} catch (IOException e) {
				//System.out.println("Wtf IO exception");
				//e.printStackTrace();
				new Thread(new Input()).start();
				return;
			}
		}
		
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
