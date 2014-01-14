package SocExperiment;
import java.io.IOException;
import java.util.*;

 
public class HeurSoc {
	
	private double MIIAthreshold;
	private Integer TargetID ;
	private Hashtable<Integer, Integer> previousNodes = new Hashtable<Integer, Integer>();
	//private Hashtable<Integer, Hashtable<Integer, Double>> nbrMIIAProb = new Hashtable<Integer, Hashtable<Integer, Double>>();
	private Hashtable<Integer, Double> MIIAScore = new Hashtable<Integer, Double>();	
	private ArrayList<Integer> seed;
	/*private SortedSet<Map.Entry<Integer, Double>> Score = new TreeSet<Map.Entry<Integer, Double>>(
			new Comparator<Map.Entry<Integer, Double>>() {
				@Override
				public int compare(Map.Entry<Integer, Double> e1, Map.Entry<Integer, Double> e2) 
				{
					return e1.getValue().compareTo(e2.getValue());
				}
			});
	private SortedSet<Map.Entry<Integer, Double>> sortedset = new TreeSet<Map.Entry<Integer, Double>>(
			new Comparator<Map.Entry<Integer, Double>>() {
				@Override
				public int compare(Map.Entry<Integer, Double> e1, Map.Entry<Integer, Double> e2) 
				{
					return e1.getValue().compareTo(e2.getValue());
				}
			});
	*/
	
	
	public HeurSoc()
	{
		//default setting
		this.MIIAthreshold = 0.8;
	}
	public HeurSoc(int targetID)
	{
		//default setting
		this.MIIAthreshold = 0.1;
		this.TargetID = targetID;
	}
	public HeurSoc(double threshold)
	{
		this.MIIAthreshold = threshold;
	}
	
	protected void setPreviousNode(Integer key, Integer value)
	{
		this.previousNodes.remove(key);
		this.previousNodes.put(key, value);
	}
	
	protected void setMIIAthreshold(double threshold)
	{
		this.MIIAthreshold = threshold;
	}
	
	protected void setMIIAScore(Hashtable<Integer, Double> table)
	{
		this.MIIAScore.clear();
		this.MIIAScore = table;
	}
	
	public Integer maxKey(Hashtable<Integer, Double> table)  // find min key from hashtable
	{
		Integer maxKey = -1;
		Double maxValue = Double.MIN_VALUE; 
		for(Map.Entry<Integer,Double> entry : table.entrySet()) {
		     if(entry.getValue() > maxValue) {
		         maxValue = entry.getValue();
		         maxKey = entry.getKey();
		     }
		}
		return maxKey;
	}
	
	public Double maxValue(Hashtable<Integer, Double> table)  // find min value from hash table
	{
		Double min = Collections.max(table.values());
		return min;
	}
	
