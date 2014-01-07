package SocExperiment;

import java.io.IOException;
import java.util.ArrayList;

public class Evaluation {

	public static void main(String[] args) throws IOException {
		int influenceTargetID = 59263; //default target
		int MonteCarloTimes = 10;
		String network = "com-dblp.ungraph.txt" , propnetwork = "prop-O.txt"; //default data
		int k = 10; //default
		
		double startTime, endTime, totalTime;
	
		// Initial Setting
		Soc3 d = new Soc3();
		d.dataRead(network);
		d.setNodeset();
		d.ReadPropagate(propnetwork);  //set propagation probability
		d.setInEdgeGraph();  //set in edge weight from propagation graph
		d.trim();
		d.info();
		/* Main Function */
	
		startTime = System.currentTimeMillis();
		
	
		d.showInformation(influenceTargetID, k, MonteCarloTimes);
		startTime = System.currentTimeMillis();
		
		ArrayList<Integer> seeds = new ArrayList<Integer>();
		seeds.clear();
		seeds.add(101215);
		seeds.add(120044);
		seeds.add(33971);
		seeds.add(33043);
		seeds.add(411025);
		//seeds.add(413808);
		//seeds.add(274042);
		//seeds.add(1);
		//seeds.add(403524);
		/**/
		d.setSeed(seeds);  //set our seed result 
		System.out.println("---Evaluation---\nExpected Times: ");
		System.out.println( d.MC_times(20,influenceTargetID));
		
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("\nEvaluation Spend: " + totalTime/1000+" sec");
	}

}
