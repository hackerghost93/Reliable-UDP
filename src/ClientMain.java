import java.io.FileOutputStream;
import java.io.IOException;


public class ClientMain {
	public static void WriteFile(byte[] data) throws IOException
	{
		FileOutputStream output = new FileOutputStream("out.txt");
		output.write(data);
		output.close();
	}
	public static void main(String[] args) {

	}

}
