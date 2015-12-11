import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientMain {

	public final static int port = 7666;
	public static DatagramSocket socket;
	static byte awaiting;
	static byte window;
	static DatagramPacket[] packetsReceived;
	static ByteBuffer bufferR = ByteBuffer.allocate(570);
	
	static ArrayList<DatagramPacket> ls;

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
		FileOutputStream output = new FileOutputStream("out.txt",false);
		socket = new DatagramSocket(port);
		System.out.println("Enter the window size client");
		Scanner input = new Scanner(System.in);
		window = input.nextByte();
		input.close();
		packetsReceived = new DatagramPacket[2 * window + 1];
		while (true) {
			System.out.println("awaiting receive");
			byte[] buf = new byte[512];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			byte seqnum = Functions.getSeqnum(packet);
			ACK(seqnum);
			System.out.println("received "+seqnum);
			if (seqnum == awaiting) {
				packetsReceived[awaiting] = new DatagramPacket(packet.getData()
						, packet.getLength(), packet.getAddress(), packet.getPort());
				while (packetsReceived[awaiting] != null) {
					// ADD TO BUFFER
					byte[] wr = packetsReceived[awaiting].getData().clone();
					System.out.println("sequ num = " + wr[0]);
					Functions.WriteFile(wr);
					packetsReceived[awaiting] = null;
					IncSeq();
				}
			} else if(seqnum > awaiting && 
					seqnum < (awaiting + window)%(window << 1 | 1)) {
				packetsReceived[seqnum] = new DatagramPacket(packet.getData()
						, packet.getLength(), packet.getAddress(), packet.getPort());;
			} else packetsReceived[seqnum] = null;
			if(false) break;
		}
		socket.close();
	}

}
