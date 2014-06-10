package SocExperiment;

import java.util.*;
import java.io.*;

public class InfMultiTarget {

	private Hashtable<Integer, MonteCarlo> Graph = new Hashtable<Integer, MonteCarlo>(); // Graph
	private ArrayList<Integer> nodeSet = new ArrayList<Integer>();  // all nodes
	private ArrayList<Integer> seedSet = new ArrayList<Integer>();  // seeds
	private Set<Integer> candidate = new HashSet<Integer>();
	private Hashtable<Integer, Set<Integer>> BFSresult = new Hashtable<Integer, Set<Integer>>(); // all possible influence candidate (nbr -> candidate)
	private ArrayList<Hashtable<Integer, Set<Integer>>> SingleTargetTables = new ArrayList<Hashtable<Integer, Set<Integer>>>(); // list of (nbr -> candidate)
	private Hashtable<String, ArrayList<Hashtable<Integer, Set<Integer>>>> MultiTargetTables = new Hashtable<String, ArrayList<Hashtable<Integer, Set<Integer>>>>(); //target -> list(nbr -> candidate)
	private Hashtable<Integer, Integer> totalAcceptanceTimes_MC = new Hashtable<Integer, Integer>();  // node id -> total acceptance times in # MC times 
	private ArrayList<Integer> targetList = new ArrayList<Integer>();
	
	public void setSeed(ArrayList<Integer> seeds)
	{
		this.seedSet = seeds;
	}
	
	public Hashtable<Integer, MonteCarlo> getGraph()
	{
		return this.Graph;
	}
	
	public ArrayList<Integer> getNodes()
	{
		return this.nodeSet;
	}
	
