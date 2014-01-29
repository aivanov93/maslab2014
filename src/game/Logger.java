package game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class Logger {
	
	int t;
	FileOutputStream s=null;
	public Logger(){
		t=0;
		File file = new File("resources/log.txt");

		try {
			 s = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public void step(){
		t++;
	}
	
	public void log(String text){
		if (t%20==0){
			System.out.println(text);
		}
	}
	
	public void loga(String text){
		System.out.println(text);
	}
}