	public void putBigValue2Hash(Hashtable<Integer, Double> hashScore)
	{
		for(Map.Entry<Integer, Double> entry : hashScore.entrySet())
		{
			Integer key = entry.getKey();
			if(this.MIIAScore.containsKey(key))
			{
				if(this.MIIAScore.get(key) < hashScore.get(key))
				{
					this.MIIAScore.remove(key);
					this.MIIAScore.put(key, hashScore.get(key));
				}
			}
			else
				this.MIIAScore.put(key, hashScore.get(key));
		}
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
	
	
	public void getContents(Hashtable<Integer, Double> hashtable)
	{
		for(Map.Entry<Integer,Double> entry : hashtable.entrySet()) 
		{
			System.out.println("Key: "+entry.getKey()+"\tValue: "+entry.getValue());
		}
	}
	
	public Hashtable<Integer, Double> MIIAalg(int targetID, Hashtable<Integer, MonteCarlo> Graph)  
	{
		Hashtable<Integer, Double> miiaScore = new Hashtable<Integer, Double>();
		
		//---
		ArrayList<Integer> neighbors = Graph.get(targetID).neighborID;
		ArrayList<Double> nbr_probability = Graph.get(targetID).probability;
		Hashtable<Integer, Double> targetHash = new Hashtable<Integer, Double>();
		
		for(int i = 0; i < neighbors.size(); i++)		// target neighbors hash
			targetHash.put(neighbors.get(i), nbr_probability.get(i));
		
		putBigValue2Hash(targetHash); // nbrs of target score
		//---
		
		Hashtable<Integer, Double> hash = new Hashtable<Integer, Double>();
		Hashtable<Integer, Double> Fixhash = new Hashtable<Integer, Double>(); //already max
		for(int nbr : neighbors)  //for each nbr do miia process
		{
			ArrayList<Integer> nb_nbr = Graph.get(nbr).neighborID;
			ArrayList<Double> nb_nbr_prob = Graph.get(nbr).activeProbability();
			
			Fixhash.put(nbr, 1.0); //initial
			
			if(nb_nbr.size() != nb_nbr_prob.size())
				System.out.println("size not equal");
			else
			for(int i = 0; i < nb_nbr.size(); i++) // initial
			{
				if(nb_nbr.get(i) != targetID && nb_nbr_prob.get(i) > this.MIIAthreshold)  // ID != Target
				{
					hash.put(nb_nbr.get(i), nb_nbr_prob.get(i));
				}
			}

			//miia score
			
			while(hash.size()!=0 && maxValue(hash) > this.MIIAthreshold )
			{
				int maxkey = maxKey(hash);
				
				nb_nbr = Graph.get(maxkey).neighborID;
				nb_nbr_prob = Graph.get(maxkey).activeProbability();
				
				for(int i = 0; i <  nb_nbr.size(); i++)
				{
					if(!Fixhash.containsKey(maxkey) && nb_nbr.get(i) != targetID && nb_nbr_prob.get(i)*maxValue(hash) > this.MIIAthreshold)
						hash.put(nb_nbr.get(i), nb_nbr_prob.get(i)*maxValue(hash));
				}
				Fixhash.put(maxkey, hash.get(maxkey));
				hash.remove(maxkey); // reach
			}
			
			//
			
			for(Map.Entry<Integer,Double> entry : Fixhash.entrySet())
			{
				if(miiaScore.containsKey(entry.getKey())) // if key exist than plus 
				{
					double score = miiaScore.get(entry.getKey());
					miiaScore.remove(entry.getKey());
					miiaScore.put(entry.getKey(), entry.getValue()*targetHash.get(nbr) + score);  //update
				}
				else
					miiaScore.put(entry.getKey(), entry.getValue()*targetHash.get(nbr));
			}
			
			hash = new Hashtable<Integer, Double>();
			Fixhash = new Hashtable<Integer, Double>();
			
		}
		//System.out.println("Size: " + miiaScore.size());
		//getContents(miiaScore);
		return miiaScore;
	}
	public Hashtable<Integer, Double> MIIAalg2(int targetID, Hashtable<Integer, MonteCarlo> Graph)  
	{
		Hashtable<Integer, Double> miiaScore = new Hashtable<Integer, Double>();
		miiaScore.put(targetID, 0.0);
		//---
		ArrayList<Integer> neighbors = Graph.get(targetID).neighborID;
		ArrayList<Double> nbr_probability = Graph.get(targetID).activeProbability();
		Hashtable<Integer, Double> Hash = arr2Hash(1.0, neighbors, nbr_probability);
		
		
		while(Hash.size()!=0)
		{
			Hashtable<Integer, Double> tempHash = new Hashtable<Integer, Double>();
			miiaScore.put(maxKey(Hash), maxValue(Hash)); //put max every time
			tempHash = arr2Hash(maxValue(Hash), Graph.get(maxKey(Hash)).neighborID, Graph.get(maxKey(Hash)).activeProbability());
			for(Map.Entry<Integer, Double> entry : tempHash.entrySet())
			{
				if(!miiaScore.containsKey(entry.getKey()) )
				{
					if(!Hash.containsKey(entry.getKey())) // put new
					{
						//Hash.remove(entry.getKey());
						Hash.put(entry.getKey(), entry.getValue());
					}
					else if(entry.getValue() > Hash.get(entry.getKey()))// update 
					{
						Hash.remove(entry.getKey());
						Hash.put(entry.getKey(), entry.getValue());
					}
				}
			}
			
			Hash.remove(maxKey(Hash));
		}
		
		
			

			
		
		System.out.println("Size: " + miiaScore.size());
		//getContents(miiaScore);
		return miiaScore;
	}
	
	public Hashtable<Integer, Double> arr2Hash(double multi, ArrayList<Integer> arr, ArrayList<Double> prob)
	{
		Hashtable<Integer, Double> hash = new Hashtable<Integer, Double>();
		int size = arr.size();
		if (size!=prob.size())
		{
			System.out.println("size different");
			return hash;
		}
		for(int i = 0; i < size; i++)
		{
			double probs = prob.get(i)*multi;
			if(probs >= this.MIIAthreshold)
				hash.put(arr.get(i), probs);
		}
		
		return hash;
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
	public int MiiaMaxSeed(ArrayList<Integer> seed)
	{
		int maxID = -1;
		Hashtable<Integer, Double> tempHash = new Hashtable<Integer, Double>(this.MIIAScore.size());
		for(Map.Entry<Integer, Double> entry : this.MIIAScore.entrySet())
			tempHash.put(entry.getKey(), entry.getValue());
		while(true)
		{
			maxID = maxKey(tempHash);
			if(seed.contains(maxID))
				tempHash.remove(maxID);
			else
				break;
		}
		
		return maxID;
		
	}
	
	
	public int getseed()
	{
		int a = maxKey(this.MIIAScore);
		this.MIIAScore.remove(a);
		return a;
	}
	
	
	public static void main(String[] args) throws IOException {
		
		HeurSoc t = new HeurSoc();
		/*
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
		
		*/
		Soc3 d = new Soc3();
		
		int influenceTargetID = 0; //default target
		int MonteCarloTimes = 200; //default times
		int k = 10; //default seed size
		String network = "Brightkite_edges.txt" , propnetwork = "Brightkite_edges_prop.txt"; //default data
		
		
		d.dataRead(network, false);
		d.setNodeset();
		d.ReadPropagate(propnetwork);  //set propagation probability
		d.setInEdgeGraph();  //set in edge weight from propagation graph
		d.trim();
		d.info();
		
		
		double startTime, endTime, totalTime; //timing
		
		startTime = System.currentTimeMillis();
		
		d.showInformation(influenceTargetID, k, MonteCarloTimes);
			
		//Seed Setting
		ArrayList<Integer> seeds = new ArrayList<Integer>();
	
		//MonteCarlo simulation
		
		//seeds = d.gr(k, influenceTargetID, MonteCarloTimes);
		for(int i = 0; i < MonteCarloTimes; i++)
		{
			d.clearActResult();
			d.createResult();
			//System.out.println("----"+i+" miia score----");
			t.MiiaScoreUpdate(t.MIIAalg2(0, d.getGraph()));
		}
		
		for(int i = 0; i < k; i++)
			seeds.add(t.getseed());
		
		System.out.println("\nHeuristic algorithm:\n"+"Seed: " + seeds.toString());
	
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Execution Time: " + totalTime/1000+" sec");

		//evaluation
	
		/*d.setSeed(seeds);  //set our seed result 
		System.out.println("---Evaluation---\nExpected Times: ");
		System.out.println( d.MC_times(200,influenceTargetID));
		*/
	}

}
