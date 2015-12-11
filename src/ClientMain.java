import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class ClientMain {

	public final static int port = 7666;
	public static DatagramSocket socket;
	static byte awaiting;
	static byte window;
	static DatagramPacket[] packetsReceived;
	static ByteBuffer bufferR = ByteBuffer.allocate(570);

	public static void ACK(byte seqnum) throws IOException {
		byte[] BUFFER = new byte[1];
		final ByteBuffer buf = ByteBuffer.allocate(1);
		buf.put(seqnum);
		BUFFER[0] = buf.get(0);
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
		socket = new DatagramSocket(port);
		System.out.println("Enter the window size client");
		Scanner input = new Scanner(System.in);
		window = input.nextByte();
		input.close();
		packetsReceived = new DatagramPacket[2 * window + 1];
		byte[] buf = new byte[512];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		while (packet.getData().length != 0) {
			System.out.println("awaiting receive");
			bufferR.clear();
			socket.receive(packet);
			byte seqnum = Functions.getSeqnum(packet);
			System.out.println("received "+seqnum);
			if (packetsReceived[seqnum] != null) {
				ACK(seqnum);
			}
			if (seqnum == awaiting) {
				// ADD TO BUFFER
				bufferR.put(packet.getData());
				Functions.WriteFile(bufferR.array());
				ACK(seqnum);
				while (packetsReceived[awaiting] != null) {
					// ADD TO BUFFER
					bufferR.put(packetsReceived[awaiting].getData());
					Functions.WriteFile(bufferR.array());
					packetsReceived[awaiting] = null;
					IncSeq();
				}
			} else {
				Functions.WriteFile(packet.getData());
				packetsReceived[seqnum] = packet ;
				ACK(seqnum);
			}
		}
		socket.close();
	}

}
