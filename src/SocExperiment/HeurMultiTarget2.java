package SocExperiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

public class HeurMultiTarget2 {
	private double threshold = 0.1;
	//private ArrayList<Integer> candidate = new ArrayList<Integer>();
	//private Hashtable<Integer, ArrayList<Double>> MiiaScore = new Hashtable<Integer, ArrayList<Double>>();
	private ArrayList<Integer> candidate = new ArrayList<Integer>();
	private Hashtable<Integer, Double> MIIAScore = new Hashtable<Integer, Double>();
	private ArrayList<Integer> targets = new ArrayList<Integer>();
	
	private Hashtable<Integer, Double[]> MIIAArrScore = new Hashtable<Integer, Double[]>(); // node score
	private double[] remainTargetScore; // maximum remain score
	private ArrayList<Integer> elements = new ArrayList<Integer>(); // 
	
	
	
	public void mainProcess(ArrayList<Integer> targets, int k, String network, String propnetwork, int MonteCarloTimes) throws IOException
	{
		boolean isDuplica = true;
		if(network.equals("Brightkite_edges.txt"))
			isDuplica = false;
		
		InfMultiTarget iMt = new InfMultiTarget();
		iMt.dataRead(network, isDuplica);  // read network structure
		iMt.setNodeset();       // all nodes
		iMt.ReadPropagate(propnetwork);  //set propagation probability
		iMt.info();
		
		this.targets = targets;
		initRTV(iMt); // initialize remain target score
		
		
		MiiaScore(iMt, MonteCarloTimes, k);
	}
	public void initRTV(InfMultiTarget im)
	{
		if(this.targets.size() == 0)
			System.out.println("Targets not initialize");
		double[] a = new double[this.targets.size()];
		
		for(int i = 0; i < this.targets.size(); i++)
			for(int j = 0; j < im.getGraph().get(targets.get(i)).probability.size(); j++)
				a[i] += im.getGraph().get(targets.get(i)).probability.get(j);
		
		this.remainTargetScore = a;
	}
	public void setRTV(double[] a)
	{
		this.remainTargetScore = a;
	}
	public void subRTV(Double[] a, int mcScale)
	{
		for(int i = 0; i < a.length; i++)
			this.remainTargetScore[i] -= a[i]/(double)mcScale;
	}
	public void MiiaScore(InfMultiTarget imt, int monteCarloTimes, int k)  
	{
		//ArrayList<Integer> allnodes = imt.getNodes(); // all nodes
		
		ArrayList<Integer> stopArr = new Heur2Soc().splitTimesArr(monteCarloTimes, k);
		int stopIndex = 0;
		
		for(int i = 1; i <= monteCarloTimes; i++)
		{
			if(i == stopArr.get(stopIndex))
			{
				int key = sortTopElement();
				//System.out.println("!"+i);
				stopIndex++;
				subRTV(this.MIIAArrScore.get(key), i); 
			}
			
			imt.clearActResult();
			imt.createResult();
			for(int j = 0; j < this.targets.size();j++)
			{
				for(Map.Entry<Integer, Double> e : MIIAalg(this.targets.get(j), imt.getGraph()).entrySet())
				{
					if(!this.candidate.contains(e.getKey())) //<- if no such key
						this.candidate.add(e.getKey());
					if(this.MIIAArrScore.keySet().contains(e.getKey())) //<- already scoring
					{
						Double[] replaceArr = this.MIIAArrScore.get(e.getKey());
						replaceArr[j] += e.getValue();
						this.MIIAArrScore.put(e.getKey(), replaceArr);
					}
					else //<- not exist in hash
					{
						Double[] replaceArr = new Double[this.targets.size()];
						for(int a = 0; a<replaceArr.length;a++)
							replaceArr[a] = 0.0;
						replaceArr[j] += e.getValue();
						this.MIIAArrScore.put(e.getKey(), replaceArr);
					}
				}
				
			}
		}
	}
	
