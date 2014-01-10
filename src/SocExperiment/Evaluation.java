package SocExperiment;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Evaluation{

	/*public void serializeHash()
	{
		MyClass object1 = new MyClass("Hello", 32139604, 2.7e10);
        //System.out.println("Object 1: " + object1);
        FileOutputStream fos = new FileOutputStream("hashSerializable/");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object1);
        oos.flush();
        oos.close();
	}*/
	
	public static void main(String[] args) throws IOException {
		int influenceTargetID = 33043; //default target
		int MonteCarloTimes = 1000;
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
		seeds.add(0);
		//seeds.add(126556);
		//seeds.add(154258);
		//seeds.add(118667);
		//seeds.add(215179);
		//seeds.add(101215);
		//seeds.add(120044);
		//seeds.add(33971);
		//seeds.add(33043);
		//seeds.add(411025);
		//seeds.add(413808);
		//seeds.add(274042);
		//seeds.add(1);
		//seeds.add(403524);
		/**/
		d.setSeed(seeds);  //set our seed result 
		System.out.println("---Evaluation---\nExpected Times: ");
		System.out.println( d.MC_times(MonteCarloTimes,influenceTargetID));
		
		endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("\nEvaluation Spend: " + totalTime/1000+" sec");
	}

}
