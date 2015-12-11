import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Functions {
	public static byte getSeqnum(DatagramPacket packet) {
		return packet.getData()[0];
	}

	public synchronized static 
		void WriteFile(byte[] data) throws IOException {
		FileOutputStream output = new FileOutputStream("out.txt",true);
		for(int i = 1 ; i < data.length && data[i] != 0 ; ++i)
			output.write(data[i]);
		output.close();
	}

	public static byte[] ReadFile() throws IOException {
		byte[] data;
		Path path = Paths.get("output.txt");
		data = Files.readAllBytes(path);
		return data;
	}

}
