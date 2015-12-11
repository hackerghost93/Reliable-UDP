import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientMain {

	public final static int port = 7777;
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
	
	private static byte IncSeq(byte seqnum)
	{
		return (byte)((++seqnum)%(2 * window +1));
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Enter the window size client");
		Scanner input = new Scanner(System.in);
		window = input.nextByte();
		input.close();
		packetsReceived = new DatagramPacket[2 * window + 1];
		socket = new DatagramSocket(port);
		byte[] buf = new byte[600];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		while (packet.getData().length != 0) {
			socket.receive(packet);
			byte seqnum = Functions.getSeqnum(packet);
			System.out.println("received " + seqnum);
			if (packetsReceived[seqnum] != null) {
				ACK(seqnum);
			}
			if (seqnum == awaiting) {
				// ADD TO BUFFER 
				Functions.WriteFile(packet.getData());
				ACK(seqnum);
				packetsReceived[seqnum] = null;
				awaiting = IncSeq(awaiting);
				while (packetsReceived[awaiting] != null) {
					// ADD TO BUFFER
					Functions.WriteFile(packetsReceived[awaiting].getData());
					packetsReceived[awaiting] = null;
					awaiting = IncSeq(awaiting);
				}
			} else {
				packetsReceived[seqnum] = packet;
				ACK(seqnum);
			}
		}
		socket.close();
	}

}
