import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerMain {
	public static int port = 7900;
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

	public static int MOD;

	public static final double probability = 0.2;

	public static void CreatePackets() throws IOException {
		packets = new ArrayList<DatagramPacket>();
		byte[] data = Functions.ReadFile();
		InetAddress ip = InetAddress.getByName("127.0.0.1");
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
					ClientMain.port));
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
		System.out.println("Enter Window Size");
		Scanner input = new Scanner(System.in);
		window = input.nextInt();
		MOD = 3 * window;
		CreatePackets();
		input.close();
		socket = new DatagramSocket(port);
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
			socket.receive(temp);
			int num = Functions.getSeqnum(temp);
			if (num == waitACK) {
				acked[waitACK] = true;
				System.out.println("Number Acked " + num);
				for (int i = 0; i < window && acked[waitACK]; ++i) {
					acked[waitACK] = false;
					timers[waitACK].interrupt();
					timers[waitACK].join();
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
					timers[num].interrupt();
					timers[num].join();
				}
			} else
				System.out.println("Duplicate Ack " + num);
		}
		socket.close();
		// The last part when to send the EOF
	}

}
