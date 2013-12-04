package SocExperiment;
import java.io.IOException;
import java.util.*;

 
public class HeurSoc {
	
	private double MIIAthreashold;
	private Integer TargetID = 0;
	private Hashtable<Integer, Integer> previousNodes = new Hashtable<Integer, Integer>();
	private Hashtable<Integer, Double> MIIAScore = new Hashtable<Integer, Double>();	
	private ArrayList<Integer> seed;
	private HashSet<Integer> target;
	
	protected void setPreviousNode(Integer key, Integer value)
	{
		this.previousNodes.remove(key);
		this.previousNodes.put(key, value);
	}
	
	protected void setMIIAthreshold(double threshold)
	{
		this.MIIAthreashold = threshold;
	}
	
	protected void setMIIAScore(Hashtable<Integer, Double> table)
	{
		this.MIIAScore.clear();
		this.MIIAScore = table;
	}
	
	public Integer minKey(Hashtable<Integer, Double> table)  // find min key from hashtable
	{
		Integer minKey = -1;
		Double minValue = Double.MAX_VALUE; 
		for(Map.Entry<Integer,Double> entry : table.entrySet()) {
		     if(entry.getValue() > minValue) {
		         minValue = entry.getValue();
		         minKey = entry.getKey();
		     }
		}
		return minKey;
	}
	
	public Double minValue(Hashtable<Integer, Double> table)  // find min value from hash table
	{
		Double min = Collections.min(table.values());
		return min;
	}
	
	public void putSmallValue2Hash(Hashtable<Integer, Double> hashScore)
	{
		for(Map.Entry<Integer, Double> entry : hashScore.entrySet())
		{
			Integer key = entry.getKey();
			if(this.MIIAScore.containsKey(key))
			{
				if(this.MIIAScore.get(key) > hashScore.get(key))
				{
					this.MIIAScore.remove(key);
					this.MIIAScore.put(key, hashScore.get(key));
				}
			}
			else
				this.MIIAScore.put(key, hashScore.get(key));
		}
	}
	
	public void getContents()
	{
		for(Map.Entry<Integer,Double> entry : this.MIIAScore.entrySet()) 
		{
			System.out.println("Key: "+entry.getKey()+"\tValue: "+entry.getValue());
		}
	}
	
	public void MIIAalg(int targetID, Hashtable<Integer, MonteCarlo> Graph, int MonteCarloTimes)  
	{
		ArrayList<Integer> neighbors = Graph.get(targetID).activeNbr();
		ArrayList<Double> nbr_probility = Graph.get(targetID).activeProbability();
		Hashtable<Integer, Double> a = new Hashtable<Integer, Double>();
		for(int nbr : neighbors)
		{
			ArrayList<Integer> nb_nbr = Graph.get(nbr).neighborID;
			nb_nbr.remove(targetID);
			for(int n : nb_nbr)
			{
				
			}
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		
		HeurSoc t = new HeurSoc();
		Hashtable<Integer, Double> table = new Hashtable<Integer, Double>();
		Hashtable<Integer, Double> table2 = new Hashtable<Integer, Double>();
		table.put(1, 0.1);
		table.put(2, 0.2);
		table.put(3, 0.3);
		t.setMIIAScore(table);
		
		//table = new Hashtable<Integer, Double>();
		table2.put(1, 0.3);
		table2.put(2, 0.05);
		table2.put(3, 0.1);
		
		t.putSmallValue2Hash(table2);
		
		t.getContents();
		
		
		/*Soc3 d = new Soc3();
		
		int influenceTargetID = 0; //default target
		int MonteCarloTimes = 200; //default times
		int k = 1; //default seed size
		String network = "com-dblp.ungraph - small.txt" , propnetwork = "prop.txt"; //default data
		
		
		d.dataRead(network);
		d.setNodeset();
		d.ReadPropagate(propnetwork);  //set propagation probability
		d.setInEdgeGraph();  //set in edge weight from propagation graph
		d.trim();
		d.info();
		
		
		double startTime, endTime, totalTime; //timing
		
		startTime = System.currentTimeMillis();
		
	
		System.out.println
			("Our Target: "+influenceTargetID
			+"\n\nTarget Neighbors: \n"+d.getNeibh(influenceTargetID)
			+"\n\nCorresponding Propagation Probability: \n"+d.getNeibhPropGraph(influenceTargetID)
			+"\n\n ---- Find "+k+"-Seeds ----\n"
			+"Monte Carlo times: "+ MonteCarloTimes
			);
	
		//Seed Setting
		ArrayList<Integer> seeds = new ArrayList<Integer>();
	
		//MonteCarlo simulation
		
		//seeds = d.gr(k, influenceTargetID, MonteCarloTimes);
		
		System.out.println("\nGreedy algorithm:\n"+"Seed: " + seeds.toString());
	
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Execution Time: " + totalTime/1000+" sec");

		//evaluation
	
		d.setSeed(seeds);  //set our seed result 
		System.out.println("---Evaluation---\nExpected Times: ");
		System.out.println( d.MC_times(10000,influenceTargetID));
		*/
	}

}
