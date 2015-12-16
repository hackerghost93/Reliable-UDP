package GoBackN;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
	public static int clientPort;
	public static int serverPort;
	public static int window;
	public static DatagramSocket socket;
	public static boolean acked[];
	private static Thread mytimer = null;
	public static int pointerNext;
	static ArrayList<DatagramPacket> packets;
	static byte[] data;
	private static byte waitACK = 0;
	private static int fileIndex = 0;
	private static int currentFileIndex = 0 ;
	
	public static InetAddress ip;
	
	public static double probability;
	
	public static byte MOD ;
	
	public static byte[] ReadFile() throws IOException {
		byte[] data;
		Path path = Paths.get("output.txt");
		data = Files.readAllBytes(path);
		return data;
	}
	
	public static synchronized byte getSeqnum(DatagramPacket packet) {
		return packet.getData()[0];
	}
	
	public static void CreatePackets() throws IOException {
		packets = new ArrayList<DatagramPacket>();
		byte[] data = ReadFile();
		byte sequ = 0;
		for(int i = 0; i < data.length;) {
			byte[] buffer = new byte[512];
			buffer[0] = sequ;
			sequ++;
			sequ %= MOD;
			int sz = 1;
			for (; sz < 512 && i < data.length; ++i, sz++)
				buffer[sz] = data[i];
			packets.add(new DatagramPacket(buffer, buffer.length, ip,
					clientPort));
		}
	}
	
	public synchronized static void SendPacket(int physicalNumber)
			throws IOException {
		double num = Math.random();
		if(num > probability) {
			socket.send(packets.get(physicalNumber));
			System.out.println("send packet "
					+ packets.get(physicalNumber).getData()[0]);
		} else
			System.out.println("Drop packet " + packets.get(physicalNumber).getData()[0]);
	}
	
	private static void IncACK() {
		++waitACK;
		waitACK %= MOD;
	}
	
	
	public static void main(String[] args) throws IOException {
		System.out.println("Server Window size");
		if(!Functions.initServer()) {
			System.out.println("\n\n\t looks like your config file is " + 
								"not formated as we expect");
			return;
		}
		MOD = (byte) (3*window) ;
		CreatePackets();
		socket = new DatagramSocket(serverPort);
		acked = new boolean[MOD];
		
		for(fileIndex = 0; fileIndex < window && fileIndex < packets.size();
				++fileIndex) {
			SendPacket(fileIndex);
		}
		mytimer = new Thread(new Timer(waitACK,currentFileIndex));
		mytimer.start();
		byte[] BUFFER = new byte[15];
		DatagramPacket packet = new DatagramPacket(BUFFER,BUFFER.length);
		byte num ;
		while(currentFileIndex < packets.size())
		{
			System.out.println("waiting ACK of " + waitACK);
			socket.receive(packet);
			num = getSeqnum(packet);
			System.out.println("ACK for "+ num+" awaiting " + waitACK);
			mytimer.interrupt();
			while(waitACK != num)
			{
				IncACK();
				currentFileIndex++;
				if(fileIndex < packets.size())
					SendPacket(fileIndex++);
			}
			mytimer = new Thread(new Timer(waitACK,currentFileIndex));
			mytimer.start();
		}
		DatagramPacket p = new DatagramPacket(null,0,ip,clientPort);
		socket.send(p);
		
		socket.close();	
	}

}