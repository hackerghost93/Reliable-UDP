import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;


public class ServerMain {
	public static int port = 7891 ;
	public static int window ;
	public static DatagramSocket socket ;
    public static boolean acked[] ;
    public static int pointerNext ;
    static ArrayList<DatagramPacket> packets ;
    static byte[] data ;
    int lastACK  ;
    
	
	public static void CreatePackets(byte[] data) throws UnknownHostException
	{
		// first byte = sequence number 
		// sequence number increment and % 2window+1
		// write code here
		// buffer to put in the data of each packet
		// you need to loop here and push into the list
		byte[] BUFFER = new byte[512];
		// for local host IP
		InetAddress ip = InetAddress.getByName("127.0.0.1");
		// create packet 
		DatagramPacket packet= new DatagramPacket(BUFFER , BUFFER.length,ip,port);
	}	
	
	public static void SendPacket(int physicalNumber) throws IOException
	{
		// where the probability of error can occur
		socket.send(packets.get(physicalNumber));
	}
	
	
	
	public static void main(String[] args) throws IOException {
		socket = new DatagramSocket(port);
		System.out.println("Enter Window Size");
		Scanner input = new Scanner(System.in);		
		window = input.nextInt();
		acked  = new boolean[2*window+1];
		input.close();
	}
	
}
