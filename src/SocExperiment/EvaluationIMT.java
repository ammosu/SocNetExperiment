package SocExperiment;

import java.io.IOException;
import java.util.ArrayList;

public class EvaluationIMT {

	public void mainProcess(int maxStep, int MonteCarloTimes, String network, String propNetwork, ArrayList<Integer> targets, ArrayList<Integer> seed1,ArrayList<Integer> seed2) throws IOException
	{
		double startTime, endTime, totalTime;
		
		startTime = System.currentTimeMillis();
		// Initial Setting
		InfMultiTarget iMt = new InfMultiTarget();
		if(!network.equals("Brightkite_edges.txt"))
			iMt.dataRead(network, true);
		else
			iMt.dataRead(network, false);
		
		iMt.setNodeset();
		iMt.ReadPropagate(propNetwork, 0);  //set propagation probability
		
		iMt.info();
		/* Main Function */
		//
		System.out.println("Max Step:"+maxStep);
		System.out.println("Targets: "+targets.toString());
		System.out.println("Seeds: "+seed1.toString());
		System.out.println("Seeds2: "+seed2.toString());
		//d.setSeed(seeds);
		if(seed2.size()!=0)
		{
			System.out.println("2");
			iMt.acceptanceEvaluation(targets, MonteCarloTimes, seed1, seed2, maxStep);
		}
		else
			iMt.acceptanceEvaluation(targets, MonteCarloTimes, seed1, maxStep);
		
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("\nEvaluation Spend: " + totalTime/1000+" sec");

	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ArrayList<Integer> TargetIDs = new ArrayList<Integer>(); //default target
		
		int maxSteps = 3;
		int MonteCarloTimes = 1000;
		String network = "com-dblp.ungraph.txt" , propnetwork = "prop_dblp"; //default data
		int k = 9; //default
		String[] seedStr = "37678,14662,173256,33126,49097".split(",");
		String[] seedStr2 = "14662,10971,74728,37678,33316".split(",");
		String[] tarStr = "0,1,269,304".split(",");   //input target string
		
		for(int i = 0; i< tarStr.length; i++)
			TargetIDs.add(Integer.parseInt(tarStr[i]));
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		ArrayList<Integer> seeds2 = new ArrayList<Integer>();
		

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
		if(args.length >= 6)
			seedStr = args[5].split(",");
		if(args.length >= 7)
			seedStr2 = args[6].split(",");
		if(args.length >= 8)
			maxSteps = Integer.parseInt(args[7]);
		/*else
			seedStr2 = new String[0];*/

		if(args.length <= 5)
		{
			for(int i = 0; i< seedStr.length; i++)
				seeds.add(Integer.parseInt(seedStr[i]));
			for(int i = 0; i< seedStr2.length; i++)
				seeds2.add(Integer.parseInt(seedStr2[i]));
		}
		else
		{
			if(args.length >= 6)
				for(String s : seedStr)
					seeds.add(Integer.parseInt(s));
			if(args.length >= 7)
				for(String s : seedStr2)
					seeds2.add(Integer.parseInt(s));
		}
		
		
		EvaluationIMT evaImt = new EvaluationIMT();
		//for(int i = 2; i <= maxSteps ;i++)
		evaImt.mainProcess(maxSteps, MonteCarloTimes, network, propnetwork, TargetIDs, seeds, seeds2);

	}

}
