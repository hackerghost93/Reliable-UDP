package GoBackN;



import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Client {
	
	public final static int port = 5372;
	public static DatagramSocket socket;
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
		InetAddress ip = InetAddress.getByName("127.0.0.1");
		DatagramPacket packet = new DatagramPacket(BUFFER, BUFFER.length, ip,
				Server.port);
		double num = Math.random();
		 if(num > Server.probability) {
		socket.send(packet);
		System.out.println("Sending ack for " + seqnum);
		 }
	}

	private static void IncWait() {
		++awaiting;
		awaiting %= MOD;
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		new FileOutputStream("out.txt", false);
		socket = new DatagramSocket(port);
		System.out.println("Enter the window size client");
		Scanner input = new Scanner(System.in);
		window = input.nextByte();
		MOD = 3 * window;
		input.close();
		packetsReceived = new DatagramPacket[MOD];
		while(true)
		{
			byte[] BUFFER = new byte[600];
			DatagramPacket packet = new DatagramPacket(BUFFER,BUFFER.length);
			socket.receive(packet);
			if(packet.getLength() == 0)
				break ;
			byte num = getSeqnum(packet);
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
					System.out.println("sequ num = " + wr[0]);
					WriteFile(wr);
					IncWait();
				}
				// to be cumulative the loop will get me the last in sequence 
				// as it write it to file
				ACK((byte)((awaiting+MOD-1)%MOD));
			}
			else if( num >= awaiting && num < (awaiting+window)%MOD)
			{
				packetsReceived[num] = new DatagramPacket(
						packet.getData(), packet.getLength(),
						packet.getAddress(), packet.getPort());
			}
			else if(num <= (awaiting+MOD -1) % MOD && num >=(awaiting+MOD-window)%MOD)
				ACK(num);
			else if((awaiting + MOD -1) %MOD < 5 && num >= (MOD-window+awaiting)%MOD)
				ACK(num);
//			
		}
		socket.close();

	}

}
