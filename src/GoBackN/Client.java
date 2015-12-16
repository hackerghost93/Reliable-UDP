package GoBackN;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.zip.CRC32;

public class Client {
	
	public static int clientPort;
	public static int serverPort;
	public static DatagramSocket socket;
	
	public static double probability;
	
	public static InetAddress ip;
	
	static byte awaiting;
	static byte window;
	static DatagramPacket[] packetsReceived;
	static ByteBuffer bufferR = ByteBuffer.allocate(570);
	public static synchronized byte getSeqnum(DatagramPacket packet) {
		return packet.getData()[0];
	}
	
	public static void WriteFile(byte[] data) throws IOException {
		FileOutputStream output = new FileOutputStream("out.txt", true);
		for (int i = 1; i < data.length && data[i] != 0; ++i)
			output.write(data[i]);
		output.close();
		data = null;
	}
	

	public static int MOD;

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
		 } else System.out.println("Drop ack for " + seqnum);
	}

	private static void IncWait() {
		++awaiting;
		awaiting %= MOD;
	}
	
	private static String toBinary(long t) {
		StringBuilder ret = new StringBuilder("");
		while(t != 0) {
			ret.append(t%2);
			t >>= 1L;
		}
		return ret.reverse().toString();
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		new FileOutputStream("out.txt", false);
		System.out.println("\t\tWelcome to Client\n\n");
		if(!Functions.initClient()) {
			System.out.println("\n\n\t looks like your config file is " + 
								"not formated as we expect");
			return;
		}
		MOD = 3 * window;
		socket = new DatagramSocket(clientPort);
		packetsReceived = new DatagramPacket[MOD];
		while(true)
		{
			byte[] BUFFER = new byte[600];
			DatagramPacket packet = new DatagramPacket(BUFFER,BUFFER.length);
			socket.receive(packet);
			CRC32 checksum = new CRC32();
			checksum.update(packet.getData());
			byte num = getSeqnum(packet);
			System.out.println("received " + num + " checksum in decimal = "
				+ checksum.getValue() + " checksum in binary = " 
									  + toBinary(checksum.getValue()));
			if(packet.getLength() == 0)
				break ;
			if(num == awaiting)
			{
				packetsReceived[awaiting] = new DatagramPacket(
						packet.getData(), packet.getLength(),
						packet.getAddress(), packet.getPort());
				for (int i = 0; i < window && packetsReceived[awaiting] != null
						; ++i) 
				{
					byte[] wr = packetsReceived[awaiting].getData();
					packetsReceived[awaiting] = null;
					System.out.println("write in file packet with sequ num = " 
												+ wr[0]);
					WriteFile(wr);
					IncWait();
				}
				// to be cumulative the loop will get me the last in sequence 
				// as it write it to file
			}
			else if( num >= awaiting && num < (awaiting+window)%MOD)
			{
				packetsReceived[num] = new DatagramPacket(
						packet.getData(), packet.getLength(),
						packet.getAddress(), packet.getPort());
			}
				
			ACK((byte)((awaiting+MOD-1)%MOD));
		}
		socket.close();

	}

}