	public void targetReader(String targetfile) throws IOException
	{
		FileReader fr;
		try {
			fr = new FileReader(targetfile);
			BufferedReader br = new BufferedReader(fr);
			while(br.ready())
			{
				this.targetList.add(Integer.parseInt(br.readLine()));
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public void ReadPropagate(String propfile) throws IOException  // read propagate file and write to propagate graph
	{
		FileReader fr;
		try {
			fr = new FileReader(propfile);
			BufferedReader br = new BufferedReader(fr);
			
			while(br.ready())
			{
				ArrayList<Double> value = new ArrayList<Double>();
				String s = br.readLine();
				String str[] = s.split(" ")[1].split(","); //probability
				for(int i = 0; i < str.length; i++)
					value.add(Double.parseDouble(str[i]));
				this.Graph.get(Integer.parseInt(s.split(" ")[0])).probability = value;
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void ReadPropagate(String propfile, int i) // read matlab created power-law file
	{
		double d = 0.0;
		String fileName = propfile;
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String line;
			
			while((line = br.readLine())!=null){
				d = Double.parseDouble(line);
				
				if(this.Graph.get(this.nodeSet.get(i)).addProb(d)==-1) //probability full
					i++;
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void ReadPropgate_WC(String propfile) throws IOException
	{
		for(int node : this.nodeSet)
		{
			int size = this.Graph.get(node).neighborID.size();
			for(int i = 0; i < size; i++)
				this.Graph.get(node).probability.add(1/(double)(size));
		}
	}

	public void dataRead(String Filename, boolean isDuplication) //Read text and build network
	{
		int lineCount = 0;
		FileReader FileStream;
		try {
			FileStream = new FileReader(Filename);
			BufferedReader BufferedStream = new BufferedReader(FileStream);
			try {
				do{
					
					String readline = BufferedStream.readLine();
					String[] readlines = readline.split("\t"); // format: ID\tNbrID
					
					lineCount ++ ;
					
					int ID = Integer.parseInt(readlines[0]);
					int NbrID = Integer.parseInt(readlines[1]);
					/*if(!this.nodeSet.contains(ID))
						this.nodeSet.add(ID);*/
					if(isDuplication)
						this.putUpdate(ID, NbrID);
					else
						this.putUpdateNoduplication(ID, NbrID);
				}
				while(BufferedStream.ready());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Read Fininsh!!\n total lines:"+lineCount); /**/
	}
	
	public void putUpdate(Integer key, Integer value) // for all lines put or update hash
	{
		ArrayList<Integer> updatelist;
		if(this.Graph.containsKey(key)) //contain key -> update key
		{
			updatelist = new ArrayList<Integer>();
			updatelist = this.Graph.get(key).neighborID;
			updatelist.add(value);
			this.Graph.remove(key);
			this.Graph.put(key, new MonteCarlo(updatelist));
			
			if(this.Graph.containsKey(value)) // contain value -> update value
			{
				updatelist = new ArrayList<Integer>();
				updatelist = this.Graph.get(value).neighborID;
				updatelist.add(key);
				this.Graph.remove(value);
				this.Graph.put(value, new MonteCarlo(updatelist));
			}
			else  // no value -> create value
			{
				updatelist = new ArrayList<Integer>();
				updatelist.add(key);
				this.Graph.remove(value);
				this.Graph.put(value, new MonteCarlo(updatelist));
			}
		}
		else if(!this.Graph.containsKey(value)) //no key -> put key value & no value -> put value key
		{
			updatelist = new ArrayList<Integer>();
			updatelist.add(key);
			this.Graph.put(value, new MonteCarlo(updatelist));
			updatelist = new ArrayList<Integer>();
			updatelist.add(value);
			this.Graph.put(key, new MonteCarlo(updatelist));
		}
		else	//no key -> put key value & update value
		{
			updatelist = new ArrayList<Integer>();
			updatelist.add(value);
			this.Graph.put(key, new MonteCarlo(updatelist));
			updatelist = new ArrayList<Integer>();
			updatelist = this.Graph.get(value).neighborID;
			updatelist.add(key);
			this.Graph.remove(value);
			this.Graph.put(value, new MonteCarlo(updatelist));
			
		}
	}
	
	public void putUpdateNoduplication(Integer key, Integer value)
	{
		ArrayList<Integer> updatelist;
		if(this.Graph.containsKey(key)) //contain key -> update key
		{
			updatelist = new ArrayList<Integer>();
			updatelist = this.Graph.get(key).neighborID;
			updatelist.add(value);
			this.Graph.remove(key);
			this.Graph.put(key, new MonteCarlo(updatelist));
		}
		else	//no key -> put key value 
		{
			updatelist = new ArrayList<Integer>();
			updatelist.add(value);
			this.Graph.put(key, new MonteCarlo(updatelist));
		}
	}
	public void setNodeset()  //set node set from hashtable
	{
		Set<Integer> set = this.Graph.keySet();
		Iterator<Integer> it = set.iterator();
		
		while(it.hasNext())
		{
			Integer key = it.next();
			this.nodeSet.add(key);
		}
		Collections.sort(this.nodeSet);  //sort
	}
	
	
	public void bfsProcess(int nodeID, int targetID, int maxSteps) // neighbor
	{
		Set<Integer> bfsNodes = new HashSet<Integer>();
		Set<Integer> now = new HashSet<Integer>();
		Set<Integer> next = new HashSet<Integer>();
		bfsNodes.add(nodeID); //neighbor
		bfsNodes.add(targetID); // target
		now.add(nodeID);
		//System.out.println(".");
		int stepcount = 1;
		if(maxSteps != -1)
			while(now.size() != 0 && stepcount < maxSteps)
			{
				stepcount++ ;
				Iterator<Integer> it = now.iterator(); //current layer
				
				while(it.hasNext())
				{
					next.addAll(this.Graph.get(it.next()).activeNbr()); // next layer
				}
				next.removeAll(bfsNodes); // remember all node from next layer
				next.remove(targetID); // don't remember target node
				now.clear(); 
				now.addAll(next);
				bfsNodes.addAll(next);
				next.clear();
			}
		else
			while(now.size() != 0)
			{
				stepcount++ ;
				Iterator<Integer> it = now.iterator(); //current layer
				
				while(it.hasNext())
				{
					next.addAll(this.Graph.get(it.next()).activeNbr()); // next layer
				}
				next.removeAll(bfsNodes); // remember all node from next layer
				next.remove(targetID); // don't remember target node
				now.clear(); 
				now.addAll(next);
				bfsNodes.addAll(next);
				next.clear();
			}
		bfsNodes.remove(targetID);
		this.candidate.addAll(bfsNodes);
		if(bfsNodes.size()!=0)
			this.BFSresult.put(nodeID, bfsNodes);
		//System.out.println("Neighbor No. "+ nodeID + " Size: " + bfsNodes.size());
		this.SingleTargetTables.add(this.BFSresult);
		this.BFSresult = new Hashtable<Integer, Set<Integer>>();
	}
	
	
	public double accTimes(int targetID) // calculate acceptance times for targetID
	{
		double times = 0.0;
		Set<Integer> seed = new HashSet<Integer>();
		for(int i = 0; i<this.seedSet.size();i++)
			seed.add(this.seedSet.get(i));
		Set<Integer> nbrs = new HashSet<Integer>();
		nbrs.addAll(this.Graph.get(targetID).activeNbr());
		Iterator<Integer> iter = nbrs.iterator();
		while(iter.hasNext())
		{
			int Nbr = iter.next();
			if(hasIntersection(this.BFSresult.get(Nbr),seed))
				times += 1.0;
		}
		
		return times;
		
	}
	public static <T> boolean hasIntersection(Set<T> setA, Set<T> setB) 
	{
		if(setA.size() > setB.size())
		{
			for (T x : setB)
				if (setA.contains(x))
					return true;
			return false;
		}
		else
		{
			for (T x : setA)
				if (setB.contains(x))
					return true;
			return false;
		}
	}
	public ArrayList<Double> createRandomDouble(int arraysize)   // create random double arraylist
	{
		ArrayList<Double> ranArr = new ArrayList<Double>();
		for(int i = 0; i < arraysize; i++)
			ranArr.add(Math.random());
		return ranArr;
	}
	public void clearActResult() // clear all active result from Graph
	{
		for(int i = 0; i < this.Graph.size(); i++)
		{
			this.Graph.get(this.nodeSet.get(i)).actResult.clear();
			this.Graph.get(this.nodeSet.get(i)).actResult.trimToSize();
		}
	}
	public void createResult() // create a random activated result 
	{
		int tablesize = this.nodeSet.size();
		ArrayList<Double> propArr;
		ArrayList<Double> propResult;
		ArrayList<Boolean> resultEdge;
		for(int i = 0; i < tablesize; i++)
		{
			propArr = new ArrayList<Double>();    // original probability
			propResult = new ArrayList<Double>(); // random number
			resultEdge = new ArrayList<Boolean>();// propagate success
			propArr = this.Graph.get(this.nodeSet.get(i)).probability;
			propResult = createRandomDouble(propArr.size());
			for(int j = 0; j < propArr.size(); j++)
			{
				if(propArr.get(j)>propResult.get(j))
					resultEdge.add(true);  //smaller than propagation probability
				else
					resultEdge.add(false); //bigger
			}
			this.Graph.get(this.nodeSet.get(i)).actResult = resultEdge;
			this.Graph.get(this.nodeSet.get(i)).actResult.trimToSize();
		}
	}
	public void createBinaryResult() // create a random activated result (independent with propagation probability)
	{
		int tablesize = this.nodeSet.size();
		ArrayList<Double> propArr;
		ArrayList<Double> propResult;
		ArrayList<Boolean> resultEdge;
		for(int i = 0; i < tablesize; i++)
		{
			propArr = new ArrayList<Double>();
			propResult = new ArrayList<Double>();
			resultEdge = new ArrayList<Boolean>();
			propArr = this.Graph.get(this.nodeSet.get(i)).probability;
			propResult = createRandomDouble(propArr.size());
			for(int j = 0; j < propArr.size(); j++)
			{
				if(0.5>propResult.get(j))
					resultEdge.add(true);  //smaller than propagation probability
				else
					resultEdge.add(false); //bigger
			}
			
			
			this.Graph.get(this.nodeSet.get(i)).actResult = resultEdge;
			this.Graph.get(this.nodeSet.get(i)).actResult.trimToSize();
		}
	}
	public void connectedTable(int targetID, int maxSteps) //target nbr bfs process
	{
		ArrayList<Integer> nbrs = new ArrayList<Integer>();
		Set<Integer> actNbr = new HashSet<Integer>();
		actNbr.addAll(this.Graph.get(targetID).activeNbr());
		Iterator<Integer> iter = actNbr.iterator();
		while(iter.hasNext())
			nbrs.add(iter.next());  // neighbor add to nbrs(not duplicate)
		
		for(int i = 0; i< nbrs.size(); i++)
			bfsProcess(nbrs.get(i), targetID, maxSteps);
	}
	
	public void createBFSTables(int times, ArrayList<Integer> targets, int maxSteps) // Pre-Compute connected nodes for all Monte Carlo result
	{
		double startTime, endTime, totalTime, startTime2 = 0.0, endTime2 = 0.0;
		startTime = System.currentTimeMillis();
		System.out.println("Create Tables");
		for(int i = 0; i < times; i++)
		{
			clearActResult();
			createResult();
			this.BFSresult.clear();
			if(i==0)
				startTime2 = System.currentTimeMillis();
			
			for(int target : targets)
			{
				connectedTable(target, maxSteps);  // for all targets create connected tables
				this.MultiTargetTables.put(Integer.toString(target)+"-"+Integer.toString(i), this.SingleTargetTables); //target-i => list of nbr->bfs
				this.SingleTargetTables = new ArrayList<Hashtable<Integer, Set<Integer>>>();
			}
			
			if(i==0)
			{
				endTime2 = System.currentTimeMillis();
			}
			
			//this.BFSresult = new Hashtable<Integer, Set<Integer>>();
			
			if(i==0)
				System.out.println("\nCreate the first table spend " + (endTime2 - startTime2)/1000);
			/**/
			if(i!=times-1)
				System.out.print(i+", ");
			else
				System.out.print(i);
		}
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("\nCreate All Tables Time: " + totalTime/1000+" sec");
		
	}

	public double MC_expectedTimes() //average result (using pre-computing bfs result)
	{
		double expectAccTimes = 0.0;
		
		if(this.MultiTargetTables.size()==0)
		{
			System.out.println("BFS tables do not build");
			return 0.0;
		}
		
		for(String mKey : this.MultiTargetTables.keySet()) //string mKey = target,MonteCarloTimes
		{
			int nbrsize = this.MultiTargetTables.get(mKey).size();
			for(int i = 0; i< nbrsize; i++)
			{
				  // nbr->candidate
				 for(int j : this.MultiTargetTables.get(mKey).get(i).keySet())
				 {
					 Set<Integer> a = new HashSet<Integer>();
					 a.addAll(this.seedSet);
					 if(hasIntersection(a ,this.MultiTargetTables.get(mKey).get(i).get(j)))
						 expectAccTimes+=1.0;
						 
				 }
			}
			//expectAccTimes += nConnectNbr(this.BFSTables.get(j));
		}
		
		return expectAccTimes;
	}
	
	public ArrayList<Integer> greedy(int top_k, ArrayList<Integer> targets, int times, int maxSteps) // greedy algorithm (remember all BFS results)
	{
		ArrayList<Integer> seeds = new ArrayList<Integer>(); // found seed
		ArrayList<Integer> tempSeed = new ArrayList<Integer>();  // add a candidate to seeds 
		
		
		createBFSTables(times, targets, maxSteps);
		//showHash();
		
		double maxValue = 0.0;
		int maxNodeID = -1;
		while(seeds.size() < top_k)
		{
			double startTime, endTime;
			startTime = System.currentTimeMillis();
			for(int cand : this.candidate)
			{
				if(targets.contains(cand) || seeds.contains(cand)) //don't care ID = target or ID in seeds
					continue;
				tempSeed.clear();
				tempSeed.addAll(seeds);
				//if(!seeds.contains(this.nodeSet.get(i))) //except and seeds
				
				tempSeed.add(cand);
				
				setSeed(tempSeed); //edit seed
				double i_value = MC_expectedTimes()/(double)times; //get value
				
				if(i_value > maxValue) // record max value, id
				{
					maxValue = i_value;
					maxNodeID = cand;
				}
					
			}
			System.out.print(".");
			seeds.add(maxNodeID);
			if(seeds.size()!=top_k)
				System.out.println("\nmiddle result: "+maxValue);
			if(seeds.size()==top_k)
				System.out.println("\nExpected times:"+maxValue);
			maxNodeID = -1;
			maxValue = 0.0;
			endTime = System.currentTimeMillis();
			System.out.println("\n"+seeds.size()+"-Seed time: "+(endTime-startTime)/1000 +" sec");
		}
		return seeds;
		
	}
	
	
	public void info() // print information of this network
	{
		System.out.println("----Graph Information----");
		System.out.println("Node Size: "+this.nodeSet.size());
		int edgesize = 0;
		for(int i = 0; i<this.nodeSet.size();i++)
		{
			edgesize += this.Graph.get(this.nodeSet.get(i)).size();
		}
		System.out.println("Edge Size: "+ edgesize);
		System.out.println("-------------------------");
	}
	public void showInformation(ArrayList<Integer> Targets)
	{
		double Max = 0.0;
		int totalNbrs = 0;
		for(int target : Targets)
		{
			ArrayList<Integer> nbrs = this.Graph.get(target).neighborID;
			ArrayList<Double> nbrProb = this.Graph.get(target).probability;
			double sum = 0.0;
			
			for(double prob : nbrProb)
			{
				sum += prob;
			}
			Max += sum;
			totalNbrs += nbrs.size();
			System.out.println("Target ID: "+target+"\tSize: "+nbrs.size()+"\nMaximum Probability: "+sum+"\n");//+"\nNeighbors: "+nbrs.toString()
		}
		System.out.println("Total Neighbor size: "+ totalNbrs);
		System.out.println("Upper Bound: "+ Max);
		
	}
	public ArrayList<Integer> RandomTargets(int leastNum, int targetSize)
	{
		ArrayList<Integer> randomTargets = new ArrayList<Integer>();
		Random r = new Random();
		for(int i = 0; i < targetSize; i++)
		{
			//System.out.println(this.nodeSet.get(r.nextInt(this.nodeSet.size())));
			int target = this.nodeSet.get(r.nextInt(this.nodeSet.size()));
			if(this.Graph.get(target).size()<=leastNum)
				i--;
			else
				randomTargets.add(target);
		}
		
		return randomTargets;
	}
	public String seqEvaluetion(ArrayList<Integer> seed, ArrayList<Integer> seed2, int targetNbrID, int target, int maxSteps)
	{
		
		Set<Integer> tracedNode = new HashSet<Integer>();
		Set<Integer> nowArr = new HashSet<Integer>();
		Set<Integer> nextNodeArr = new HashSet<Integer>();
				
		nowArr.add(targetNbrID);  // target(neighbors)
		tracedNode.add(targetNbrID);  // remember targetNbr
		tracedNode.add(target);       // remember target
		
		int firstInfluenceNode = seed.size();
		int firstInfluenceNode2 = seed2.size();
		int stepIndex = 1;
		
		for(int j = 0; j < firstInfluenceNode; j++)
		{
			if(tracedNode.contains(seed.get(j))) // which i that seed(i) is connected to nbr
			{
				firstInfluenceNode = j;
			}
		}
		for(int j2 = 0; j2 < firstInfluenceNode2; j2++)
		{
			if(tracedNode.contains(seed2.get(j2))) // which i that seed(i) is connected to nbr
			{
				firstInfluenceNode2 = j2;
			}
		}
		
		while(nowArr.size()!=0 && stepIndex < maxSteps)
		{
			stepIndex++;
			Iterator<Integer> iter = nowArr.iterator(); //next BFS level array
			while(iter.hasNext())
			{
				nextNodeArr.addAll(this.Graph.get(iter.next()).activeNbr());
			}
			
			nextNodeArr.removeAll(tracedNode);
			
			nowArr.clear();
			nowArr.addAll(nextNodeArr);
			tracedNode.addAll(nextNodeArr);
			
			for(int j = 0; j < firstInfluenceNode; j++)
			{
				if(tracedNode.contains(seed.get(j))) // which i that seed(i) is connected to nbr
				{
					firstInfluenceNode = j;
				}
			}
			for(int j2 = 0; j2 < firstInfluenceNode2; j2++)
			{
				if(tracedNode.contains(seed2.get(j2))) // which i that seed2(i) is connected to nbr
				{
					firstInfluenceNode2 = j2;
				}
			}
			if(firstInfluenceNode==0 && firstInfluenceNode2==0)  // if target influenced by the first seed
				break;
			
			nextNodeArr.clear();
		}
		
		if(firstInfluenceNode==seed.size())
			firstInfluenceNode = -1;
		if(firstInfluenceNode2==seed2.size())
			firstInfluenceNode2 = -1;
		return firstInfluenceNode+","+firstInfluenceNode2;
		
	}
	public void acceptanceEvaluation(ArrayList<Integer> targets, int MonteCarloTimes, ArrayList<Integer> seed1, ArrayList<Integer> seed2, int steps)
	{
		double[] acceptanceTimes1 = new double[seed1.size()]; 
		double[] acceptanceTimes2 = new double[seed2.size()]; 
		
		if(MonteCarloTimes <= 0)
			System.out.println("Number Setting Wrong");
		if(!this.nodeSet.containsAll(targets))
			System.out.println("No such target ID");
		double time = (double)MonteCarloTimes;
		
		for(int i = 0; i < MonteCarloTimes; i++)
		{
			clearActResult();
			createResult();
			
			//System.out.println("Neighbors group "+i+": "+this.Graph.get(targetNode).activeNbr().toString());//check
			for(int m = 0; m < targets.size();m++)
			for(int nbr : this.Graph.get(targets.get(m)).activeNbr())
			{
				String[] kStr = seqEvaluetion(seed1, seed2, nbr, targets.get(m), steps).split(","); 
				int k = Integer.parseInt(kStr[0]);
				int k2 = Integer.parseInt(kStr[1]);
				if(k!=-1)
				{
					for(int j = k; j < seed1.size() ;j++)
					{
						acceptanceTimes1[j]+=1.0;
					}
				}
				if(k2!=-1)
				{
					for(int j = k2; j < seed2.size() ;j++)
					{
						acceptanceTimes2[j]+=1.0;
					}
				}
			}
			if(i%100 ==0 )
				System.out.print(".");
			
		}
		System.out.print("\n");
		for(int j = 0; j < seed1.size() ;j++)
		{
			acceptanceTimes1[j] /= time ;
			if(j!=seed1.size()-1)
				System.out.print(acceptanceTimes1[j]+", ");
			else
				System.out.print(acceptanceTimes1[j]+"\n");
		}
		System.out.print("\n");
		for(int j = 0; j < seed2.size() ;j++)
		{
			acceptanceTimes2[j] /= time ;
			if(j!=seed2.size()-1)
				System.out.print(acceptanceTimes2[j]+", ");
			else
				System.out.print(acceptanceTimes2[j]+"\n");
		}

	}
	public int seqEvaluetion(ArrayList<Integer> seed, int targetNbrID, int target, int maxSteps)
	{
		
		Set<Integer> tracedNode = new HashSet<Integer>();
		Set<Integer> nowArr = new HashSet<Integer>();
		Set<Integer> nextNodeArr = new HashSet<Integer>();
				
		nowArr.add(targetNbrID);  // target(neighbors)
		tracedNode.add(targetNbrID);  // remember targetNbr
		tracedNode.add(target);       // remember target
		
		int firstInfluenceNode = seed.size();
		int stepIndex = 1;
		
		for(int j = 0; j < firstInfluenceNode; j++)
		{
			if(tracedNode.contains(seed.get(j))) // which i that seed(i) is connected to nbr
			{
				firstInfluenceNode = j;
			}
		}
		
		
		while(nowArr.size()!=0 && stepIndex < maxSteps)
		{
			stepIndex++;
			for(int j = 0; j < firstInfluenceNode; j++)
			{
				if(tracedNode.contains(seed.get(j))) // which i that seed(i) is connected to nbr
				{
					firstInfluenceNode = j;
				}
			}
			Iterator<Integer> iter = nowArr.iterator(); //next BFS level array
			while(iter.hasNext())
			{
				nextNodeArr.addAll(this.Graph.get(iter.next()).activeNbr());
			}
			
			nextNodeArr.removeAll(tracedNode);
			
			nowArr.clear();
			nowArr.addAll(nextNodeArr);
			tracedNode.addAll(nextNodeArr);
			
			for(int j = 0; j < firstInfluenceNode; j++)
			{
				if(tracedNode.contains(seed.get(j))) // which i that seed(i) is connected to nbr
				{
					firstInfluenceNode = j;
				}
			}
			if(firstInfluenceNode==0)  // if target influenced by the first seed
				break;
			
			nextNodeArr.clear();
		}
		
		if(firstInfluenceNode!=seed.size())
			return firstInfluenceNode;
		else
			return -1;
	}
	public void acceptanceEvaluation(ArrayList<Integer> targets, int MonteCarloTimes, ArrayList<Integer> seed, int steps)
	{
		double[] acceptanceTimes = new double[seed.size()];
		if(MonteCarloTimes <= 0)
			System.out.println("Number Setting Wrong");
		if(!this.nodeSet.containsAll(targets))
			System.out.println("No such target ID");
		
		for(int i = 0; i < MonteCarloTimes; i++)
		{
			clearActResult();
			createResult();
			
			//System.out.println("Neighbors group "+i+": "+this.Graph.get(targetNode).activeNbr().toString());//check
			for(int m = 0; m < targets.size();m++)
				for(int nbr : this.Graph.get(targets.get(m)).activeNbr())
				{
				
					int k = seqEvaluetion(seed, nbr, targets.get(m), steps);
				
					if(k!=-1) //intersection not empty
					{
						for(int j = k; j < seed.size() ;j++)
						{
							acceptanceTimes[j]+=1.0;
						}
					}
				}
			if(i%100 ==0 )
				System.out.print(".");
			
		}
		System.out.print("\n");
		for(int j = 0; j < seed.size() ;j++)
		{
			acceptanceTimes[j] /= (double)MonteCarloTimes ;
			if(j!=seed.size()-1)
				System.out.print(acceptanceTimes[j]+", ");
			else
				System.out.print(acceptanceTimes[j]+"\n");
		}
		System.out.print("\n");
		
	}
	/**
	 *    1            2               3              4             5     
	 * TargetIDs  network Path Propagation Path  Seed size k MonteCarlo Times
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int MonteCarloTimes = 10000;
		int maxSteps = 5;
		
		ArrayList<Integer> Targets = new ArrayList<Integer>();
		
		String TargetStr = "0,1,269,304,23671,178,47340,2675,119672,246255"; //default target
		String[] str = TargetStr.split(",");
		for(int i = 0; i <str.length; i++)
			Targets.add(Integer.parseInt(str[i]));
		
		
		if(args.length >= 1)
		{
			Targets.clear();
			String s = args[0];
			for(String a : s.split(","))
				Targets.add(Integer.parseInt(a));
		}
		
		String network = "com-dblp.ungraph.txt" , propnetwork = "prop_dblp_8020"; //default data
		
		if(args.length >= 2)
			network = args[1];
		if(args.length >= 3)
			propnetwork = args[2];
		int k = 5; //default
		if(args.length >= 4)
			k = Integer.parseInt(args[3]);
		if(args.length >= 5)
			MonteCarloTimes = Integer.parseInt(args[4]);
		if(args.length >= 6)
			maxSteps = Integer.parseInt(args[5]);
		
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
		iMt.ReadPropagate(propnetwork, 0);  //set propagation probability
		iMt.info();
		
		//Targets = iMt.RandomTargets(5, 20);
		
		iMt.showInformation(Targets);  // show targets information
		
		System.out.println("\nTargets: "+Targets.toString()+"\nMax step: "+maxSteps);
		
		startTime = System.currentTimeMillis();
		
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		
		seeds = iMt.greedy(k, Targets, MonteCarloTimes, maxSteps);
		System.out.println("\nGreedy algorithm:\n"+"Seed: " + seeds.toString());
		
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Execution Time: " + totalTime/1000+" sec");
		
		//evaluation
		
		startTime = System.currentTimeMillis();
		
	}

}
