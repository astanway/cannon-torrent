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
				System.out.println(input);
			} catch (IOException e) {
				System.out.println("Wtf IO exception");
				e.printStackTrace();
				new Thread(new Input()).start();
				return;
			}
		}
    // Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
		Runtime.getRuntime().exit(1);
	}
}
