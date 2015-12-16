package GoBackN;

public class Timer implements Runnable{
	private int timeout = 50;
	byte seqnum ;
	int fileIndex ;
	public Timer(byte seqnum,int fileIndex)
	{
		this.seqnum = seqnum ;
		this.fileIndex = fileIndex ;
	}
	public void run()
	{
		try
		{
			while(true)
			{
				Thread.sleep(timeout);
				if(Server.acked[seqnum])	
					break; 	
				for(int i = 0 ; i < Server.window && fileIndex+i < Server.packets.size() ; i++)
				{
					Server.SendPacket(fileIndex+i);
				}
			}
		}catch(Exception e)
		{
			
		}
	}
}