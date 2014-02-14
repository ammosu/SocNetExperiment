package SocExperiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class HeurMultiTarget {
	private double threshold = 0.1;
	private ArrayList<Integer> candidate = new ArrayList<Integer>();
	private Hashtable<Integer, Double[]> MiiaArrScore = new Hashtable<Integer, Double[]>();
	
	private Hashtable<Integer, Double> MIIAScore = new Hashtable<Integer, Double>();
	private ArrayList<Integer> targets = new ArrayList<Integer>();
	private double[] remainTargetScore; 
	private Hashtable<Integer, Double> Tscore = new Hashtable<Integer, Double>();
	
	
	public void mainProcess(ArrayList<Integer> target,int k, String network, String propnetwork, int MonteCarloTimes) throws IOException
	{
		InfMultiTarget iMt = new InfMultiTarget();
		
		boolean isDuplica = true;
		if(network.equals("Brightkite_edges.txt"))
			isDuplica = false;
		
		iMt.dataRead(network, isDuplica);  // read network structure
		iMt.setNodeset();       // all nodes
		iMt.ReadPropagate(propnetwork);  //set propagation probability
		iMt.info();
		
		this.targets = target;
		this.remainTargetScore = new double[this.targets.size()];
		for(int i = 0; i < this.targets.size(); i++)
		{
			double sum = 0.0;
			for(double p : iMt.getGraph().get(targets.get(i)).probability)
			{
				sum += p;
			}
			this.remainTargetScore[i] = sum;
		}
		MiiaScore(iMt, MonteCarloTimes);
		setTscore(MonteCarloTimes);
		
		//System.out.println(this.Tscore.toString());
		
		
		for(int i = 0; i < k; i++)
		{
			int key = new HeurSoc().maxKey(this.Tscore);
			System.out.print(key+" ");
			for(int j = 0; j < this.MiiaArrScore.get(key).length-1; j++)
				System.out.print(this.MiiaArrScore.get(key)[j]+",");
			System.out.println(this.MiiaArrScore.get(key)[this.MiiaArrScore.get(key).length-1]);
			subRTScore(this.MiiaArrScore.get(key));
			this.MiiaArrScore.remove(key);
			
			
			setTscore(MonteCarloTimes);
		}
		
		
	}
	
	
	public void subRTScore(Double[] doubles)
	{
		for(int i = 0; i < this.remainTargetScore.length; i++)
		{
			if(this.remainTargetScore[i] - doubles[i]>0)
				this.remainTargetScore[i] -= doubles[i];
			else
				this.remainTargetScore[i] = 0.0;
		}
	}
	
	public void setTscore(int MCtimes)
	{
		this.Tscore.clear();
		for(Map.Entry<Integer, Double[]> e : this.MiiaArrScore.entrySet())
		{
			double sum = 0.0;
			for(int i = 0; i< e.getValue().length; i++)
			{
				
				sum += this.remainTargetScore[i]*e.getValue()[i]/(double)MCtimes;
			}
			this.Tscore.put(e.getKey(), sum);
		}
	}
	public void setThreshold(double th)
	{
		this.threshold = th;
	}
	public void setTargets(ArrayList<Integer> targets)
	{
		this.targets = targets;
	}
	public void MiiaScore(InfMultiTarget imt, int monteCarloTimes)  
	{
		//ArrayList<Integer> allnodes = imt.getNodes(); // all nodes
		
		for(int i = 0; i < monteCarloTimes; i++)
		{
			imt.clearActResult();
			imt.createResult();
			for(int j = 0; j < this.targets.size();j++)
			{
				for(Map.Entry<Integer, Double> e : MIIAalg(this.targets.get(j), imt.getGraph()).entrySet())
				{
					if(!this.candidate.contains(e.getKey())) //<- if no such key
						this.candidate.add(e.getKey());
					if(this.MiiaArrScore.keySet().contains(e.getKey())) //<- already scoring
					{
						Double[] replaceArr = this.MiiaArrScore.get(e.getKey());
						replaceArr[j] += e.getValue();
						this.MiiaArrScore.put(e.getKey(), replaceArr);
					}
					else //<- not exist in hash
					{
						Double[] replaceArr = new Double[this.targets.size()];
						for(int a = 0; a<replaceArr.length;a++)
							replaceArr[a] = 0.0;
						replaceArr[j] += e.getValue();
						this.MiiaArrScore.put(e.getKey(), replaceArr);
					}
				}
				
			}
		}
	}
	public void MiiaScoreArr(InfMultiTarget imt, int monteCarloTimes)
	{
		
		imt.createResult();
		// for each monteCarlo process
		for(int i = 0; i < monteCarloTimes; i++)
		{//for each target
			for(int target : this.targets)
			{
				//Hashtable<Integer, Double> tScoreTable = new Hashtable<Integer, Double>();
				//ArrayList<Integer> tScore = new ArrayList<Integer>();
				MiiaScoreUpdate(MIIAalg(target, imt.getGraph()), (double)imt.getGraph().get(target).size());
				
			}
		}
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
	
	public void MiiaScoreUpdate(Hashtable<Integer, Double> scoreHash, double scale)
	{
		for(Map.Entry<Integer,Double> entry : scoreHash.entrySet())
		{
			if(this.MIIAScore.containsKey(entry.getKey())) // if key exist than plus 
			{
				double score = this.MIIAScore.get(entry.getKey());
				this.MIIAScore.remove(entry.getKey());
				this.MIIAScore.put(entry.getKey(), entry.getValue()*scale + score);  //update
			}
			else
				this.MIIAScore.put(entry.getKey(), entry.getValue()*scale);
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
		System.out.println("Heuristic 1 for multi-target");
		HeurMultiTarget hMt = new HeurMultiTarget();
		
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
		int k = 5; //default
		if(args.length >= 4)
			k = Integer.parseInt(args[3]);
		if(args.length >= 5)
			MonteCarloTimes = Integer.parseInt(args[4]);
	
		double startTime, endTime, totalTime;
		
		/*boolean isDuplica = true;
		if(network.equals("Brightkite_edges.txt"))
			isDuplica = false;*/
		
		System.out.println("Targets: "+Targets.toString());
		
		/*InfMultiTarget iMt = new InfMultiTarget();
		iMt.dataRead(network, isDuplica);  // read network structure
		iMt.setNodeset();       // all nodes
		iMt.ReadPropagate(propnetwork);  //set propagation probability
		iMt.info();*/
		
		startTime = System.currentTimeMillis();
		
		/*hMt.MiiaScoreArr(iMt, MonteCarloTimes);
		System.out.println(hMt.getTopKMiiaScore(k).toString());
		*/
		hMt.mainProcess(Targets, k, network, propnetwork, MonteCarloTimes);
		
		
		endTime = System.currentTimeMillis();
		
		totalTime = endTime - startTime;
		
		System.out.println("Execution Time: " + totalTime/1000+" sec");
	}

}