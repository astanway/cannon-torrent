package utils;

public class Shutdown implements Runnable{
	
	public void run(){
		System.out.println("Shutting Down");
		System.out.println("SHUTTING DOWN");
		for(int i = 0;i <Manager.activePeerList.size();i++){
			System.out.println(i);
			Manager.activePeerList.get(i).closeSocket();
		}
		Manager.activePeerList.clear();
	}
}
