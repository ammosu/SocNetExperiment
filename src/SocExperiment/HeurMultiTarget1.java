package SocExperiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

	public class HeurMultiTarget1 {
		private double threshold = 0.1;
		//private ArrayList<Integer> candidate = new ArrayList<Integer>();
		//private Hashtable<Integer, ArrayList<Double>> MiiaScore = new Hashtable<Integer, ArrayList<Double>>();
		
		//private Hashtable<Integer, Double> MIIAScore = new Hashtable<Integer, Double>();
		//private ArrayList<Integer> candidate = new ArrayList<Integer>();
		private ArrayList<Integer> targets = new ArrayList<Integer>();
		
		private Hashtable<Integer, Double[]> MIIAArrScore = new Hashtable<Integer, Double[]>(); // node -> score array
		private double[] targetScore; // maximum remain score
		private ArrayList<Integer> elements = new ArrayList<Integer>(); // 
		private Double[] expectUsedRatios; // used ratio
		private Double[] currentTotalNbrScore; // total score for monte carlo times i
		
		
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
			
			/*Main function*/
			
			this.targets = targets;
			initRTV(iMt); // initialize values that dependent on target
			
			
			MiiaScore(iMt, MonteCarloTimes, k);
		}
		public void initRTV(InfMultiTarget im)
		{
			if(this.targets.size() == 0)
				System.out.println("Targets not initialize");
			double[] a = new double[this.targets.size()];
			this.currentTotalNbrScore = new Double[this.targets.size()]; 
			this.expectUsedRatios = new Double[this.targets.size()];
			
			for(int i = 0; i < this.targets.size(); i++)
				a[i] = 0.0;
			for(int i = 0; i < this.targets.size(); i++)
				for(int j = 0; j < im.getGraph().get(targets.get(i)).probability.size(); j++)
					a[i] += im.getGraph().get(targets.get(i)).probability.get(j);
			for(int i = 0; i < this.targets.size(); i++)
				this.currentTotalNbrScore[i] = 0.0;
			for(int i = 0; i < this.targets.size(); i++)
				this.expectUsedRatios[i] = 0.0;
			
			this.targetScore = a;
		}
		
		public void MiiaScore(InfMultiTarget imt, int monteCarloTimes, int k)  
		{
			for(int i = 1; i <= monteCarloTimes; i++)
			{
				imt.clearActResult();
				imt.createResult();
				
				for(int j = 0; j < this.targets.size();j++) // for all target calculate influence score
				{
					Hashtable<Integer, Double> mipHash = MIIAalg(this.targets.get(j), imt.getGraph(), j);
					for(Map.Entry<Integer, Double> e : mipHash.entrySet())
					{
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
			for(int i = 0; i < k; i++)
				System.out.print(findTopElement()+",");
			
		}
		
		/*public int sortTopElement()
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
		}*/
		public int findTopElement()
		{
			int topKey = -1;
			double topValue = Double.MIN_VALUE;
			for(int key : this.MIIAArrScore.keySet()) // for all key already scoring calculate the degree of importance of target 
			{
				if(!this.elements.contains(key)) // not top before
				{
					Double sum = 0.0;
					Double[] arr = this.MIIAArrScore.get(key);
					for(int i = 0; i < arr.length; i++)
						sum+=arr[i]*this.targetScore[i]*(1.0-this.expectUsedRatios[i]);
					if(sum>=topValue)
					{
						topKey = key;
						topValue = sum;
					}
				}
			}
			
			this.elements.add(topKey);
			
			Double[] maxArr = this.MIIAArrScore.get(topKey);
			
			/*System.out.println("Max value: "+topValue);
			
			System.out.println("\n  MaxArr = ");
			for(int j = 0; j < maxArr.length;j++)
				System.out.print(" "+maxArr[j]+", ");
			*/
			for(int i = 0; i < maxArr.length; i++)
			{
				double scale = maxArr[i]/this.currentTotalNbrScore[i];
				if(this.expectUsedRatios[i] + scale >= 1.0)
					this.expectUsedRatios[i] = 1.0;
				else
					this.expectUsedRatios[i] += scale;
			}
			/*System.out.println("\n  Use Ratio = ");
			for(int j = 0; j < this.expectUsedRatios.length;j++)
				System.out.print(" "+this.expectUsedRatios[j]+", ");
			*/
			
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
		/*public void cleatMIIAScore()
		{
			this.MIIAScore.clear();
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
			
		}*/
		
		public Hashtable<Integer, Double> MIIAalg(int targetID, Hashtable<Integer, MonteCarlo> Graph, int idIndex)  
		{
			HeurSoc hS = new HeurSoc(this.threshold);
			Hashtable<Integer, Double> miiaScore = new Hashtable<Integer, Double>();
			miiaScore.put(targetID, 0.0);
			//---
			ArrayList<Integer> neighbors = Graph.get(targetID).neighborID;
			ArrayList<Double> nbr_probability = Graph.get(targetID).activeProbability();
			Hashtable<Integer, Double> nbrHash = hS.arr2Hash(1.0, neighbors, nbr_probability);  // activate neighbors -> probability
			Hashtable<Integer, Double> Hash = new Hashtable<Integer, Double>();
			
			Hashtable<Integer, Double> nbrScore = new Hashtable<Integer,Double>();
			
			this.currentTotalNbrScore[idIndex] += (double)nbrHash.size();
			
			for(Map.Entry<Integer, Double> e : nbrHash.entrySet())
			{
				
				Hash.put(e.getKey(), e.getValue());
				//nbrHash.remove(e.getKey());
				while(Hash.size()!=0)
				{
					Hashtable<Integer, Double> tempHash = new Hashtable<Integer, Double>();
					int maxkey = hS.maxKey(Hash);
					miiaScore.put(maxkey, Hash.get(maxkey)); //put max every time
					tempHash = hS.arr2Hash(Hash.get(maxkey), Graph.get(maxkey).neighborID, Graph.get(maxkey).activeProbability());
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
				
				miiaScore.remove(targetID);
				
				
				for(int key : miiaScore.keySet())
				{
					if(!nbrScore.containsKey(key))
						nbrScore.put(key, 1.0);
					else
					{
						nbrScore.put(key, nbrScore.get(key)+1.0);
					}
				}
				miiaScore.clear();
				miiaScore.put(targetID, 0.0);
			}
			return nbrScore;
		}
		
		/*public void MiiaScoreUpdate(Hashtable<Integer, Double> scoreHash)
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
		}*/
		
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
			System.out.println("Heuristic 1 for multi-target");
			HeurMultiTarget3 hMt = new HeurMultiTarget3();
			
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

