import java.io.IOException;
import java.net.DatagramPacket;

public class Timer implements Runnable {
	static int timeout ;
	DatagramPacket packet;
	int seqnum;
	int physicalNumber;

	static
	{
		timeout = 50;
	}
	Timer(DatagramPacket packet, int seqnum, int physicalNumber) {
		this.packet = packet;
		this.seqnum = seqnum;
		// real value of fileIndex
		this.physicalNumber = physicalNumber;
	}

	public synchronized void run() {
		while (true) {
			try {
				Thread.sleep(timeout);
				if (!ServerMain.acked[seqnum]) {
					System.out.println("retransmit " + physicalNumber);
					ServerMain.SendPacket(physicalNumber);
				} else
					break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
