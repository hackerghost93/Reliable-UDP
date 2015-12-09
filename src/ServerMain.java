import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ServerMain {
	public static int port = 7891;
	public static int window;
	public static DatagramSocket socket;
	public static boolean acked[];
	public static int pointerNext;
	static ArrayList<DatagramPacket> packets;
	static byte[] data;
	int lastACK;

	public static void CreatePackets(byte[] data) throws IOException {
		data = Functions.ReadFile();
		InetAddress ip = InetAddress.getByName("127.0.0.1");
		byte[] buffer = new byte[512];
		byte sequ = 0;
		for (int i = 0; i < data.length; ++i) {
			Arrays.fill(buffer, (byte) 0);
			buffer[0] = sequ++;
			sequ %= window << 1 | 1;
			for (int j = 1; j < 512 && i < data.length; ++i, ++j)
				buffer[j] = data[i];
			packets.add(new DatagramPacket(buffer, buffer.length, ip, port));
			if (i != data.length)
				--i;
		}
	}

	public static void SendPacket(int physicalNumber) throws IOException {
		// where the probability of error can occur
		socket.send(packets.get(physicalNumber));
	}

	public static void main(String[] args) throws IOException {
		socket = new DatagramSocket(port);
		System.out.println("Enter Window Size");
		Scanner input = new Scanner(System.in);
		window = input.nextInt();
		acked = new boolean[2 * window + 1];
		input.close();
	}

}
