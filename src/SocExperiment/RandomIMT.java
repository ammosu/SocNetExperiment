package SocExperiment;

import java.io.IOException;
import java.util.ArrayList;

public class RandomIMT {
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int MonteCarloTimes = 200;
		int maxSteps = 4;
		
		ArrayList<Integer> Targets = new ArrayList<Integer>();
		int targetSize = 50; // default target size
		
		if(args.length >= 1)
			targetSize = Integer.parseInt(args[0]);
		
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
		
		Targets = iMt.RandomTargets(3, targetSize); // target at least 5 nbrs
		System.out.println("Targets: "+Targets);
		
		iMt.showInformation(Targets);  // show targets information
		
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
