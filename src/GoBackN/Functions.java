package GoBackN;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Functions {

	public static String fileName;

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

	public static byte[] ReadFile() throws IOException {
		byte[] data;
		Path path = Paths.get(fileName);
		data = Files.readAllBytes(path);
		return data;
	}

	public static String removeBlanks(String t) {
		StringBuilder ret = new StringBuilder("");
		for (int i = 0; i < t.length(); ++i)
			if (t.charAt(i) == (char) 32 || t.charAt(i) == (char) 4
					|| t.charAt(i) == (char) 0 || t.charAt(i) == '\n'
					|| t.charAt(i) == '\t')
				continue;
			else
				ret.append(t.charAt(i));
		return ret.toString();
	}

	public static boolean isValidIP(String t, boolean client) {
		String[] all = removeBlanks(t).split(".");
		StringBuilder ip = new StringBuilder("");
		for (int i = 0; i < all.length; ++i)
			for (int j = 0; j < all[i].length(); ++j)
				if (Character.isDigit(all[i].charAt(j))
						|| all[i].charAt(j) == '.')
					ip.append(all[i].charAt(j));
				else
					return false;
		try {
			if (client)
				Client.ip = InetAddress.getByName(ip.toString());
			else
				Server.ip = InetAddress.getByName(ip.toString());
		} catch (UnknownHostException e) {
			return false;
		}
		return true;
	}

	public static int isValidInt(String t) {
		t = removeBlanks(t);
		StringBuilder ret = new StringBuilder("");
		for (int i = 0; i < t.length(); ++i)
			if (Character.isDigit(t.charAt(i)))
				ret.append(t.charAt(i));
			else
				return -1;
		return Integer.parseInt(ret.toString());
	}

	@SuppressWarnings("resource")
	public static boolean initClient() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("client.in"));
		String s;
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		if (!isValidIP(s, true)) {
			System.out.println("\nInvalid or missing IP address\n\n");
			return false;
		}
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		int t = isValidInt(s);
		if (t == -1) {
			System.out.println("\nInvalid or missing port\n\n");
			return false;
		} else
			Client.serverPort = t;
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		t = isValidInt(s);
		if (t == -1) {
			System.out.println("\nInvalid or missing port\n\n");
			return false;
		} else
			Client.clientPort = t;
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		t = isValidInt(s);
		if (t == -1 || t > 50) {
			System.out.println("\nInvalid or missing window size\n\n");
			return false;
		} else
			Client.window = (byte) t;
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		try {
			double p = Double.parseDouble(s);
			Client.probability = p;
		} catch (Exception e) {
			System.out.println("\nInvalid or missing probability\n\n");
			return false;
		}
		in.close();
		return true;
	}

	@SuppressWarnings("resource")
	public static boolean initServer() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("server.in"));
		String s;
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		if (!isValidIP(s, false)) {
			System.out.println("\nInvalid or missing IP address\n\n");
			return false;
		}
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		int t = isValidInt(s);
		if (t == -1) {
			System.out.println("\nInvalid or missing port\n\n");
			return false;
		} else
			Server.serverPort = t;
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		t = isValidInt(s);
		if (t == -1) {
			System.out.println("\nInvalid or missing port\n\n");
			return false;
		} else
			Server.clientPort = t;
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		fileName = s;
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		t = isValidInt(s);
		if (t == -1 || t > 50) {
			System.out.println("\nInvalid or missing window size\n\n");
			System.out.println(t);
			return false;
		} else
			Server.window = (byte) t;
		while ((s = in.readLine()) != null)
			if (!removeBlanks(s).isEmpty())
				break;
		try {
			double p = Double.parseDouble(s);
			Server.probability = p;
		} catch (Exception e) {
			System.out.println("\nInvalid or missing probability\n\n");
			return false;
		}
		in.close();
		return true;
	}

}
