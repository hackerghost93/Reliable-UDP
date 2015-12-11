import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Functions {
	public static byte getSeqnum(DatagramPacket packet) {
		byte[] arr = packet.getData();
		// dih msh sha3'ala leh ?
		System.out.println("getting seq num " +arr[0]);
		return arr[0];
	}

	public static void WriteFile(byte[] data) throws IOException {
		FileOutputStream output = new FileOutputStream("out.txt",true);
		output.write(data);
		output.close();
	}

	public static byte[] ReadFile() throws IOException {
		byte[] data;
		Path path = Paths.get("output.txt");
		data = Files.readAllBytes(path);
		return data;
	}

}
