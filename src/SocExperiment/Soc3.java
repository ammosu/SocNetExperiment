package SocExperiment;
import java.util.*;
import java.io.*;


public class Soc3 {

	private Hashtable<Integer, MonteCarlo> Graph = new Hashtable<Integer, MonteCarlo>(); // Graph
	private ArrayList<Integer> nodeSet = new ArrayList<Integer>();  // all nodes
	private ArrayList<Integer> seedSet = new ArrayList<Integer>();  // seeds
	private Hashtable<Integer, Set<Integer>> BFSresult = new Hashtable<Integer, Set<Integer>>(); // all possible influence path 
	private Hashtable<Integer, Hashtable<Integer, Set<Integer>>> BFSTables = new Hashtable<Integer, Hashtable<Integer, Set<Integer>>>(); 
	
	Hashtable<Integer, MonteCarlo> getGraph()
	{
		return Graph;
	}
	
	public ArrayList<Integer> getNodeSet()
	{
		return this.nodeSet;
	}
	public ArrayList<Integer> getNeibh(Integer a)
	{
		return this.Graph.get(a).neighborID;
	}
	
	public ArrayList<Double> getNeibhInEdge(Integer a)
	{
		return this.Graph.get(a).in_Edge;
	}
	
	public ArrayList<Double> getNeibhPropGraph(Integer a)
	{
		return this.Graph.get(a).probability;
	}
	
