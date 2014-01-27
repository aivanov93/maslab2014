package game;

public class Logger {
	
	int t;
	public Logger(){
		t=0;
	}
	
	public void step(){
		t++;
	}
	
	public void log(String text){
		if (t%10==0){
			System.out.println(text);
		}
	}
	
	public void loga(String text){
		System.out.println(text);
	}
}
