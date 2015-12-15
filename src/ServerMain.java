import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerMain {
	
	public static int window;
	public static DatagramSocket socket;
	public static boolean acked[];
	public static Thread timers[];
	public static int pointerNext;
	static ArrayList<DatagramPacket> packets;
	static byte[] data;
	private static int waitACK = 0;
	private static int seqnum = 0;
	private static int fileIndex = 0;
	public static InetAddress ip;

	public static int MOD;

	public static double probability;
	
	public static int serverPort;
	public static int clientPort;

	public static void CreatePackets() throws IOException {
		packets = new ArrayList<DatagramPacket>();
		byte[] data = Functions.ReadFile();
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
		// where the probability of error can occur
		double num = Math.random();
		if(num > probability) {
			socket.send(packets.get(physicalNumber));
			System.out.println("send packet "
					+ packets.get(physicalNumber).getData()[0]);
		}
	}

	private static void IncSeq() {
		++seqnum;
		seqnum %= MOD;
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		System.out.println("\t\tWelcome to Server\n\n");
		if(!Functions.initServer()) {
			System.out.println("\n\n\t looks like your config file is " + 
								"not formated as we expect");
			return;
		}
		MOD = 3 * window;
		CreatePackets();
		socket = new DatagramSocket(serverPort);
		acked = new boolean[MOD];
		timers = new Thread[MOD];
		for(fileIndex = 0; fileIndex < window && fileIndex < packets.size();
				++fileIndex) {
			SendPacket(fileIndex);
			timers[seqnum] = new Thread(new Timer(seqnum, fileIndex));
			timers[seqnum].start();
			IncSeq();
		}
		while(fileIndex < packets.size()) {
			byte[] BUFFER = new byte[1];
			DatagramPacket temp = new DatagramPacket(BUFFER, BUFFER.length);
			for(int i = 0 ; i < MOD ;i++)
			{
				if(!(i >= waitACK && i < (waitACK + window) % (MOD)))
					acked[i] = false ;
			}
			System.out.println("wait Ack " + waitACK);
			SendPacket(fileIndex-window);
			socket.receive(temp);
			int num = Functions.getSeqnum(temp);
			if (num == waitACK) {
				acked[waitACK] = true;
				System.out.println("Number Acked " + num);
				for (int i = 0; i < window && acked[waitACK]; ++i) {
					if(timers[waitACK]!=null)
						if(timers[waitACK].isAlive())
						{
							timers[waitACK].interrupt();
							timers[num].join();
							timers[num] = null;
						}
					acked[waitACK] = false;
					SendPacket(fileIndex);
					timers[seqnum] = new Thread(new Timer(seqnum, fileIndex));
					timers[seqnum].start();
					++fileIndex;
					IncSeq();
					++waitACK;
					waitACK %= MOD;
				}
			} else if(num > waitACK && num < (waitACK + window) % (MOD)) {
				if(!acked[num]) {
					acked[num] = true;
					System.out.println("Number Acked " + num);
					if(timers[waitACK].isAlive())
					{
						timers[num].interrupt();
						timers[num].join();
						timers[num] = null;
					}
				}
			} else
			{
				System.out.println("Duplicate Ack " + num);
			}
			
		}
		// The last part when to send the EOF
		while(true) {
			byte[] BUFFER = new byte[1];
			DatagramPacket temp = new DatagramPacket(BUFFER, BUFFER.length);
			socket.setSoTimeout(20000);
			try {
				socket.receive(temp);
			} catch(SocketTimeoutException e) {
				break;
			}
			int sqnum = Functions.getSeqnum(temp);
			System.out.println("Number Acked " + sqnum);
			acked[seqnum] = true;
			if(timers[waitACK].isAlive()) {
				timers[seqnum].interrupt();
				timers[seqnum].join();
			}
		}
		System.out.println("\n\n\t\t File is sent Successfully");
		socket.close();
		System.exit(1);
	}

}
