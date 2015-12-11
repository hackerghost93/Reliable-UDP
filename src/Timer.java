import java.io.IOException;
import java.net.DatagramPacket;

public class Timer implements Runnable {
	static final int timeout = 10000;
	DatagramPacket packet;
	int seqnum;
	int physicalNumber;

	Timer(DatagramPacket packet, int seqnum, int physicalNumber) {
		this.packet = packet;
		this.seqnum = seqnum;
		// real value of fileIndex
		this.physicalNumber = physicalNumber;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(timeout);
				if (!ServerMain.acked[seqnum]) {
					ServerMain.SendPacket(physicalNumber);
				} else
					break;
			} catch (InterruptedException e) {
			} catch (IOException e) {
			}
		}
	}
}