	public void setSeed(ArrayList<Integer> seeds)
	{
		this.seedSet = seeds;
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
				String str[] = s.split(" ")[1].split(",");
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
	

	public void WritePropagate(String propfile) // write propagate graph to file
	{
		FileWriter fw;
		try {
			fw = new FileWriter(propfile);
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i = 0; i < this.Graph.size();i++)
			{
				bw.write(this.nodeSet.get(i)+" ");
				for(int j = 0; j < this.Graph.get(this.nodeSet.get(i)).size()-1;j++)
					bw.write(this.Graph.get(this.nodeSet.get(i)).probability.get(j)+",");
				bw.write(this.Graph.get(this.nodeSet.get(i)).probability.get(this.Graph.get(this.nodeSet.get(i)).probability.size()-1).toString());
				bw.newLine();
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void WritePropagateUniform(String propfile) // write propagate graph to file
	{
		FileWriter fw;
		try {
			fw = new FileWriter(propfile);
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i = 0; i < this.nodeSet.size();i++)
			{
				bw.write(this.nodeSet.get(i)+" ");
				for(int j = 0; j < this.Graph.get(this.nodeSet.get(i)).probability.size()-1;j++)
					bw.write(1.0/(double)this.Graph.get(this.nodeSet.get(i)).size()+",");
				bw.write(1.0/(double)this.Graph.get(this.nodeSet.get(i)).size()+"\n");
				//bw.newLine();
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	public void WritePropagateTriValue(String propfile) // write propagate graph to file
	{
		double[] tri = {0.1, 0.01, 0.001};
		Random ran = new Random();
		FileWriter fw;
		try {
			fw = new FileWriter(propfile);
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i = 0; i < this.nodeSet.size();i++)
			{
				bw.write(this.nodeSet.get(i)+" ");
				for(int j = 0; j < this.Graph.get(this.nodeSet.get(i)).probability.size()-1;j++)
					bw.write(tri[ran.nextInt(3)]+",");
				bw.write(tri[ran.nextInt(3)]+"\n");
				//bw.newLine();
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public Set<Integer> activatedNbr(int nodeID) //activated neighbor
	{
		ArrayList<Integer> a = this.Graph.get(nodeID).activeNbr();
		Set<Integer> arr = new HashSet<Integer>();
		for(int i : a)
		{
			arr.add(i);
		}
		/*for(int i = 0 ; i< this.Graph.get(nodeID).size(); i++)
		{
			if(this.Graph.get(nodeID).actResult.get(i) == true)
			{
				//arr.add(this.GraphTable.get(nodeID).get(i));
				arr.add(this.Graph.get(nodeID).neighborID.get(i));
			}
		}*/
		return arr;
	}
	
	//set operation


	public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) 
	{
		Set<T> tmp = new HashSet<T>();
		for (T x : setA)
			if (setB.contains(x))
				tmp.add(x);
		return tmp;
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

	
	public boolean isConnectTrace(int nodeID) // is node connected to seeds
	{
		Set<Integer> seed = new HashSet<Integer>();
		for(int i = 0; i< this.seedSet.size();i++)  //seed from arraylist to set 
			seed.add(this.seedSet.get(i));
		
		Set<Integer> tracedNode = new HashSet<Integer>();
		
		Set<Integer> lastNodeArr = new HashSet<Integer>();
		Set<Integer> currentNodeArr = new HashSet<Integer>();
		lastNodeArr.add(nodeID);  // target(neighbors)
		tracedNode.add(nodeID);  // remember target
		
		while(true)
		{
			tracedNode.addAll(currentNodeArr); // remember all traced nodes
			
			
			Iterator<Integer> iter = lastNodeArr.iterator(); //element of lastnodearr
			while(iter.hasNext())
			{
				currentNodeArr.addAll(activatedNbr(iter.next()));
			}
			
			currentNodeArr.removeAll(tracedNode);
			if(hasIntersection(seed, currentNodeArr)) //is connected
				break;
			if(currentNodeArr.size()==0)
				break;
			lastNodeArr.clear();
			lastNodeArr.addAll(currentNodeArr);
			
		}
		if(hasIntersection(seed ,currentNodeArr))
			return true;
		else
			return false;
	}
	
	
	public void connectedTable(int targetID) //target nbr bfs process
	{
		ArrayList<Integer> nbrs = new ArrayList<Integer>();
		Set<Integer> actNbr = new HashSet<Integer>();
		actNbr = activatedNbr(targetID);
		Iterator<Integer> iter = actNbr.iterator();
		while(iter.hasNext())
			nbrs.add(iter.next());  // neighbor add to nbrs(not duplicate)
		
		for(int i = 0; i< nbrs.size(); i++)
			bfsProcess(nbrs.get(i), targetID);
		//System.out.println("-------------Done~---------------");
	}
	
	/*public boolean isConnected(int nodeID, int targetNbrID)
	{
		//Set<Integer> nbr = new HashSet<Integer>();
		if(this.BFSresult.get(targetNbrID).contains(nodeID))
			return true;
		else
			return false;
	}*/
	
	public double nConnectNbr(Hashtable<Integer, Set<Integer>> hash) //number of neighbors that seed can be connected 
	{
		double number = 0.0;
		Set<Integer> seed = new HashSet<Integer>();
		for(int i = 0; i<this.seedSet.size();i++)
			seed.add(this.seedSet.get(i));
		Iterator<Integer> it = hash.keySet().iterator();
		
		while(it.hasNext())  // for each neighbor
		{
			int i = it.next();
			if(hasIntersection(hash.get(i),seed))
				number += 1.0;
		}
		return number;
	}
	
	
	public double accTimes(int targetID) // calculate acceptance times for targetID
	{
		double times = 0.0;
		Set<Integer> seed = new HashSet<Integer>();
		for(int i = 0; i<this.seedSet.size();i++)
			seed.add(this.seedSet.get(i));
		Set<Integer> nbrs = new HashSet<Integer>();
		nbrs = activatedNbr(targetID);
		Iterator<Integer> iter = nbrs.iterator();
		while(iter.hasNext())
		{
			int Nbr = iter.next();
			if(hasIntersection(this.BFSresult.get(Nbr),seed))
				times += 1.0;
		}
		
		return times;
		
	}
		
	/* read data and build Social Network*/
	
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
					String[] readlines = readline.split("\t");
					
					lineCount ++ ;
					if(isDuplication)
						this.putUpdate(Integer.parseInt(readlines[0]), Integer.parseInt(readlines[1]));
					else
						this.putUpdateNoduplication(Integer.parseInt(readlines[0]), Integer.parseInt(readlines[1]));
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
	
	/*public void dataPrint()   //hash table print
	{
		Set<Integer> set = this.GraphTable.keySet();
		Iterator<Integer> it = set.iterator();
		//int maxlen = this.GraphTable.size();
		
		while(it.hasNext())
		{
			Integer key = it.next();
			System.out.print(key+",");
			int m = this.GraphTable.get(key).size();
			System.out.print("{");
			for(int i = 0; i < m; i++)
				if(i != m-1)
					System.out.print(this.GraphTable.get(key).get(i)+" ");
				else
					System.out.print(this.GraphTable.get(key).get(i));
			System.out.println("}");
		}
	}
	*/
	
	
	public ArrayList<Double> createRandomDouble(int arraysize)   // create random double arraylist
	{
		ArrayList<Double> ranArr = new ArrayList<Double>();
		for(int i = 0; i < arraysize; i++)
			ranArr.add(Math.random());
		return ranArr;
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
	
	public void setInEdgeGraph() 
	{
		ArrayList<Double> list, value;
		for(int i = 0; i<this.nodeSet.size(); i++)
		{
			list = new ArrayList<Double>();
			value = new ArrayList<Double>();
			list = this.Graph.get(this.nodeSet.get(i)).probability;
			for(int j = 0; j < list.size(); j++)
			{
				value.add(-1/Math.log(this.Graph.get(this.nodeSet.get(i)).probability.get(j)));
			}
			this.Graph.get(this.nodeSet.get(i)).in_Edge = value;
		}
	}
	
	public void setPropagateGraph()  // random setting (0, 1] arraylist
	{
		ArrayList<Double> list;
		for(int i = 0; i<this.nodeSet.size(); i++)
		{
			int a = this.Graph.get(this.nodeSet.get(i)).size();
			list = new ArrayList<Double>();
			list = this.createRandomDouble(a);
			this.Graph.get(this.nodeSet.get(i)).probability = list;
		}
	}
	
	
	
	public void bfsProcess(int nodeID, int targetID) // neighbor
	{
		Set<Integer> bfsNodes = new HashSet<Integer>();
		Set<Integer> now = new HashSet<Integer>();
		Set<Integer> next = new HashSet<Integer>();
		bfsNodes.add(nodeID); //neighbor
		bfsNodes.add(targetID); // target
		now.add(nodeID);
		//System.out.println(".");
		while(now.size() != 0)
		{
			
			//ArrayList<Integer> nextlayer = new ArrayList<Integer>();
			
			Iterator<Integer> it = now.iterator(); //current layer
			
			while(it.hasNext())
			{
				next.addAll(activatedNbr(it.next())); // next layer
			}
			next.removeAll(bfsNodes); // remember all node from next layer
			next.remove(targetID); // don't remember target node
			now.clear(); 
			now.addAll(next);
			bfsNodes.addAll(next);
			next.clear();
		}
		bfsNodes.remove(targetID);
		//System.out.println("No. "+ nodeID + "\n" + bfsNodes.toString());
		//System.out.println("ID:"+nodeID+" BFS Fin!");
		if(bfsNodes.size()!=0)
			this.BFSresult.put(nodeID, bfsNodes);
		//System.out.println("No. "+ nodeID + " Size: " + bfsNodes.size());
	}
	
	/*public void showNodeResult(int nodeID)
	{
		int a = nodeID;
		ArrayList<Integer> nbr = new ArrayList<Integer>();
		ArrayList<Double> edgeWeight = new ArrayList<Double>();
		ArrayList<Double> prop_b = new ArrayList<Double>();
		nbr = getNeibh(a);
		edgeWeight = getNeibhInEdge(a);
		prop_b = getNeibhPropGraph(a);
		System.out.println("Show node ID: "+a);
		System.out.print("neighbor: \n");
		for(int i = 0; i < nbr.size();i++)
			System.out.print(nbr.get(i)+" ");
		System.out.print("\nPropagation probability:\n");
		for(int i = 0; i < prop_b.size();i++)
			System.out.print(prop_b.get(i)+" ");
		System.out.print("\nIn-Edge Weight:\n");
		for(int i = 0; i < edgeWeight.size();i++)
			System.out.print(edgeWeight.get(i)+" ");
		System.out.println("\n-------------------");
		
	}
	*/
	
	
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
	
	
	
	public void clearActResult() // clear all active result from Graph
	{
		for(int i = 0; i < this.Graph.size(); i++)
		{
			this.Graph.get(this.nodeSet.get(i)).actResult.clear();
			this.Graph.get(this.nodeSet.get(i)).actResult.trimToSize();
		}
	}
	
	/*public void showHash()
	{
		if(this.BFSTables.size()>=2) // size >=2 show two hash table
		{
			Iterator<Integer> it = this.BFSTables.get(0).keySet().iterator();
			Iterator<Integer> it2 = this.BFSTables.get(1).keySet().iterator();
		
			System.out.println("Hash1 size: "+this.BFSTables.get(0).keySet().size());
			int i = 0;
			while(it.hasNext())
			{
				i++;
				int key = it.next();
				System.out.println("Key "+i+" :"+key+" \nvalue:"+this.BFSTables.get(0).get(key).toString());
			}
			i = 0;
			System.out.println("\nHash2 size: "+this.BFSTables.get(1).keySet().size());
			while(it2.hasNext())
			{
				i++;
				int key = it2.next();
				System.out.println("Key "+i+" :"+key+" \nvalue :"+this.BFSTables.get(1).get(key).toString());
			}
		}
		else
		{
			System.out.println("Table size "+this.BFSTables.size()+"\nCan't show two hash");
			
		}
	}*/
	
	public double acceptanceTimes(int targetNode) //return acceptance times
	{
		double acceptanceTimes = 0.0;
		ArrayList<Integer> Nbr = new ArrayList<Integer>();
		Nbr = this.Graph.get(targetNode).neighborID;
		MonteCarlo Target = this.Graph.get(targetNode);
		
		for(int i = 0; i < Nbr.size(); i++)
		{
			if(Target.actResult.get(i) && isConnectTrace(Target.neighborID.get(i))) //Neighbor can influence target and seed can connect to target neighbor
				acceptanceTimes += 1.0;
		}
		
		
		return acceptanceTimes;
	}
	
	public void acceptanceEvaluation(int targetNode, int MonteCarloTimes, ArrayList<Integer> seed1, ArrayList<Integer> seed2)
	{
		double[] acceptanceTimes1 = new double[seed1.size()]; 
		double[] acceptanceTimes2 = new double[seed2.size()]; 
		
		if(MonteCarloTimes <= 0)
			System.out.println("Number Setting Wrong");
		if(!this.nodeSet.contains(targetNode))
			System.out.println("No such target ID");
		double time = (double)MonteCarloTimes;
		
		for(int i = 0; i < MonteCarloTimes; i++)
		{
			clearActResult();
			createResult();
			
			//System.out.println("Neighbors group "+i+": "+this.Graph.get(targetNode).activeNbr().toString());//check
			for(int nbr : this.Graph.get(targetNode).activeNbr())
			{
				String[] kStr = seqEvaluetion(seed1, seed2, nbr, targetNode).split(","); 
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
			if(j!=seed2.size()-1)
				System.out.print(acceptanceTimes1[j]+", ");
			else
				System.out.print("\n");
		}
		System.out.print("\n");
		for(int j = 0; j < seed2.size() ;j++)
		{
			acceptanceTimes2[j] /= time ;
			if(j!=seed2.size()-1)
				System.out.print(acceptanceTimes2[j]+", ");
			else
				System.out.print("\n");
		}
		
		
		
	}
	
	public void acceptanceTest(int targetNode, int MonteCarloTimes, ArrayList<Integer> seed1, ArrayList<Integer> seed2)
	{
		double[] acceptanceTimes1 = new double[seed1.size()]; 
		double[] acceptanceTimes2 = new double[seed2.size()]; 
		this.seedSet = seed1;
		
		if(MonteCarloTimes <= 0)
			System.out.println("Number Setting Wrong");
		if(!this.nodeSet.contains(targetNode))
			System.out.println("No such target ID");
		double time = (double)MonteCarloTimes;
		
		for(int i = 0; i < MonteCarloTimes; i++)
		{
			clearActResult();
			createResult();
			
			//System.out.println("Neighbors group "+i+": "+this.Graph.get(targetNode).activeNbr().toString());//check
			for(int nbr : this.Graph.get(targetNode).activeNbr())
			{
				String[] kStr = seqEvaluetion(seed1, seed2, nbr, targetNode).split(","); 
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
	public String seqEvaluetion(ArrayList<Integer> seed, ArrayList<Integer> seed2, int targetNbrID, int target )
	{
		
		Set<Integer> tracedNode = new HashSet<Integer>();
		Set<Integer> nowArr = new HashSet<Integer>();
		Set<Integer> nextNodeArr = new HashSet<Integer>();
				
		nowArr.add(targetNbrID);  // target(neighbors)
		tracedNode.add(targetNbrID);  // remember targetNbr
		tracedNode.add(target);       // remember target
		
		int firstInfluenceNode = seed.size();
		int firstInfluenceNode2 = seed2.size();
		
		while(nowArr.size()!=0)
		{
			Iterator<Integer> iter = nowArr.iterator(); //next BFS level array
			while(iter.hasNext())
			{
				nextNodeArr.addAll(activatedNbr(iter.next()));
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
		
		if(firstInfluenceNode!=seed.size())
			return firstInfluenceNode+","+firstInfluenceNode2;
		else
			return "-1,-1";
	}
	
	
	public double MC_acceptanceTimes(int times, int targetNodeID) // using Monte Carlo to simulation acceptance times without pre-computation
	{
		if(times <= 0)
			System.out.println("Number Setting Wrong");
		if(!this.nodeSet.contains(targetNodeID))
			System.out.println("No such target ID");
		double time = (double)times;
		double expectAccTimes = 0.0;
		for(int i = 0; i < times; i++)
		{
			clearActResult();
			createResult();
			expectAccTimes += acceptanceTimes(targetNodeID);
		}
		return expectAccTimes/time;
	}
	
	/*public double MC_times(int times, int targetNodeID)
	{
		if(times <= 0)
			System.out.println("Number Setting Wrong");
		if(!this.nodeSet.contains(targetNodeID))
			System.out.println("No such target ID");
		double time = (double)times;
		double expectAccTimes = 0.0;
		for(int i = 0; i < times; i++)
		{
			clearActResult();
			createResult();
			this.BFSresult.clear();
			this.BFSTables.clear();
			connectedTable(targetNodeID); //bfs table
			
			
			expectAccTimes += accTimes(targetNodeID);
			if( i%1000 == 0)
				System.out.print(".");
		}
		
		return expectAccTimes/time;
	}*/
	
	public void createBFSTables(int times, int targetID) // Pre-Compute connected nodes for all Monte Carlo result
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
			connectedTable(targetID);
			
			
			//System.out.println(".");
			if(i==0)
			{
				endTime2 = System.currentTimeMillis();
				/*System.out.println("Table size: " + this.BFSresult.size());
				Iterator<Integer> iter = this.BFSresult.keySet().iterator();
				while(iter.hasNext())
				{
					int key = iter.next();
					System.out.println("Key: " + key + "\nSize: " + this.BFSresult.get(key).size() + "\n");
				}
				int key = iter.next();
				System.out.println("Key: " + key + "\nValue: " + this.BFSresult.get(key) + "\n");*/
			}
			this.BFSTables.put(i, this.BFSresult);
			this.BFSresult = new Hashtable<Integer, Set<Integer>>();
			
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
		System.out.println("Create Tables Time: " + totalTime/1000+" sec");
		
	}
	
	public double MC_expectedTimes() //average result (using pre-computing bfs result)
	{
		double expectAccTimes = 0.0;
		
		if(this.BFSTables.size()==0)
		{
			System.out.println("BFS tables do not build");
			return 0.0;
		}
		
					
		for(int j = 0; j < this.BFSTables.size(); j++)
			expectAccTimes += nConnectNbr(this.BFSTables.get(j));
		
		
		return expectAccTimes/(double)this.BFSTables.size();
	}
	
	public ArrayList<Integer> greedy(int top_k, int targetID, int times) // greedy algorithm (remember all BFS results)
	{
		ArrayList<Integer> seeds = new ArrayList<Integer>(); // found seed
		ArrayList<Integer> tempSeed = new ArrayList<Integer>();  // add a candidate to seeds 
		
		
		createBFSTables(times, targetID);
		//showHash();
		
		double maxValue = 0.0;
		int maxNodeID = -1;
		while(seeds.size() < top_k)
		{
			double startTime, endTime;
			startTime = System.currentTimeMillis();
			for(int i = 0; i < this.nodeSet.size(); i++)
			{
				if(this.nodeSet.get(i) == targetID || seeds.contains(this.nodeSet.get(i))) //don't care ID = target or ID in seeds
					continue;
				tempSeed.clear();
				tempSeed.addAll(seeds);
				//if(!seeds.contains(this.nodeSet.get(i))) //except and seeds
				
				tempSeed.add(this.nodeSet.get(i));
				
				setSeed(tempSeed); //edit seed
				double i_value = MC_expectedTimes(); //get value
				
				if(i_value > maxValue) // record max value, id
				{
					maxValue = i_value;
					maxNodeID = i;
				}
				if( i%10000 == 0 )
					System.out.print(".");
			}
			seeds.add(this.nodeSet.get(maxNodeID));
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
	
	
	/*
	//Too slow
	public ArrayList<Integer> greedyAlg(int top_k, int targetID, int times)
	{
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		ArrayList<Integer> tempSeed = new ArrayList<Integer>();
		
		double maxValue = 0.0;
		int maxNodeID = -1;
		while(seeds.size() < top_k)
		{
			for(int i = 0; i < this.nodeSet.size(); i++)
			{
				tempSeed.clear();
				tempSeed.addAll(seeds);
				if(this.nodeSet.get(i) != targetID && !seeds.contains(this.nodeSet.get(i))) //except target and seeds
				{
					tempSeed.add(this.nodeSet.get(i));
				}
				setSeed(tempSeed);
				double i_value = MC_times(times, targetID);
				if(i_value > maxValue) // record max value, id
				{
					maxValue = i_value;
					maxNodeID = i;
				}
				if(i%100==0)
					System.out.print(".");
			}
			seeds.add(this.nodeSet.get(maxNodeID));
			maxNodeID = -1;
			maxValue = 0.0;
		}
		return seeds;
	}
	*/
	public void coNbr(int node1, int node2) //print common neighbor
	{
		ArrayList<Integer> co = new ArrayList<Integer>();
		ArrayList<Integer> nb1 = this.Graph.get(node1).neighborID;
		ArrayList<Integer> nb2 = this.Graph.get(node2).neighborID;
		co.addAll(nb1);
		co.retainAll(nb2);
		System.out.println("(N1, N2) = ("+node1+", "+node2+")"+"\ncoNbr:"+co.toString());
		
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
	public void trim()
	{
		for(int i = 0; i < this.Graph.size(); i++)
		{
			this.Graph.get(this.nodeSet.get(i)).MonteCarlo_trim();
		}
	}
	public void showInformation(int TargetID, int k, int MonteCarloTimes)
	{
		ArrayList<Double> a = getNeibhPropGraph(TargetID);
		double max = 0.0;
		for(int i = 0; i < a.size();i++)
			max+=a.get(i);
		System.out.println
		("Our Target: "+TargetID
		+"\n\nTarget Neighbors: \n"+getNeibh(TargetID)
		+"\n\nCorresponding Propagation Probability: \n"+a.toString()
		+"\n\nMaximum Influence Times: \n" + max
		+"\n\n ---- Find "+k+"-Seeds ----\n"
		+"Monte Carlo times: "+ MonteCarloTimes
		);
	}
	
	
	public static void main(String[] args) throws IOException
	{
		
		if(args.length > 5 )
			System.out.println("Too many args, your args' length : "+args.length+"\nPlease enter: 1.TargetID 2.Network_data 3.Propagate_data 4.Seed size 5.Monte Carlo Times" +
					"\ne.g. 0 com-dblp.ungraph.txt prop-O.txt 1 200");
		
		else
		{
			int influenceTargetID = 0; //default target
			int MonteCarloTimes = 200;
			if(args.length >= 1)
				influenceTargetID = Integer.parseInt((args[0]));
			String network = "com-dblp.ungraph - small.txt" , propnetwork = "prop.txt"; //default data
			if(args.length >= 2)
				network = args[1];
			if(args.length >= 3)
				propnetwork = args[2];
			int k = 10; //default
			if(args.length >= 4)
				k = Integer.parseInt(args[3]);
			if(args.length >= 5)
				MonteCarloTimes = Integer.parseInt(args[4]);
		
			double startTime, endTime, totalTime;
		
			boolean isDuplica = true;
			if(network.equals("Brightkite_edges.txt"))
				isDuplica = false;
			// Initial Setting
			Soc3 d = new Soc3();
			d.dataRead(network, isDuplica);  // read network structure
			d.setNodeset();       // all nodes
			d.ReadPropagate(propnetwork);  //set propagation probability
			//d.setInEdgeGraph();  //set in edge weight from propagation graph
			d.trim();
			d.info();
			// Main Function 
		
			startTime = System.currentTimeMillis();
			//d.showNodeResult(0);
		
			d.showInformation(influenceTargetID, k, MonteCarloTimes);
		
			//Seed Setting
			ArrayList<Integer> seeds = new ArrayList<Integer>();
			
			//MonteCarlo simulation
			
			seeds = d.greedy(k, influenceTargetID, MonteCarloTimes);
			
			System.out.println("\nGreedy algorithm:\n"+"Seed: " + seeds.toString());
			
			endTime = System.currentTimeMillis();
			totalTime = endTime - startTime;
			System.out.println("Execution Time: " + totalTime/1000+" sec");
			
			//evaluation
			
			startTime = System.currentTimeMillis();
			/*
			seeds.clear();
			seeds.add(101215);
			seeds.add(120044);
			seeds.add(33971);
			seeds.add(33043);
			seeds.add(411025);
			//seeds.add(413808);
			//seeds.add(274042);
			//seeds.add(1);
			//seeds.add(403524);
			
			d.setSeed(seeds);  //set our seed result 
			System.out.println("---Evaluation---\nExpected Times: ");
			System.out.println( d.MC_times(1000,influenceTargetID));
			0
			endTime = System.currentTimeMillis();
			totalTime = endTime - startTime;
			System.out.println("\nEvaluation Spend: " + totalTime/1000+" sec");
			*/
		}
		/*
		String[] seedStr = "2, 411025".split(", ");
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		for(String Str : seedStr)
		{
			seeds.add(Integer.parseInt(Str));
		}
		
		Soc3 s = new Soc3();
		s.dataRead("com-dblp.ungraph - small.txt", true);
		s.ReadPropagate("prop.txt");
		s.setNodeset();
		s.setSeed(seeds);
		s.createBFSTables(200, 0);
		
		System.out.println("Expected Influence Times: "+s.MC_expectedTimes());*/
	}
}
