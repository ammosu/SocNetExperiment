package SocExperiment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class NetworkCreate {

	public void set(String readPath, String WritePath)
	{
		Soc3 s = new Soc3();
		s.dataRead(readPath, false); //Brightkite => false
		s.setNodeset();
		s.setPropagateGraph();
		//s.WritePropagateUniform(path2);
		s.WritePropagateTriValue(WritePath);
	}
	public void smallDataPrint(String Filename, int maxID) //Read text and build network
	{
		int lineCount = 0;
		FileReader FileStream;
		try {
			FileStream = new FileReader(Filename);
			BufferedReader BufferedStream = new BufferedReader(FileStream);
			try {
				do{
					
					String readline = BufferedStream.readLine();
					String[] readlines = readline.split("\t");
					
					lineCount ++ ;
					if(Integer.parseInt(readlines[0])<=maxID && Integer.parseInt(readlines[1])<=maxID)
					System.out.println(Integer.parseInt(readlines[0])+"\t"+ Integer.parseInt(readlines[1]));
					
				}
				while(BufferedStream.ready());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Read Fininsh!!\n total lines:"+lineCount); /**/
	}
	public static void main(String[] args) {
		
		NetworkCreate netcreate = new NetworkCreate();
		
		String networkPath = "com-dblp.ungraph.txt";
		String targetPath = "C:\\Users\\mosu\\Desktop\\prop_TV2.txt";
		
		netcreate.set(networkPath, targetPath);
		//netcreate.smallDataPrint("Brightkite_edges.txt", 20);
	}

}
