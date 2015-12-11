import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ServerMain {
	public static int port = 7891;
	public static int window;
	public static DatagramSocket socket;
	public static boolean acked[];
	public static int pointerNext;
	static ArrayList<DatagramPacket> packets;
	static byte[] data;
	private static int waitACK = 0;
	private static int seqnum = 0 ;
	private static int fileIndex =0;
	

	public static void CreatePackets() throws IOException {
		packets = new ArrayList<DatagramPacket>();
		byte[] data = Functions.ReadFile();
		InetAddress ip = InetAddress.getByName("127.0.0.1");
		byte[] buffer = new byte[512];
		byte sequ = 0;
		for (int i = 0; i < data.length; ++i) {
			Arrays.fill(buffer, (byte) 0);
			buffer[0] = sequ++;
			sequ %= window << 1 | 1;
			System.out.println(buffer[0]);
			for (int j = 1; j < 512 && i < data.length; ++i, ++j)
				buffer[j] = data[i];
			packets.add(new DatagramPacket(buffer, buffer.length, ip, ClientMain.port));
			if (i != data.length)
				--i;
		}
		// lama ba7ot el rakam fel packet beybawaz el denya
		byte[] x;
			x = packets.get(0).getData();
			for(int j = 0 ; j < x.length ; j++)
				System.out.print(x[j]);
			System.out.println("");
			Functions.WriteFile(x);
			x = packets.get(1).getData();
			for(int j = 0 ; j < x.length ; j++)
				System.out.print(x[j]);
			System.out.println("");
			Functions.WriteFile(x);
	}

	public synchronized static void SendPacket(int physicalNumber) throws IOException {
		// where the probability of error can occur
		socket.send(packets.get(physicalNumber));
	}
	
	private static int IncSeq(int seqnum)
	{
		return ((++seqnum)%(2 * window +1));
	}

	public static void main(String[] args) throws IOException {
//		socket = new DatagramSocket(port);
		System.out.println("Enter Window Size");
		Scanner input = new Scanner(System.in);
		window = input.nextInt();
		acked = new boolean[2 * window + 1];
		input.close();
		CreatePackets();
//		for(fileIndex = 0 ; fileIndex < window && fileIndex < packets.size() ; fileIndex++)
//		{
//			socket.send(packets.get(fileIndex));
//			System.out.println("send " + fileIndex);
//			new Thread(new Timer(packets.get(fileIndex),seqnum,fileIndex)).start();
//			IncSeq(seqnum);
//		}
//		byte[] BUFFER = new byte[1];
//		DatagramPacket temp = new DatagramPacket(BUFFER,BUFFER.length);
//		while(fileIndex < packets.size())
//		{
//			socket.receive(temp);
//			int num = Functions.getSeqnum(temp);
//			System.out.println("Number Acked " + num);
//			if(!acked[num])
//				acked[num] = true ;
//			else
//			{
//				System.out.println("Duplicate Ack " + num);
//				continue ;		
//			}
//			if(num == waitACK)
//			{
//				while(acked[num])
//				{
//					socket.send(packets.get(fileIndex));
//					System.out.println("send " + fileIndex);
//					acked[waitACK] = false ;
//					new Thread(new Timer(packets.get(fileIndex),seqnum,fileIndex));
//					fileIndex++;
//					IncSeq(seqnum);
//					IncSeq(waitACK);
//				}
//			}
//		}
//		socket.close();
//		// The last part when to send the EOF 
//		for(int i = 0 ; i < packets.size(); i++)
//		{
//			byte arr[] = packets.get(i).getData();
//			System.out.println(arr[0]);
//		}
	}

}
