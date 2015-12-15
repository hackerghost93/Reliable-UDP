import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientMain {

	public static int serverPort;
	public static int clientPort;
	
	public static DatagramSocket socket;
	static byte awaiting;
	static byte window;
	static DatagramPacket[] packetsReceived;
	static ByteBuffer bufferR = ByteBuffer.allocate(570);

	static ArrayList<DatagramPacket> ls;

	public static int MOD;
	
	public static InetAddress ip;
	public static double probability;

	public static void ACK(byte seqnum) throws IOException {
		byte[] BUFFER = new byte[1];
		final ByteBuffer buf = ByteBuffer.allocate(1);
		buf.put(seqnum);
		BUFFER[0] = buf.get(0);
		DatagramPacket packet = new DatagramPacket(BUFFER, BUFFER.length, ip,
				serverPort);
		double num = Math.random();
		 if(num > probability) {
			 socket.send(packet);
			System.out.println("Sending ack for " + seqnum);
		 }
	}

	private static void IncSeq() {
		++awaiting;
		awaiting %= MOD;
	}

	public static void main(String[] args) throws IOException {
		System.out.println("\t\tWelcome to Client\n\n");
		if(!Functions.initClient()) {
			System.out.println("\n\n\t looks like your config file is " + 
								"not formated as we expect");
			return;
		}
		FileOutputStream output = new FileOutputStream("out.txt", false);
		socket = new DatagramSocket(clientPort);
		MOD = 3 * window;
		packetsReceived = new DatagramPacket[MOD];
		while (true) {
			System.out.println("awaiting receive " + awaiting);
			byte[] buf = new byte[512];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			byte seqnum = Functions.getSeqnum(packet);
			System.out.println("received " + seqnum);
			if(seqnum == awaiting) {
				packetsReceived[awaiting] = new DatagramPacket(
						packet.getData(), packet.getLength(),
						packet.getAddress(), packet.getPort());
				ACK(awaiting);
				for (int i = 0; i < window && packetsReceived[awaiting] != null
												; ++i) {
					byte[] wr = packetsReceived[awaiting].getData();
					packetsReceived[awaiting] = null;
					System.out.println("sequ num = " + wr[0]);
					Functions.WriteFile(wr);
					//ACK(awaiting);
					IncSeq();
				}
			} else if(seqnum > awaiting
					&& seqnum < (awaiting + window) % (MOD)) {
					packetsReceived[seqnum] = new DatagramPacket(
							packet.getData(), packet.getLength(),
							packet.getAddress(), packet.getPort());
				ACK(seqnum);
			}
			else if(seqnum <= (awaiting+MOD -1) % MOD && seqnum >=(awaiting+MOD-window)%MOD)
				ACK(seqnum);
			else if((awaiting + MOD -1) %MOD < 5 && seqnum >= (MOD-window+awaiting)%MOD)
				ACK(seqnum);
				
			if (false)
				break;
		}
		socket.close();
	}

}
