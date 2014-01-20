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
		String[] seedStr = "13924, 30888, 33963, 13939, 13909, 10599, 13931, 8675, 33533, 13928".split(", ");
		
		if(args.length >= 1)
			influenceTargetID = Integer.parseInt((args[0]));
		if(args.length >= 2)
			network = args[1];
		if(args.length >= 3)
			propnetwork = args[2];
		if(args.length >= 4)
			k = Integer.parseInt(args[3]);
		if(args.length >= 5)
			MonteCarloTimes = Integer.parseInt(args[4]);
		if(args.length >= 6)
			seedStr = args[5].split(",");
		
		
		
		double startTime, endTime, totalTime;
	
		// Initial Setting
		Soc3 d = new Soc3();
		if(network!="Brightkite_edges.txt")
			d.dataRead(network, true);
		else
			d.dataRead(network, false);
		d.setNodeset();
		d.ReadPropagate(propnetwork);  //set propagation probability
		d.setInEdgeGraph();  //set in edge weight from propagation graph
		d.trim();
		d.info();
		/* Main Function */
	
		startTime = System.currentTimeMillis();
		
	
		d.showInformation(influenceTargetID, k, MonteCarloTimes);
		startTime = System.currentTimeMillis();
		
		//seedStr = "0, 126556, 154258, 118667, 215179".split(", ");   //input seed string
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		seeds.clear();
		for(int i = 0; i< seedStr.length; i++)
		{
			seeds.add(Integer.parseInt(seedStr[i]));
		}
		System.out.println("Seeds: "+seeds.toString());
		d.setSeed(seeds);
		d.acceptanceEvaluation(influenceTargetID, MonteCarloTimes, seeds, seeds);
		
		
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("\nEvaluation Spend: " + totalTime/1000+" sec");
	}

}
