package GoBackN;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
	public static int serverPort = 7672;
	public static int clientPort;
	public static int window;
	public static DatagramSocket socket;
	public static boolean acked[];
	public static int pointerNext;
	static ArrayList<DatagramPacket> packets;
	static byte[] data;
	private static int fileIndex = 0;
	private static int last_rec;

	public static double probability;

	public static InetAddress ip;

	public static byte MOD;

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
		for (int i = 0; i < data.length;) {
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
		if (num > probability) {
			socket.send(packets.get(physicalNumber));
			System.out.println("send packet "
					+ packets.get(physicalNumber).getData()[0]);
		} else System.out.println("drop packet " + 
					packets.get(physicalNumber).getData()[0]);
	}
	
	private static int in_range(int rec) {
		for(int i = last_rec, j = 0 ; j < window ; ++j) {
			if(i == rec) return j+1;
			++i;
			i %= MOD;
		}
		return -1;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("\t\tWelcome to Server\n\n");
		if (!Functions.initServer()) {
			System.out.println("\n\n\t looks like your config file is "
					+ "not formated as we expect");
			return;
		}
		MOD = (byte) (3 * window);
		CreatePackets();
		socket = new DatagramSocket(serverPort);
		for (fileIndex = 0; fileIndex < window && fileIndex < packets.size(); ++fileIndex) {
			SendPacket(fileIndex);
		}
		fileIndex = 0;
		last_rec = 0;
		int t = 0, o = -1;
		acked = new boolean[MOD];
		byte[] BUFFER = new byte[15];
		DatagramPacket packet = new DatagramPacket(BUFFER, BUFFER.length);
		byte num;
		while (fileIndex < packets.size()) {
			socket.receive(packet);
			num = getSeqnum(packet);
			System.out.println("Received ACK for " + num);
			if(num < o) ++t;// add 1 to cycles
			o = num; // new offset
			for(int i = t*MOD + o + 1, j = 0 ; i < packets.size() && 
								j < window ; ++j, ++i)
				SendPacket(i);
		}
		socket.close();
	}

}
