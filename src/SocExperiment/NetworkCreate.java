package SocExperiment;

public class NetworkCreate {

	public void set(String path1, String path2)
	{
		Soc3 s = new Soc3();
		s.dataRead(path1, false); //Brightkite => false
		s.setNodeset();
		s.setPropagateGraph();
		//s.WritePropagateUniform(path2);
		s.WritePropagateTriValue(path2);
	}
	
	public static void main(String[] args) {
		
		NetworkCreate netcreate = new NetworkCreate();
		
		String networkPath = "C:\\Users\\mosu\\Desktop\\Brightkite_edges.txt";
		String targetPath = "C:\\Users\\mosu\\Desktop\\Brightkite_edges_TV.txt";
		
		netcreate.set(networkPath, targetPath);
		
	}

}
