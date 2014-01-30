package game;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class Logger {
	
	int t;
	PrintWriter writer = null;

	public Logger(){
		t=0;
		
		try {
			writer=new PrintWriter("resources/log.txt", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
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
		writer.write(text);
	}
	
	public void fileLog(String text){
		writer.write(text);
	}
	
	public void flush(){
		writer.write("\n");
	}
	
	public void loga(String text){
		System.out.println(text);
		writer.write(text);
	}
}
