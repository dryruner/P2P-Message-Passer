package bin;
import java.io.*;
import java.net.*;

/* act as a "Receiver" to receive Messages from other nodes and storing them in receive_queue after checking and reacting to corresponding rule */
public class Receiver extends Thread
{
	private MessagePasser mp;
	private String local_name;
	private boolean flag = true;

	public Receiver(MessagePasser mp, String local_name)
	{
		this.mp = mp;
		this.local_name = local_name;
	}
	
	public void setFlag(){flag = false;}

	public void run()
	{
		ServerSocket ss = null;
		User uu = mp.getUsers().get(local_name);
		try
		{
			ss = new ServerSocket(uu.getPort());
			while(true)
			{
				Socket conn_sock = ss.accept();
//				System.err.println("accept hit !!!!!!!!!!");
				if(flag)
					new ReceiverWorker(conn_sock, mp).start();
				else
					break;
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try
			{
				if(ss != null)
					ss.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
}
