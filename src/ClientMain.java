import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientMain {

	public final static int port = 7777;
	static DatagramSocket socket;
	static int awaiting;
	static int window;
	static DatagramPacket[] packetsReceived;

	public static void ACK(int seqnum) throws IOException {
		byte[] BUFFER = new byte[1];
		BUFFER[0] = (byte) seqnum;
		InetAddress ip = InetAddress.getByName("127.0.0.1");
		DatagramPacket packet = new DatagramPacket(BUFFER, BUFFER.length, ip,
				port);
		socket.send(packet);
	}

	public static void main(String[] args) throws IOException {
		Scanner input = new Scanner(System.in);
		window = input.nextInt();
		input.close();
		packetsReceived = new DatagramPacket[2 * window + 1];
		socket = new DatagramSocket(port);
		DatagramPacket packet = null;
		while (true) {
			socket.receive(packet);
			int seqnum = Functions.getSeqnum(packet);
			if (packetsReceived[seqnum] != null) {
				ACK(seqnum);
			}
			if (seqnum == awaiting) {
				// ADD TO BUFFER
				ACK(seqnum);
				packetsReceived[seqnum] = null;
				awaiting = (++awaiting) % (2 * window + 1);
				while (packetsReceived[awaiting] != null) {
					// ADD TO BUFFER
					packetsReceived[awaiting] = null;
					awaiting = (++awaiting) % (2 * window + 1);
				}
			} else {
				packetsReceived[seqnum] = packet;
				ACK(seqnum);
			}
		}

	}

}
