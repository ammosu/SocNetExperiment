package SocExperiment;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class LRandom {
	
	
	public void localTopK(Hashtable<Integer, Double> hash, int k)
	{
		
		ArrayList<Map.Entry<Integer, Double>> l = new ArrayList<Entry<Integer, Double>>(hash.entrySet());
	    Collections.sort(l, new Comparator<Map.Entry<Integer, Double>>()
	    		{
	    			public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) 
	    			{
	    				return o1.getValue().compareTo(o2.getValue());
	    			}
	    		});
	    
	    ArrayList<Integer> randomPool = new ArrayList<Integer>();
	    ArrayList<Integer> topKList =  new ArrayList<Integer>();
	    double max = l.get(l.size()-1).getValue();
	    
	    while (topKList.size()!=k)
	    {
	    	if(max==l.get(l.size()-1).getValue())
	    	{
	    		randomPool.add(l.get(l.size()-1).getKey()); // add max key to pool
	    		l.remove(l.size()-1); // remove max key from l
	    	}
	    	else if(topKList.size()+randomPool.size()<k)
	    	{
	    		max = l.get(l.size()-1).getValue();
	    		topKList.addAll(randomPool);
	    		randomPool.clear();
	    	}
	    	else
	    	{
	    		int rankey = 0;
	    		Random r = new Random();
	    		for(int i = 0; i < k-topKList.size(); i++)
	    		{
	    			rankey = randomPool.get(r.nextInt(randomPool.size()));
	    			topKList.add(rankey);
	    			randomPool.removeAll(topKList);
	    		}
	    	}
	    	
	    }
	    for(int i = 0; i < topKList.size()-1; i++)
	    	System.out.print(topKList.get(i)+",");
	    System.out.println(topKList.get(topKList.size()-1));
	    
	    //System.out.println(topKList.toString());
	    /*for(int i = 0; i < k; i++)
	       if(i!=k-1)
	    	   System.out.print(l.get(l.size()-i-1).getKey()+",");
	       else
	    	   System.out.println(l.get(l.size()-i-1).getKey());*/
	}
	
	public void randK(Hashtable<Integer, Double> hash, int k)
	{
		ArrayList<Integer> seed = new ArrayList<Integer>();
		Random r = new Random();
		ArrayList<Integer> keys = new ArrayList<Integer>(hash.keySet());
		int key;
		for(int i = 0; i < k; i++)
		{
			key = keys.get(r.nextInt(keys.size()));
			seed.add(key);
			keys.remove(seed);
		}
		System.out.println(seed.toString());
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//initialize targets
		
		String TargetStr = "27210, 25704, 22883, 32792, 21773, 38395, 20859, 44903, 48409, 20506"; //default target
		ArrayList<Integer> Targets = new ArrayList<Integer>();
		String[] str = TargetStr.split(", ");
		for(int i = 0; i <str.length; i++)
			Targets.add(Integer.parseInt(str[i]));
		
		
		if(args.length >= 1)
		{
			Targets.clear();
			String s = args[0];
			for(String a : s.split(","))
				Targets.add(Integer.parseInt(a));
		}
		
		String network = "Brightkite_edges.txt" , propnetwork = "Brightkite_edges_TV2.txt"; //default data
		
		if(args.length >= 2)
			network = args[1];
		if(args.length >= 3)
			propnetwork = args[2];
		int k = 5; //default
		if(args.length >= 4)
			k = Integer.parseInt(args[3]);
		
		
		double startTime, endTime, totalTime;
		
		boolean isDuplica = true;
		if(network.equals("Brightkite_edges.txt"))
			isDuplica = false;
		
		//System.out.println("Targets: "+Targets.toString());
		
		System.out.println("Network: "+propnetwork);
		
		
		InfMultiTarget iMt = new InfMultiTarget();
		
		iMt.dataRead(network, isDuplica);  // read network structure
		iMt.setNodeset();       // all nodes
		//iMt.test();
		
		
		/**/
		iMt.ReadPropagate(propnetwork);  //set propagation probability
		iMt.info();
		
		//Targets = iMt.RandomTargets(5, 20);
		
		iMt.showInformation(Targets);  // show targets information
		
		startTime = System.currentTimeMillis();
		
		//ArrayList<Integer> seeds = new ArrayList<Integer>();
		
		Hashtable<Integer, MonteCarlo> graph = iMt.getGraph();
		Hashtable<Integer, Double> score = new Hashtable<Integer, Double>();
		for(int target : Targets)
		{
			ArrayList<Integer> idList = graph.get(target).neighborID;
			ArrayList<Double> probList = graph.get(target).probability;
			for(int i = 0; i<idList.size(); i++)
			{
				if(!score.containsKey(idList.get(i)))
					score.put(idList.get(i), probList.get(i));
				else if(score.get(idList.get(i))<probList.get(i))
				{
					score.put(idList.get(i), probList.get(i));
				}
				
			}
		}
		//System.out.println("Score size: "+score.size());
		if(k > score.size())
			k = score.size();
		
		LRandom lrd = new LRandom();
		lrd.localTopK(score, k);
		//lrd.randK(score, k);
		
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Execution Time: " + totalTime/1000+" sec");
		
		//evaluation
		
		startTime = System.currentTimeMillis();

	}

}
