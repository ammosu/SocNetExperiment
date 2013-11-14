package SocExperiment;
import java.util.*;
public class MonteCarlo {
	ArrayList<Integer> neighborID = new ArrayList<Integer>(); 
	ArrayList<Double> propability = new ArrayList<Double>();
	ArrayList<Double> in_Edge = new ArrayList<Double>();
	ArrayList<Boolean> actResult = new ArrayList<Boolean>(); 
	public MonteCarlo()
	{
		
	}
	public MonteCarlo(ArrayList<Integer> a)
	{
		neighborID = a;
	}
	
	public void MonteCarlo_trim() {
		neighborID.trimToSize();
		in_Edge.trimToSize();
		propability.trimToSize();
	}
	public int size() {
		
		return neighborID.size();
	}
	
}
