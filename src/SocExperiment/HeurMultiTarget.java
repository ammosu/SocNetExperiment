package SocExperiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class HeurMultiTarget {
	private double threshold;
	private ArrayList<Integer> candidate = new ArrayList<Integer>();
	private Hashtable<Integer, ArrayList<Double>> MiiaScore = new Hashtable<Integer, ArrayList<Double>>();
	private Hashtable<Integer, Double> MIIAScore = new Hashtable<Integer, Double>();
	private ArrayList<Integer> targets = new ArrayList<Integer>();
	
	public void setThreshold(double th)
	{
		this.threshold = th;
	}
	public void setTargets(ArrayList<Integer> targets)
	{
		this.targets = targets;
	}
	
	
	public void MiiaScoreArr(InfMultiTarget imt, int monteCarloTimes)
	{
		ArrayList<Integer> allnodes = imt.getNodes(); // all nodes
		imt.createResult();
		// for each monteCarlo process
		for(int i = 0; i < monteCarloTimes; i++)
		{//for each target
			for(int target : this.targets)
			{
				Hashtable<Integer, Double> tScoreTable = new Hashtable<Integer, Double>();
				//ArrayList<Integer> tScore = new ArrayList<Integer>();
				MiiaScoreUpdate(MIIAalg2(target, imt.getGraph()));
				
			}
		}
	}
	
	
	public Hashtable<Integer, Double> MIIAalg2(int targetID, Hashtable<Integer, MonteCarlo> Graph)  
	{
		HeurSoc hS = new HeurSoc();
		Hashtable<Integer, Double> miiaScore = new Hashtable<Integer, Double>();
		miiaScore.put(targetID, 0.0);
		//---
		ArrayList<Integer> neighbors = Graph.get(targetID).neighborID;
		ArrayList<Double> nbr_probability = Graph.get(targetID).activeProbability();
		Hashtable<Integer, Double> Hash = hS.arr2Hash(1.0, neighbors, nbr_probability);
		
		
		while(Hash.size()!=0)
		{
			Hashtable<Integer, Double> tempHash = new Hashtable<Integer, Double>();
			miiaScore.put(hS.maxKey(Hash), hS.maxValue(Hash)); //put max every time
			tempHash = hS.arr2Hash(hS.maxValue(Hash), Graph.get(hS.maxKey(Hash)).neighborID, Graph.get(hS.maxKey(Hash)).activeProbability());
			for(Map.Entry<Integer, Double> entry : tempHash.entrySet())
			{
				if(!miiaScore.containsKey(entry.getKey()) )
				{
					if(!Hash.containsKey(entry.getKey())) // put new
					{
						Hash.put(entry.getKey(), entry.getValue());
					}
					else if(entry.getValue() > Hash.get(entry.getKey()))// update 
					{
						Hash.remove(entry.getKey());
						Hash.put(entry.getKey(), entry.getValue());
					}
				}
			}
			
			Hash.remove(hS.maxKey(Hash));
		}
		return miiaScore;
	}
	
	public void MiiaScoreUpdate(Hashtable<Integer, Double> scoreHash)
	{
		for(Map.Entry<Integer,Double> entry : scoreHash.entrySet())
		{
			if(this.MIIAScore.containsKey(entry.getKey())) // if key exist than plus 
			{
				double score = this.MIIAScore.get(entry.getKey());
				this.MIIAScore.remove(entry.getKey());
				this.MIIAScore.put(entry.getKey(), entry.getValue() + score);  //update
			}
			else
				this.MIIAScore.put(entry.getKey(), entry.getValue());
		}
	}
	
	public ArrayList<Integer> string2Targets(String s)
	{
		ArrayList<Integer> Targets = new ArrayList<Integer>();
		String[] str = {"-1"};
		if(s.split(", ").length>1)
			str = s.split(", ");
		else if (s.split(",").length>1) 
			str = s.split(",");
		
		for(int i = 0; i < str.length; i++)
			Targets.add(Integer.parseInt(str[i]));
			
		return Targets;
	}
	
	public void testS2T()
	{
		String s = "1, 100";
		if(string2Targets(s).get(0) != 1 || string2Targets(s).get(1) != 100)
			System.out.println("S2T error");
	}
	public ArrayList<Integer> getTopKMiiaScore(int k)
	{
		ArrayList<Integer> topKScoreKey = new ArrayList<Integer>();
		for(int i = 0; i < k; i++)
		{
			int key = new HeurSoc().maxKey(this.MIIAScore);
			this.MIIAScore.remove(key);
			topKScoreKey.add(key);
		}
		return topKScoreKey;
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HeurMultiTarget hMt = new HeurMultiTarget();
		
		String TargetStr = "0, 1"; //default target
		int MonteCarloTimes = 200;
		ArrayList<Integer> Targets = new ArrayList<Integer>();
		Targets = hMt.string2Targets(TargetStr);
		
		
		if(args.length >= 1)
		{
			Targets.clear();
			Targets = hMt.string2Targets(args[1]);
		}
		
		hMt.setTargets(Targets);
		
		String network = "com-dblp.ungraph - small.txt" , propnetwork = "prop.txt"; //default data
		
		if(args.length >= 2)
			network = args[1];
		if(args.length >= 3)
			propnetwork = args[2];
		int k = 5; //default
		if(args.length >= 4)
			k = Integer.parseInt(args[3]);
		if(args.length >= 5)
			MonteCarloTimes = Integer.parseInt(args[4]);
	
		double startTime, endTime, totalTime;
		
		boolean isDuplica = true;
		if(network.equals("Brightkite_edges.txt"))
			isDuplica = false;
		
		System.out.println("Targets: "+Targets.toString());
		
		InfMultiTarget iMt = new InfMultiTarget();
		iMt.dataRead(network, isDuplica);  // read network structure
		iMt.setNodeset();       // all nodes
		iMt.ReadPropagate(propnetwork);  //set propagation probability
		iMt.info();
		
		startTime = System.currentTimeMillis();
		
		hMt.MiiaScoreArr(iMt, 200);
		System.out.println(hMt.getTopKMiiaScore(k).toString());

		endTime = System.currentTimeMillis();
		
		totalTime = endTime - startTime;
		
		System.out.println("Execution Time: " + totalTime/1000+" sec");
	}

}