	public int sortTopElement()
	{
		final double[] values = this.remainTargetScore;
		int topKey = -1;
		ArrayList<Map.Entry<Integer, Double[]>> sortlist = new ArrayList<Map.Entry<Integer, Double[]>>(this.MIIAArrScore.entrySet());
		Collections.sort(sortlist, new Comparator<Map.Entry<Integer, Double[]>>()
	    		{
	    			public int compare(Map.Entry<Integer, Double[]> o1, Map.Entry<Integer, Double[]> o2) 
	    			{
	    				Double sum1 = 0.0, sum2 = 0.0;
	    				for(int i = 0; i < o1.getValue().length; i++)
	    					sum1+= o1.getValue()[i]*values[i];
	    				for(int i = 0; i < o2.getValue().length; i++)
	    					sum2+= o2.getValue()[i]*values[i];
	    				return sum1.compareTo(sum2);
	    			}
	    		});
		for(int i = sortlist.size()-1; i > 0; i--)
		{
			if(!this.elements.contains(sortlist.get(i).getKey()))
			{
				System.out.print(sortlist.get(i).getKey() + ",");
				topKey = sortlist.get(i).getKey();
				i = 0;
				this.elements.add(topKey);
			}
		}
		return topKey;
	}
	
	public void setThreshold(double th)
	{
		this.threshold = th;
	}
	public void setTargets(ArrayList<Integer> targets)
	{
		this.targets = targets;
	}
	public void cleatMIIAScore()
	{
		this.MIIAScore.clear();
	}
	
	public void MiiaScoreArr(InfMultiTarget imt, int monteCarloTimes, int k)
	{
		ArrayList<Integer> StopArray = new Heur2Soc().splitTimesArr(monteCarloTimes, k);
		int stopIndex = 0;
		ArrayList<Integer> seed = new ArrayList<Integer>();
		//ArrayList<Integer> allnodes = imt.getNodes(); // all nodes
		imt.createBinaryResult();
		// for each monteCarlo process
		for(int i = 0; i < monteCarloTimes; i++)
		{//for each target
			if(i == StopArray.get(stopIndex))
			{
				stopIndex++;
				seed.add(MiiaMaxSeed(seed));
			}
			
			for(int target : this.targets)
			{
				//Hashtable<Integer, Double> tScoreTable = new Hashtable<Integer, Double>();
				//ArrayList<Integer> tScore = new ArrayList<Integer>();
				MiiaScoreUpdate(MIIAalg(target, imt.getGraph()));
				
			}
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
			maxID = new HeurSoc().maxKey(tempHash);
			if(seed.contains(maxID))
				tempHash.remove(maxID);
			else
				break;
		}
		
		return maxID;
		
	}
	
	public Hashtable<Integer, Double> MIIAalg(int targetID, Hashtable<Integer, MonteCarlo> Graph)  
	{
		HeurSoc hS = new HeurSoc(this.threshold);
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
		System.out.println("Heuristic 2 for multi-target");
		HeurMultiTarget2 hMt = new HeurMultiTarget2();
		
		hMt.setThreshold(0.1);
		
		String TargetStr = "27210, 25704, 22883, 32792, 21773, 38395, 20859, 44903, 48409, 20506"; //default target
		int MonteCarloTimes = 200;
		ArrayList<Integer> Targets = new ArrayList<Integer>();
		Targets = hMt.string2Targets(TargetStr);
		
		
		if(args.length >= 1)
		{
			Targets.clear();
			Targets = hMt.string2Targets(args[1]);
		}
		
		hMt.setTargets(Targets);
		
		String network = "Brightkite_edges.txt" , propnetwork = "Brightkite_edges_TV2.txt"; //default data
		
		if(args.length >= 2)
			network = args[1];
		if(args.length >= 3)
			propnetwork = args[2];
		int k = 3; //default
		if(args.length >= 4)
			k = Integer.parseInt(args[3]);
		if(args.length >= 5)
			MonteCarloTimes = Integer.parseInt(args[4]);
	
		//ArrayList<Integer> StopArray = new Heur2Soc().splitTimesArr(MonteCarloTimes, k);
		
		System.out.println("Targets: "+Targets.toString());
		double startTime, endTime, totalTime;
		
		startTime = System.currentTimeMillis();
		
		hMt.mainProcess(Targets, k, network, propnetwork, MonteCarloTimes);
		/*boolean isDuplica = true;
		if(network.equals("Brightkite_edges.txt"))
			isDuplica = false;
		
		InfMultiTarget iMt = new InfMultiTarget();
		iMt.dataRead(network, isDuplica);  // read network structure
		iMt.setNodeset();       // all nodes
		iMt.ReadPropagate(propnetwork);  //set propagation probability
		iMt.info();
		
		for(int i = 1; i<=k; i++)
		{
			hMt.MiiaScoreArr(iMt, MonteCarloTimes, i);
			System.out.println(hMt.getTopKMiiaScore(i).toString());
			hMt.cleatMIIAScore();
		}*/
		endTime = System.currentTimeMillis();
		
		totalTime = endTime - startTime;
		
		System.out.println("Execution Time: " + totalTime/1000+" sec");
	}

}

