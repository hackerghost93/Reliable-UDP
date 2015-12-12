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
	
	public static int MOD;

	public static void ACK(byte seqnum) throws IOException {
		byte[] BUFFER = new byte[1];
		final ByteBuffer buf = ByteBuffer.allocate(1);
		buf.put(seqnum);
		BUFFER[0] = buf.get(0);
		InetAddress ip = InetAddress.getByName("127.0.0.1");
		DatagramPacket packet = new DatagramPacket(BUFFER, BUFFER.length, ip,
				ServerMain.port);
		double num = Math.random();
		//if(num > ServerMain.probability) {
			socket.send(packet);
			System.out.println("Sending ack for " + seqnum);
		//}
	}

	private static void IncSeq() {
		++awaiting;
		awaiting %= MOD;
	}

	public static void main(String[] args) throws IOException {
		FileOutputStream output = new FileOutputStream("out.txt",false);
		socket = new DatagramSocket(port);
		System.out.println("Enter the window size client");
		Scanner input = new Scanner(System.in);
		window = input.nextByte();
		MOD = 3*window;
		input.close();
		packetsReceived = new DatagramPacket[MOD];
		while (true) {
			System.out.println("awaiting receive " + awaiting);
			byte[] buf = new byte[512];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			byte seqnum = Functions.getSeqnum(packet);
			System.out.println("received "+seqnum);
			if (seqnum == awaiting) {
				packetsReceived[awaiting] = new DatagramPacket(packet.getData()
						, packet.getLength(), packet.getAddress(), packet.getPort());
				for (int i = 0 ; i < window && packetsReceived[awaiting] != null ; ++i) {
					// ADD TO BUFFER
					byte[] wr = packetsReceived[awaiting].getData().clone();
					packetsReceived[awaiting] = null;
					System.out.println("sequ num = " + wr[0]);
					Functions.WriteFile(wr);
					ACK(awaiting);
					IncSeq();
				}
			} else if(seqnum > awaiting && 
					seqnum < (awaiting + window)%(MOD)) {
				if(packetsReceived[seqnum] == null) {
					packetsReceived[seqnum] = new DatagramPacket(packet.getData()
						, packet.getLength(), packet.getAddress(), packet.getPort());
				}
				ACK(seqnum);
			}
			if(false) break;
		}
		socket.close();
	}

}
