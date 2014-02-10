package SocExperiment;

import java.io.IOException;
import java.util.ArrayList;

public class EvaluationIMT {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ArrayList<Integer> TargetIDs = new ArrayList<Integer>(); //default target
		int MonteCarloTimes = 10000;
		String network = "Brightkite_edges.txt" , propnetwork = "Brightkite_edges_TV2.txt"; //default data
		int k = 10; //default
		String[] seedStr = "0, 17104, 20819, 13435, 44739".split(", ");
		String[] seedStr2 = "0, 1, 2, 3, 4".split(", ");
		String[] tarStr = "17089, 16712, 11572, 34024, 14156".split(", ");   //input target string
		
		for(int i = 0; i<tarStr.length; i++)
			TargetIDs.add(Integer.parseInt(tarStr[i]));
		
		if(args.length >= 1)
		{// add elements in args[0] to target list
			TargetIDs.clear();
			for(String target : args[0].split(","))
				TargetIDs.add(Integer.parseInt(target));
		}
		if(args.length >= 2)
			network = args[1];
		if(args.length >= 3)
			propnetwork = args[2];
		if(args.length >= 4)
			k = Integer.parseInt(args[3]);
		if(args.length >= 5)
			MonteCarloTimes = Integer.parseInt(args[4]);
		/*if(args.length >= 6)
			seedStr = args[5].split(",");
		if(args.length >= 7)
			seedStr2 = args[6].split(",");
		else
			seedStr2 = new String[0];*/
		
		
		double startTime, endTime, totalTime;
	
		startTime = System.currentTimeMillis();
		// Initial Setting
		InfMultiTarget iMt = new InfMultiTarget();
		if(!network.equals("Brightkite_edges.txt"))
			iMt.dataRead(network, true);
		else
			iMt.dataRead(network, false);
		
		iMt.setNodeset();
		iMt.ReadPropagate(propnetwork);  //set propagation probability
		
		iMt.info();
		/* Main Function */
	
				
		
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		ArrayList<Integer> seeds2 = new ArrayList<Integer>();
		for(int i = 0; i< seedStr.length; i++)
		{
			seeds.add(Integer.parseInt(seedStr[i]));
		}
		for(int i = 0; i< seedStr2.length; i++)
		{
			seeds2.add(Integer.parseInt(seedStr2[i]));
		}
		
		
		seeds = iMt.RandomTargets(0, 5);
		seeds2 = iMt.RandomTargets(0, 5);
		
		System.out.println("Targets: "+TargetIDs.toString());
		System.out.println("Seeds: "+seeds.toString());
		System.out.println("Seeds2: "+seeds2.toString());
		//d.setSeed(seeds);
		iMt.acceptanceEvaluation(TargetIDs, MonteCarloTimes, seeds, seeds2);
		
		
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("\nEvaluation Spend: " + totalTime/1000+" sec");

	}

}
