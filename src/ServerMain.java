import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ServerMain {
	static int port = 7891 ; 
	int seqnum = 0 ;
	static DatagramSocket socket ;
	static DatagramPacket packets[] = null ;
    static byte[] data = null ;
	public static byte[] ReadFile() throws IOException
	{
		byte[] data = null;
		Path path = Paths.get("output.txt");
		data = Files.readAllBytes(path);
		return data ;
	}
	
	public DatagramPacket getPacket(byte[] data)
	{
		if(seqnum == data.length)
			return null ;
		
		byte[] buffer = new byte[512];
		for(int i = seqnum ; i < data.length || (seqnum%512==0);i++)
		{
			
		}
		
		DatagramPacket packet = new DatagramPacket()
	}
	
	
	
	public static void main(String[] args) throws IOException {
		socket = new DatagramSocket(port);
		InetAddress ip = InetAddress.getByName("127.0.0.1");
		packet = new DatagramPacket(BUFFER , BUFFER.length,ip,port);
	}
	
}
