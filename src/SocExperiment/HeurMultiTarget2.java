package SocExperiment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

	class Node implements Comparable<Node>{
		private int node;
		private double value;
		private int layer;
		
		Node(int id, double score, int layer)
		{
			this.node = id;
			this.value = score;
			this.layer = layer;
		}
		public double getValue()
		{
			return value;
		}
		public int getID()
		{
			return node;
		}
		public int getlayer()
		{
			return this.layer;
		}
		public void setValue(double newValue)
		{
			this.value = newValue;
		}
		
		@Override
		public String toString()
		{
			return String.format("%d", node);
		}
		
		@Override
		public int compareTo(Node other)
		{
			if(this.value-other.value>0)
				return 1;
			else if(this.value-other.value==0)
				return 0;
			else
				return -1;
		}
		
		@Override
		public int hashCode()
		{
			int hash = 7;
			hash = 47*hash + Objects.hashCode(this.node);
			return hash;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj == null) 
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Node other = (Node) obj;
			if (!Objects.equals(this.node, other.node))
				return false;
			
			return true;
		}
		
	}
	public class HeurMultiTarget2 {
		private double threshold = 0.0;
		private Set<Node> Score = new HashSet<Node>();
		private ArrayList<Integer> Targets = new ArrayList<Integer>();
		
		public ArrayList<Node> sortScore()
		{
			ArrayList<Node> list = new ArrayList<Node>(this.Score);
			Collections.sort(list);
			return list;
		}
		public ArrayList<Integer> getTarget()
		{
			return this.Targets;
		}
		public void setThreshold(double th)
		{
			this.threshold = th;
		}
		public double getThreshold()
		{
			return this.threshold;
		}
		public ArrayList<Integer> string2Targets(String s)
		{
			ArrayList<Integer> arr = new ArrayList<Integer>();
			s = s.replaceAll("\\s+", "");
			String[] t = s.split(",");
			for(String tt :t)
				arr.add(Integer.parseInt(tt));
			return arr;
		}
		public void setTargets(ArrayList<Integer> t)
		{
			this.Targets.addAll(t);
		}
		public ArrayList<Integer> mainProcess(int k, String network, String prop, ArrayList<Integer> mc_SA_List, int steps) throws IOException
		{
			ArrayList<Integer> seeds = new ArrayList<Integer>();
			
			boolean isDuplica = true;
			if(network.equals("Brightkite_edges.txt"))
				isDuplica = false;
			
			SA_greedy iMt = new SA_greedy();
			iMt.dataRead(network, isDuplica);  // read network structure
			iMt.setNodeset();       // all nodes
			if(prop.equals("prop_dblp_8020"))
				iMt.ReadPropagate(prop, 0);  //set propagation probability
			else
				iMt.ReadPropagate(prop);
			iMt.info();
			
			int mod = mc_SA_List.get(mc_SA_List.size()-1)/10;
			int listIndex = 0;
			
			for(int iter = 0; iter < mc_SA_List.get(mc_SA_List.size()-1) && listIndex < mc_SA_List.size(); iter++)
			{
				if(mod==0)
					mod = 1;
				if(iter%mod == 0)
					System.out.print(iter+", ");
				else if(iter == mc_SA_List.get(mc_SA_List.size()-1)-1)
					System.out.println(iter);
				
				
				iMt.clearActResult();
				iMt.createBinaryResult();
				for(int tar : this.Targets)
				{
					for(Node node : this.mipProcess(tar, iMt, steps))
					{
						
						if(this.Score.contains(node))
						{
							for(Node s : this.Score)
							{
								if(node.getID()==s.getID())
								{
									s.setValue(s.getValue()+1.0);
									break;
								}
							}
						}
						else
						{
							node.setValue(1.0);
							if(this.Score.contains(node))
								System.out.println("123");
							this.Score.add(node);
							
						}
					}
				}
				if(iter == mc_SA_List.get(listIndex)-1) // SA_MC
				{
					for(int len = this.Score.size(); len > 0; len--)
					{
						Node cand = this.sortScore().get(len-1);
						if(seeds.contains(cand.getID()))
							continue;
						else
						{
							seeds.add(cand.getID());
							break;
						}
					}
					listIndex++;
				}
				
			}
			
			
			//System.out.println(this.Score);
			//System.out.println(this.sortScore());
			//for(Node n : this.sortScore())
			//	System.out.print(n.getValue() + ",");
			return seeds;
			
		}
		public Set<Node> mipProcess(int initNode, SA_greedy sag, int maxlayer)
		{
			SortedSet<Node> score = new TreeSet<Node>(); // small -> big
			
			//inital nbr
			Hashtable<Integer, MonteCarlo> hash = sag.getGraph();
			
			for(int i = 0; i < hash.get(initNode).neighborID.size(); i++) //neighbors of initial node
			{
				if(hash.get(initNode).probability.get(i)<this.threshold)
					continue;
				Node nb = new Node(hash.get(initNode).neighborID.get(i),hash.get(initNode).probability.get(i), 1 ); //first degree neighbor
				score.add(nb);
			}
			
			Set<Node> tempList = new HashSet<Node>();
			while(score.size() > 0 && score.last().getValue() >= this.threshold ) // max score
			{
				Node maxNode = score.last();
				if(maxNode.getlayer() >= maxlayer)
				{
					tempList.add(maxNode);
					score.remove(maxNode);
					continue;
				}
				double scale = maxNode.getValue();
				int nextlayer = maxNode.getlayer()+1;
				ArrayList<Integer> nblist = hash.get(maxNode.getID()).neighborID;
				ArrayList<Double> nbprop = hash.get(maxNode.getID()).activeProbability();
				if(nblist.size() !=  nbprop.size())
					System.out.println("function call error");
				for(int i = 0; i < nblist.size(); i++)
					if(scale*nbprop.get(i)>=this.threshold)
					{
						Node nextNode = new Node(nblist.get(i),scale*nbprop.get(i),nextlayer);
						
						if(score.contains(new Node(nblist.get(i),nbprop.get(i),nextlayer)))
						{
							for(Node n : score)
								if(n.equals(nextNode) && n.getValue()<nextNode.getValue())
								{
									score.add(nextNode);
									break;
								}
						}
						else
							score.add(nextNode);
					}
				tempList.add(maxNode);
				score.remove(maxNode);
			}
			tempList.addAll(score);
			tempList.remove(new Node(initNode, 1.0 ,0));
			//System.out.println("target "+initNode+": "+tempList);
			return tempList;
		}
		
		public void targetReader(String targetfile) throws IOException
		{
			FileReader fr;
			try {
				fr = new FileReader(targetfile);
				BufferedReader br = new BufferedReader(fr);
				while(br.ready())
				{
					for(String s : br.readLine().split(", "))
						this.Targets.add(Integer.parseInt(s));
				}
				br.close();
				fr.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		/*public ArrayList<Integer> getTopKMiiaScore(int k)
		{
			ArrayList<Integer> topKScoreKey = new ArrayList<Integer>();
			for(int i = 0; i < k; i++)
			{
				int key = new HeurSoc().maxKey(this.MIIAScore);
				this.MIIAScore.remove(key);
				topKScoreKey.add(key);
			}
			return topKScoreKey;
		}*/
		
		
		/**
		 * @param args
		 * @throws IOException 
		 */
		public static void main(String[] args) throws IOException {
			System.out.println("SAB for multi-target");
			
			int maxSteps = 3;
			HeurMultiTarget2 hMt = new HeurMultiTarget2();
			
			hMt.setThreshold(0.15);
			
			String TargetStr = "12051,197819,305970,327655"; //default target
			int MonteCarloTimes = 200;
			ArrayList<Integer> Targets = new ArrayList<Integer>();
			Targets = hMt.string2Targets(TargetStr);
			/*Set<Integer> t = new HashSet<Integer>();
			t.addAll(Targets);
			System.out.println(t.size());
			*/
			
			if(args.length >= 1 && !args[0].equals("target"))
			{
				Targets.clear();
				Targets = hMt.string2Targets(args[0]);
			}
			if(args.length >= 1 && args[0].equals("target"))
			{
				hMt.targetReader("target");
				Targets = hMt.getTarget();
			}
			
			hMt.setTargets(Targets);
			
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
			//ArrayList<Integer> StopArray = new Heur2Soc().splitTimesArr(MonteCarloTimes, k);
			
			System.out.println("Targets: "+Targets.toString());
			double startTime, endTime, totalTime;
			
			startTime = System.currentTimeMillis();
			
			System.out.println("k = "+k+"; network: "+network+"; prop network: "+propnetwork+"; Monte Carlo Iterator: "+MonteCarloTimes+"; Max Steps: "+maxSteps+"\nThreshold: "+hMt.getThreshold());
			
			ArrayList<Integer> SA_List = new ArrayList<Integer>();
			SA_List.add(1);
			SA_List.add(2);
			SA_List.add(3);
			SA_List.add(5);
			SA_List.add(8);
			
			System.out.println(hMt.mainProcess(k, network, propnetwork, SA_List, maxSteps));
			
			endTime = System.currentTimeMillis();
			
			totalTime = endTime - startTime;
			
			System.out.println("Execution Time: " + totalTime/1000+" sec");
		}
	}

