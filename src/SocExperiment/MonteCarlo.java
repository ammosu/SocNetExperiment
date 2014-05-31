package SocExperiment;
import java.util.*;
public class MonteCarlo {
	ArrayList<Integer> neighborID = new ArrayList<Integer>(); 
	ArrayList<Double> probability = new ArrayList<Double>();
	ArrayList<Double> in_Edge = new ArrayList<Double>();
	ArrayList<Boolean> actResult = new ArrayList<Boolean>(); 
	public MonteCarlo()
	{
		
	}
	public MonteCarlo(ArrayList<Integer> a)
	{
		neighborID = a;
	}
	
	public MonteCarlo(ArrayList<Integer> a, ArrayList<Double> b)
	{
		neighborID = a;
		probability = b;
	}
	
	public void MonteCarlo_trim() {
		neighborID.trimToSize();
		in_Edge.trimToSize();
		probability.trimToSize();
	}
	public int size() {
		return neighborID.size();
	}
	
	public int addProb(double d)
	{
		int t = 0;
		if(probability.size() == neighborID.size()-1)
			t = -1;
		probability.add(d);
		return t;
	}
	
	public ArrayList<Integer> activeNbr()
	{
		ArrayList<Integer> actNbrs = new ArrayList<Integer>();
		int size = actResult.size();
		if( size == 0)
		{
			System.out.println("actResult do not build");
		}
		else
		{
			for(int i = 0; i < size; i++)
			{
				if(actResult.get(i))
					actNbrs.add(neighborID.get(i));
			}
		}
		return actNbrs;
	}
	public ArrayList<Double> activeProbability()
	{
		ArrayList<Double> actProbabilities = new ArrayList<Double>();
		int size = actResult.size();
		if( size == 0)
		{
			System.out.println("actResult do not build");
		}
		else
		{
			for(int i = 0; i < size; i++)
			{
				if(actResult.get(i))
					actProbabilities.add(probability.get(i)); //true -> original probability
				else
					actProbabilities.add(0.0);  // false -> 0
			}
		}
		return actProbabilities;
	}
	public ArrayList<Double> activeInedgeWeight()
	{
		ArrayList<Double> actInedges = new ArrayList<Double>();
		int size = actResult.size();
		if( size == 0)
		{
			System.out.println("actResult do not build");
		}
		else
		{
			for(int i = 0; i < size; i++)
			{
				if(actResult.get(i))
					actInedges.add(in_Edge.get(i)); //true -> original probability
				else
					actInedges.add(0.0);  // false -> 0
			}
		}
		return actInedges;
	}
}
