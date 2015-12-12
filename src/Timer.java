import java.io.IOException;
import java.net.DatagramPacket;

public class Timer implements Runnable {
	static final int timeout = 1000;
	int seqnum;
	int physicalNumber;

	Timer(int seqnum, int physicalNumber) {
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
				}
			} catch (InterruptedException e) {
				return;
			} catch (IOException e) {
			}
		}
	}
}
