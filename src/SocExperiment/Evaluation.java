package SocExperiment;

import java.io.IOException;
//import java.io.Serializable;
import java.util.ArrayList;

public class Evaluation{

	
	
	public static void main(String[] args) throws IOException {
		int influenceTargetID = 0; //default target
		int MonteCarloTimes = 10000;
		String network = "Brightkite_edges.txt" , propnetwork = "Brightkite_edges_prop.txt"; //default data
		int k = 10; //default
		
		double startTime, endTime, totalTime;
	
		// Initial Setting
		Soc3 d = new Soc3();
		d.dataRead(network,false);
		d.setNodeset();
		d.ReadPropagate(propnetwork);  //set propagation probability
		d.setInEdgeGraph();  //set in edge weight from propagation graph
		d.trim();
		d.info();
		/* Main Function */
	
		startTime = System.currentTimeMillis();
		
	
		d.showInformation(influenceTargetID, k, MonteCarloTimes);
		startTime = System.currentTimeMillis();
		
		String seedStr[] = "31".split(", ");   //input seed string
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		seeds.clear();
		for(int i = 0; i< seedStr.length; i++)
		{
			seeds.add(Integer.parseInt(seedStr[i]));
		}
		
		d.setSeed(seeds);
		d.acceptanceEvaluation(influenceTargetID, MonteCarloTimes, seeds, seeds);
		
		/*
		d.setSeed(seeds);  //set our seed result 
		System.out.println("---Evaluation---\nExpected Times: ");
		System.out.println( d.MC_acceptanceTimes(MonteCarloTimes,influenceTargetID));
		*/
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("\nEvaluation Spend: " + totalTime/1000+" sec");
	}

}
