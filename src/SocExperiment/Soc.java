package SocExperiment;


import java.util.*;
import java.io.*;
//import java.lang.Math;

public class Soc {

	private Hashtable<Integer, MonteCarlo> Graph = new Hashtable<Integer, MonteCarlo>(); 
	private Hashtable<Integer, ArrayList<Integer>> GraphTable = new Hashtable<Integer, ArrayList<Integer>>(); // Graph
	//private int MaxNode;
	private Hashtable<Integer, ArrayList<Double>> inEdgeGraph = new Hashtable<Integer, ArrayList<Double>>(); // in-edge = -1/log(propagte)
	private Hashtable<Integer, ArrayList<Double>> propagateGraph = new Hashtable<Integer, ArrayList<Double>>(); // propagation probability
	private ArrayList<Integer> nodeSet = new ArrayList<Integer>();  // all nodes
	//private ArrayList<Integer> shortestPre = new ArrayList<Integer>(); //shortest path tree prefix
	private Hashtable<Integer, ArrayList<Boolean>> actResult = new Hashtable<Integer, ArrayList<Boolean>>(); // random process active or inactive
	private ArrayList<Integer> seedSet = new ArrayList<Integer>();
	private Hashtable<Integer, Set<Integer>> BFSresult = new Hashtable<Integer, Set<Integer>>(); // all possible influence path 
	private Hashtable<Integer, Hashtable<Integer, Set<Integer>>> BFSTables = new Hashtable<Integer, Hashtable<Integer, Set<Integer>>>(); 
	
	
	public ArrayList<Integer> getNodeSet()
	{
		return this.nodeSet;
	}
	public ArrayList<Integer> getNeibh(Integer a)
	{
		return this.GraphTable.get(a);
	}
	
	public ArrayList<Double> getNeibhInEdge(Integer a)
	{
		return this.inEdgeGraph.get(a);
	}
	
