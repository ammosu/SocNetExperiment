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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

	public class HeurMultiTarget1 {
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
		public ArrayList<Integer> mainProcess(int k, String network, String prop, int mcIterator, int steps) throws IOException
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
			
			for(int iter = 0; iter < mcIterator; iter++)
			{
				int mod = mcIterator/10;
				if(mod==0)
					mod = 1;
				if(iter%mod == 0)
					System.out.print(iter+", ");
				else if(iter == mcIterator-1)
					System.out.println(iter);
				
				
				iMt.clearActResult();
				iMt.createResult();
				for(int tar : this.Targets)
				{
					Set<Node> nSet = this.mipProcess(tar,iMt,steps);
					for(Node node : nSet)
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
			}
			for(Node m : this.sortScore().subList(this.Score.size()-k, this.Score.size()))
				seeds.add(m.getID());
			
			
			//**** Print EAF result
			String seedstr2 = "286351, 6433, 92634, 33126, 19114";
			
			ArrayList<Integer> seed2 = new ArrayList<Integer>();
			
			for(String s2 : seedstr2.split(", "))
				seed2.add(Integer.parseInt(s2));
			iMt.createBFSTables(100, this.Targets, steps);
			System.out.println("Seed1's EAF: ");
			for(int i = 0; i< seeds.size(); i++)
			{
				iMt.setSeed( seeds.subList(0, i+1));
				double i_value = iMt.MC_expectedTimes()/100.0; //get value
				System.out.print(i_value+", ");
			}
			iMt.setSeed(seed2);
			System.out.println("\nGreedy's EAF: "+iMt.MC_expectedTimes()/100.0);
			//int eva_MC = 100;
			//iMt.acceptanceEvaluation(Targets, eva_MC, seed2, seeds, steps);
			
			return seeds;
			
		}
		public Set<Node> mipProcess(int initNode, SA_greedy sag, int maxlayer)
		{
			Set<Node> score = new HashSet<Node>(); // small -> big
			
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
			while(score.size() > 0 && this.getMaxNode(score).getValue() >= this.threshold ) // max score
			{
				Node maxNode = this.getMaxNode(score);
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
		public Set<Node> mipProcess2(int targetNode, int initNode, SA_greedy sag, int maxlayer)
		{
			Set<Node> score = new HashSet<Node>(); // small -> big
			
			//inital nbr
			Hashtable<Integer, MonteCarlo> hash = sag.getGraph();
			
			for(int i = 0; i < hash.get(initNode).neighborID.size(); i++) //neighbors of initial node
			{
				if(hash.get(initNode).probability.get(i)<this.threshold || hash.get(initNode).neighborID.get(i) == targetNode)
					continue;
				Node nb = new Node(hash.get(initNode).neighborID.get(i),hash.get(initNode).probability.get(i), 1 ); //first degree neighbor
				score.add(nb);
			}
			
			Set<Node> tempList = new HashSet<Node>();
			while(score.size() > 0 && this.getMaxNode(score).getValue() >= this.threshold ) // max score
			{
				Node maxNode = this.getMaxNode(score);
				if(maxNode.getlayer() == maxlayer)// stop growth
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
						Node nextNode = new Node(nblist.get(i),nbprop.get(i),nextlayer);
						
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
		private Node getMaxNode(Set<Node> nodeSet)
		{
			Node max = new Node(-1,Double.MIN_VALUE,0);
			for(Node node : nodeSet)
			{
				if(max.compareTo(node) == -1)
					max = node;
			}
			return max;
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
		
		/**
		 * @param args
		 * @throws IOException 
		 */
		public static void main(String[] args) throws IOException {
			System.out.println("MIPB for multi-target");
			
			int maxSteps = 3;
			HeurMultiTarget1 hMt = new HeurMultiTarget1();
			
			hMt.setThreshold(0.2);
			
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
			hMt.mainProcess(k, network, propnetwork, MonteCarloTimes, maxSteps);
			//System.out.println(seed);
			
			endTime = System.currentTimeMillis();
			
			totalTime = endTime - startTime;
			
			System.out.println("Execution Time: " + totalTime/1000+" sec");
			
			
			//SA_greedy sag = new SA_greedy();
			
			
		}

	}

