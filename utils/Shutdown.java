package utils;

public class Shutdown implements Runnable{
	
	public void run(){
		System.out.println("Shutting Down");
		for(int i = 0;i <Manager.activePeerList.size();i++){
			Manager.activePeerList.get(i).closeSocket();
		}
	}

}
