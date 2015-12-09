import java.io.IOException;
import java.net.DatagramPacket;


public class Timer implements Runnable{
	static int timeout ;
	DatagramPacket packet = null ;
	int seqnum ;
	int physicalNumber ;
	
	Timer(DatagramPacket packet,int seqnum,int physicalNumber)
	{
		this.packet = packet ;
		this.seqnum = seqnum ;
		// the real value of the packet in the array
		this.physicalNumber = physicalNumber ;
	}
	
	public void run()
	{
		while(true){
			try {
				Thread.sleep(timeout);
				if(!ServerMain.acked[seqnum])
				{
					ServerMain.SendPacket(physicalNumber);
				}
				else break ;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
