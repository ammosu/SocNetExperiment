package SocExperiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class Heur2Soc {
	
	private SortedSet<Map.Entry<Integer, Double>> NewScore = new TreeSet<Map.Entry<Integer, Double>>(
			new Comparator<Map.Entry<Integer, Double>>() {
				@Override
				public int compare(Map.Entry<Integer, Double> e1, Map.Entry<Integer, Double> e2) 
				{
					return e1.getValue().compareTo(e2.getValue());
				}
			});
	public ArrayList<Integer> splitTimesArr(int times, int k)
	{
		ArrayList<Integer> arr = new ArrayList<Integer>();
		int first = 0;
		if(k!=1)
			first = times/((int)Math.pow(2, k-1)-1);
		else
		{
			arr.add(times);
			return arr;
		}
		if(first!=0)
		{
			for(int i = 0; i < k; i++)
			{
				if(i!=k-1)
					arr.add(first*(int)Math.pow(2, i));
				else
					arr.add(times);
			}
		}
		else
		{
			first = 1;
			for(int i = 0; i < k; i++)
			{
				if(first*(int)Math.pow(2, i) <= times)
						arr.add(first*(int)Math.pow(2, i));
				else
				{
					int num = k - i;
					//System.out.println(num);
					int mul = (times-first*(int)Math.pow(2, i-1)) / num;
					for(int j = 1; j < num; j++)
					{
						arr.add((first*(int)Math.pow(2, i-1))+j*mul);
					}
					arr.add(times);
					break;
				}
			}
		}
		System.out.println(arr.toString());
		return arr;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		Heur2Soc t2 = new Heur2Soc();
		HeurSoc t = new HeurSoc(0.8);
		//t.miiaTimesSplit(1023, 5);
		
		
		Soc3 d = new Soc3();
		
		int influenceTargetID = 0; //default target
		int MonteCarloTimes = 200; //default times
		int k = 2; //default seed size
		String network = "Brightkite_edges.txt" , propnetwork = "Brightkite_edges_prop.txt"; //default data
		
		ArrayList<Integer> splitArr = t2.splitTimesArr(MonteCarloTimes, k);
		
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
		int splitIndex = 0;
		
		for(int i = 1; i <= MonteCarloTimes; i++)
		{
			d.clearActResult();
			d.createBinaryResult();
			t.MiiaScoreUpdate(t.MIIAalg2(0, d.getGraph()));
			if(i == splitArr.get(splitIndex))
			{
				splitIndex++;
				seeds.add(t.MiiaMaxSeed(seeds));
			}
			
		}
		
		/*for(int i = 0; i < k; i++)
			seeds.add(t.getseed());
		*/
		System.out.println("\nHeuristic algorithm 2:\n"+"Seed: " + seeds.toString());
	
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Execution Time: " + totalTime/1000+" sec");

		//evaluation
	
		/*
		d.setSeed(seeds);  //set our seed result 
		System.out.println("---Evaluation---\nExpected Times: ");
		System.out.println( d.MC_times(200,influenceTargetID));
		*/
	}

}
