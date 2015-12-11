import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientMain {

	public final static int port = 5500;
	public static DatagramSocket socket;
	static byte awaiting;
	static byte window;
	static DatagramPacket[] packetsReceived;

	public static void ACK(byte seqnum) throws IOException {
		byte[] BUFFER = new byte[1];
		BUFFER[0] = seqnum;
		InetAddress ip = InetAddress.getByName("127.0.0.1");
		DatagramPacket packet = new DatagramPacket(BUFFER, BUFFER.length, ip,
				ServerMain.port);
		socket.send(packet);
	}

	private static void IncSeq() {
		++awaiting;
		awaiting %= window << 1 | 1;
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Enter the window size client");
		Scanner input = new Scanner(System.in);
		window = input.nextByte();
		input.close();
		packetsReceived = new DatagramPacket[2 * window + 1];
		socket = new DatagramSocket(port);
		byte[] buf = new byte[512];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		while (packet.getData().length != 0) {
			socket.receive(packet);
			byte seqnum = Functions.getSeqnum(packet);
			if (packetsReceived[seqnum] != null) {
				ACK(seqnum);
			}
			if (seqnum == awaiting) {
				// ADD TO BUFFER
				Functions.WriteFile(packet.getData());
				ACK(seqnum);
				while (packetsReceived[awaiting] != null) {
					// ADD TO BUFFER
					Functions.WriteFile(packetsReceived[awaiting].getData());
					packetsReceived[awaiting] = null;
					IncSeq();
				}
			} else {
				byte[] data = packet.getData();
				packetsReceived[seqnum] = new DatagramPacket(data, data.length);
				ACK(seqnum);
			}
		}
		socket.close();
	}

}