	public ArrayList<Double> getNeibhPropGraph(Integer a)
	{
		return this.propagateGraph.get(a);
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
				for(int i = 0; i < s.split(" ")[1].split(",").length; i++)
					value.add(Double.parseDouble(s.split(" ")[1].split(",")[i]));
				this.propagateGraph.put(Integer.parseInt(s.split(" ")[0]), value);
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
			for(int i = 0; i < this.propagateGraph.size();i++)
			{
				bw.write(this.nodeSet.get(i)+" ");
				for(int j = 0; j < this.propagateGraph.get(this.nodeSet.get(i)).size()-1;j++)
					bw.write(this.propagateGraph.get(this.nodeSet.get(i)).get(j)+",");
				bw.write(this.propagateGraph.get(this.nodeSet.get(i)).get(this.propagateGraph.get(this.nodeSet.get(i)).size()-1).toString());
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
			fw = new FileWriter(propfile + "uniform");
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i = 0; i < this.propagateGraph.size();i++)
			{
				bw.write(this.nodeSet.get(i)+" ");
				for(int j = 0; j < this.propagateGraph.get(this.nodeSet.get(i)).size()-1;j++)
					bw.write(this.propagateGraph.get(this.nodeSet.get(i)).get(j)+",");
				bw.write(this.propagateGraph.get(this.nodeSet.get(i)).get(this.propagateGraph.get(this.nodeSet.get(i)).size()-1).toString());
				bw.newLine();
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/*public Set<Integer> getActivatedSet(int nodeID)  
	{
		Set<Integer> arr = new HashSet<Integer>();
		ArrayList<Boolean> boo = new ArrayList<Boolean>();
		boo = this.actResult.get(nodeID);
		for(int i = 0; i< boo.size(); i++)
		{
			if (boo.get(i)== true)
			{
				arr.add(this.GraphTable.get(nodeID).get(i));
			}
		}
		return arr;
	}*/
	
	public Set<Integer> activatedNbr(int nodeID) //activated neighbor
	{
		Set<Integer> arr = new HashSet<Integer>();
		for(int i = 0 ; i< this.actResult.get(nodeID).size(); i++)
		{
			if(this.actResult.get(nodeID).get(i) == true)
			{
				arr.add(this.GraphTable.get(nodeID).get(i));
			}
		}
		return arr;
	}
	
	//set operation
	public static <T> Set<T> union(Set<T> setA, Set<T> setB) 
	{
		Set<T> tmp = new HashSet<T>(setA);
		tmp.addAll(setB);
		return tmp;
	}

	public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) 
	{
		Set<T> tmp = new HashSet<T>();
		for (T x : setA)
			if (setB.contains(x))
				tmp.add(x);
		return tmp;
	}

	public static <T> Set<T> difference(Set<T> setA, Set<T> setB) 
	{
		Set<T> tmp = new HashSet<T>(setA);
		tmp.removeAll(setB);
		return tmp;
	}
	
	public boolean isConnectTrace(int nodeID) // is node connected to seeds
	{
		Set<Integer> seed = new HashSet<Integer>();
		for(int i = 0; i< this.seedSet.size();i++)  //seed from arraylist to set 
			seed.add(this.seedSet.get(i));
		
		Set<Integer> tracedNode = new HashSet<Integer>();
		
		Set<Integer> lastNodeArr = new HashSet<Integer>();
		Set<Integer> currentNodeArr = new HashSet<Integer>();
		lastNodeArr.add(nodeID);  // target
		tracedNode.add(nodeID);  // remember target
		
		while(true)
		{
			tracedNode = union(tracedNode, currentNodeArr); // remember all traced nodes
			
			
			Iterator<Integer> iter = lastNodeArr.iterator(); //element of lastnodearr
			while(iter.hasNext())
			{
				currentNodeArr = union(currentNodeArr, activatedNbr(iter.next()));
			}
			
			currentNodeArr.removeAll(tracedNode);
						if(intersection(seed, currentNodeArr).size()!=0) //is connected
				break;
			if(currentNodeArr.size()==0)
				break;
			lastNodeArr.clear();
			lastNodeArr.addAll(currentNodeArr);
			
		}
		if(intersection(seed ,currentNodeArr).size()!=0)
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
			nbrs.add(iter.next());
		
		for(int i = 0; i< nbrs.size(); i++)
			bfsProcess(nbrs.get(i), targetID);
		//System.out.println("-------------Done~---------------");
	}
	
	public boolean isConnected(int nodeID, int targetNbrID)
	{
		//Set<Integer> nbr = new HashSet<Integer>();
		if(this.BFSresult.get(targetNbrID).contains(nodeID))
			return true;
		else
			return false;
	}
	
	public double nConnectNbr(Hashtable<Integer, Set<Integer>> hash) //number of neighbors that seed can be connected 
	{
		double number = 0.0;
		Set<Integer> seed = new HashSet<Integer>();
		for(int i = 0; i<this.seedSet.size();i++)
			seed.add(this.seedSet.get(i));
		Iterator<Integer> it = hash.keySet().iterator();
		
		while(it.hasNext())
		{
			int i = it.next();
			if(intersection(hash.get(i),seed).size()!=0)
				number += 1;
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
			if(intersection(this.BFSresult.get(Nbr),seed).size()!=0)
				times += 1.0;
		}
		
		return times;
		
	}
		
	public void clearBfsTable()  
	{
		this.BFSresult.clear();
	}
	
	/* read data and build Social Network*/
	
	public void putUpdate(Integer key, Integer value) // for all lines put or update hash
	{
		ArrayList<Integer> updatelist;
		if(GraphTable.containsKey(key)) //contain key -> update key
		{
			updatelist = new ArrayList<Integer>();
			updatelist = this.GraphTable.get(key);
			updatelist.add(value);
			this.GraphTable.remove(key);
			this.GraphTable.put(key, updatelist);
			
			if(this.GraphTable.containsKey(value)) // contain value -> update value
			{
				updatelist = new ArrayList<Integer>();
				updatelist = this.GraphTable.get(value);
				updatelist.add(key);
				this.GraphTable.remove(value);
				this.GraphTable.put(value, updatelist);
			}
			else  // no value -> create value
			{
				updatelist = new ArrayList<Integer>();
				updatelist.add(key);
				this.GraphTable.remove(value);
				this.GraphTable.put(value, updatelist);
			}
		}
		else if(!this.GraphTable.containsKey(value)) //no key -> put key value & no value -> put value key
		{
			updatelist = new ArrayList<Integer>();
			updatelist.add(key);
			this.GraphTable.put(value, updatelist);
			updatelist = new ArrayList<Integer>();
			updatelist.add(value);
			this.GraphTable.put(key, updatelist);
		}
		else	//no key -> put key value & update value
		{
			updatelist = new ArrayList<Integer>();
			updatelist.add(value);
			this.GraphTable.put(key, updatelist);
			updatelist = new ArrayList<Integer>();
			updatelist = this.GraphTable.get(value);
			updatelist.add(key);
			this.GraphTable.remove(value);
			this.GraphTable.put(value, updatelist);
			
		}
	}
	
	public void dataRead(String Filename) //Read text and build network
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
					this.putUpdate(Integer.parseInt(readlines[0]), Integer.parseInt(readlines[1]));
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
		Set<Integer> set = this.GraphTable.keySet();
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
			list = this.propagateGraph.get(this.nodeSet.get(i));
			for(int j = 0; j < list.size(); j++)
			{
				value.add(-1/Math.log(this.propagateGraph.get(this.nodeSet.get(i)).get(j)));
			}
			this.inEdgeGraph.put(this.nodeSet.get(i), value);
		}
	}
	
	public void setPropagateGraph()  // random setting (0, 1] arraylist
	{
		ArrayList<Double> list;
		for(int i = 0; i<this.nodeSet.size(); i++)
		{
			int a = this.GraphTable.get(this.nodeSet.get(i)).size();
			list = new ArrayList<Double>();
			list = this.createRandomDouble(a);
			this.propagateGraph.put(this.nodeSet.get(i), list);
		}
	}
	
	
	
	public void bfsProcess(int nodeID, int targetID) // neighbor
	{
		Set<Integer> bfsNodes = new HashSet<Integer>();
		Set<Integer> now = new HashSet<Integer>();
		Set<Integer> next = new HashSet<Integer>();
		bfsNodes.add(nodeID);
		bfsNodes.add(targetID);
		now.add(nodeID);
		//System.out.println(".");
		while(now.size() != 0)
		{
			
			//ArrayList<Integer> nextlayer = new ArrayList<Integer>();
			
			Iterator<Integer> it = now.iterator();
			
			while(it.hasNext())
			{
				next = union(next, activatedNbr(it.next()));
			}
			next.removeAll(bfsNodes);
			next.remove(targetID);
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
			propArr = new ArrayList<Double>();
			propResult = new ArrayList<Double>();
			resultEdge = new ArrayList<Boolean>();
			propArr = this.propagateGraph.get(this.nodeSet.get(i));
			propResult = createRandomDouble(propArr.size());
			for(int j = 0; j < propArr.size(); j++)
			{
				if(propArr.get(j)>propResult.get(j))
					resultEdge.add(true);  //smaller than propagation probability
				else
					resultEdge.add(false); //bigger
			}
			
			
			this.actResult.put(this.nodeSet.get(i), resultEdge);
			
		}
	}
	
	
	
	public void clearActResult()
	{
		this.actResult.clear();
	}
	
	public void showHash()
	{
		if(this.BFSTables.size()>=2)
		{
			Iterator<Integer> it = this.BFSTables.get(1).keySet().iterator();
			Iterator<Integer> it2 = this.BFSTables.get(2).keySet().iterator();
		
			System.out.println("Hash1 size: "+this.BFSTables.get(1).keySet().size());
			int i = 0;
			while(it.hasNext())
			{
				i++;
				int key = it.next();
				System.out.println("Key "+i+" :"+key+" \nvalue:"+this.BFSTables.get(1).get(key).toString());
			}
			i = 0;
			System.out.println("\nHash2 size: "+this.BFSTables.get(2).keySet().size());
			while(it2.hasNext())
			{
				i++;
				int key = it2.next();
				System.out.println("Key "+i+" :"+key+" \nvalue :"+this.BFSTables.get(2).get(key).toString());
			}
		}
		else
		{
			System.out.println("Table Not Build");
		}
	}/**/
	
	public double acceptanceTimes(int targetNode) //return acceptance times
	{
		double acceptanceTimes = 0.0;
		ArrayList<Integer> Nbr = new ArrayList<Integer>();
		Nbr = this.GraphTable.get(targetNode);
		ArrayList<Boolean> arr = new ArrayList<Boolean>();
		for(int i = 0; i < Nbr.size(); i++)
		{
			arr.add(isConnectTrace(Nbr.get(i)));
		}
		
		for(int i = 0; i < arr.size(); i++)
		{
			if(this.actResult.get(targetNode).get(i) && arr.get(i))
				acceptanceTimes += 1.0;
		}
		
		
		return acceptanceTimes;
	}
	
	public double MC_acceptanceTimes(int times, int targetNodeID)
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
	
	public double MC_times(int times, int targetNodeID)
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
			
			clearBfsTable();
			connectedTable(targetNodeID); //bfs table
			
			
			expectAccTimes += accTimes(targetNodeID);
		}
		return expectAccTimes/time;
	}
	
	public void createBFSTables(int times, int targetNodeID) // Pre-Compute connected nodes for all Monte Carlo result
	{
		double startTime, endTime, totalTime;
		startTime = System.currentTimeMillis();
		System.out.println("Create Tables");
		for(int i = 0; i < times; i++)
		{
			clearActResult();
			createResult();
			clearBfsTable();
			connectedTable(targetNodeID);
			//System.out.println(".");
			
			this.BFSTables.put(i, this.BFSresult);
			this.BFSresult = new Hashtable<Integer, Set<Integer>>();
			//System.out.println("Table "+i+" is Created");
		}
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Create Tables Time: " + totalTime/1000+" sec");
		
	}
	
	public double MC_expectedTimes(int times) //average result (by Monte Carlo method)
	{
		double expectAccTimes = 0.0;
		
		for(int j = 0; j < this.BFSTables.size(); j++)
			expectAccTimes += nConnectNbr(this.BFSTables.get(j));
		
		
		return expectAccTimes/(double)times;
	}
	
	public ArrayList<Integer> gr(int top_k, int targetID, int times) // greedy algorithm (remember all BFS results)
	{
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		ArrayList<Integer> tempSeed = new ArrayList<Integer>();
		
		
		createBFSTables(times, targetID);
		
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
				double i_value = MC_expectedTimes(times);
				if(i_value > maxValue) // record max value, id
				{
					maxValue = i_value;
					maxNodeID = i;
				}
				if(i%100==0)
					System.out.print(".");
			}
			seeds.add(this.nodeSet.get(maxNodeID));
			if(seeds.size()==top_k)
				System.out.println("\nExpected times:"+maxValue);
			maxNodeID = -1;
			maxValue = 0.0;
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
		ArrayList<Integer> nb1 = this.GraphTable.get(node1);
		ArrayList<Integer> nb2 = this.GraphTable.get(node2);
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
			edgesize += this.GraphTable.get(this.nodeSet.get(i)).size();
		}
		System.out.println("Edge Size: "+ edgesize);
		System.out.println("-------------------------");
	}
	
	
	
	public static void main(String[] args) throws IOException
	{
		double startTime, endTime, totalTime;
		
		// Initial Setting
		Soc d = new Soc();
		d.dataRead("C:\\Users\\mosu\\Desktop\\com-dblp.ungraph.txt");
		d.setNodeset();
		d.ReadPropagate("C:\\Users\\mosu\\Desktop\\prop-O.txt");  //set propagation probability
		d.setInEdgeGraph();  //set in edge weight from propagation graph
		d.info();
		/* Main Function */
		
		startTime = System.currentTimeMillis();
		//d.showNodeResult(0);
		
		int influenceTargetID = 0;
		System.out.println
				("Our Target: "+influenceTargetID
				+"\nTarget Neighbors: "+d.getNeibh(influenceTargetID)
				+"\nCorresponding Propagation Probability"+d.getNeibhPropGraph(influenceTargetID)
				);
		
		//Seed Setting
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		seeds.add(2);
		//seeds.add(33126);
		/*seeds.add(274042);
		
		d.setSeed(seeds);
		
		
		//MonteCarlo simulation
		
		*/
		//seeds = d.gr(1, 0, 1000);
		System.out.println("\nGreedy algorithm:\n"+"Seed: " + seeds.toString());
		
		//d.showHash();
		
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Execution Time: " + totalTime/1000+" sec");
		/**/
		//evaluation
		
		d.setSeed(seeds);  //set our seed result 
		
		System.out.println( d.MC_times(10000,0));
		
		
	}
}
