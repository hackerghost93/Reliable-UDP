import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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

	public static void CreatePackets() throws IOException {
		packets = new ArrayList<DatagramPacket>();
		byte[] data = Functions.ReadFile();
		final ByteBuffer buf = ByteBuffer.allocate(570);
		InetAddress ip = InetAddress.getByName("127.0.0.1");
		byte[] buffer = new byte[512];
		byte sequ = 0;
		for (int i = 0; i < data.length; ++i) {
			Arrays.fill(buffer, (byte) 0);
			buf.clear();
			buf.put(sequ);
			buffer[0]=buf.get();
			sequ++;
			sequ %= window << 1 | 1;
			int sz = 1;
			for (; sz < 512 && i < data.length; ++i,sz++)
				buf.put(data[i]);
			if (sz != 512)
				--i;
			packets.add(new DatagramPacket(buf.array(), buf.position(), ip, ClientMain.port));
		}
	}

	public synchronized static void SendPacket(int physicalNumber)
			throws IOException {
		// where the probability of error can occur
		socket.send(packets.get(physicalNumber));
	}

	private static void IncSeq() {
		++seqnum;
		seqnum %= window << 1 | 1;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Enter Window Size");
		Scanner input = new Scanner(System.in);
		window = input.nextInt();
		CreatePackets();
		input.close();
		socket = new DatagramSocket(port);
		acked = new boolean[window << 1 | 1];
		timers = new Thread[window << 1 | 1];
		for (fileIndex = 0; fileIndex < window && fileIndex < packets.size(); 
						++fileIndex) {
			System.out.println("send " + fileIndex);
			socket.send(packets.get(fileIndex));
	     	timers[seqnum]=new Thread(new Timer(packets.get(fileIndex), seqnum, fileIndex));
	     	timers[seqnum].start();
			IncSeq();
		}
		byte[] BUFFER = new byte[1];
		DatagramPacket temp = new DatagramPacket(BUFFER, BUFFER.length);
		while (fileIndex < packets.size()) {
			socket.receive(temp);
			int num = Functions.getSeqnum(temp);
			System.out.println("Number Acked " + num);
			if (!acked[num])
			{
				acked[num] = true;
				timers[num].interrupt();
			}
			else {
				System.out.println("Duplicate Ack " + num);
				continue;
			}
			if (num == waitACK) {
				while (acked[waitACK]) {
					socket.send(packets.get(fileIndex));
					System.out.println("send " + fileIndex);
					timers[waitACK].interrupt();
					timers[waitACK].join();
					acked[waitACK] = false;
					timers[seqnum] = 
							new Thread(new Timer(packets.get(fileIndex), seqnum, fileIndex));
					timers[seqnum].start();
					++fileIndex;
					IncSeq();
					++waitACK;
					waitACK %= window << 1 | 1;
				}
			}
		}
		socket.close();
		// The last part when to send the EOF
		for (int i = 0; i < packets.size(); i++) {
			byte arr[] = packets.get(i).getData();
			System.out.println(arr[0]);
		}
	}

}
