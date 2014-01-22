package vision;
import java.util.Stack;


public class Timer {
	
	Stack<Long> start=new Stack<Long>();
	
	String name;
	double accum=0;
	public Timer(){
		
	}
	
	public void start(){
		start.push(System.nanoTime());
	}
	
	public void print(String name){
		double time=(System.nanoTime()-start.pop())/1000000000.0;
		accum+=time;
		System.out.println(name+" "+time+"s and accumulated "+accum);
	}
}
