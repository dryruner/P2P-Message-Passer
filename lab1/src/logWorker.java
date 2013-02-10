package bin;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class logWorker extends Thread
{
	private Socket socket;
	private MessagePasser mp;
	private ConcurrentLinkedQueue<logWorker> cq;

	public logWorker(Socket conn_sock, MessagePasser mp, ConcurrentLinkedQueue<logWorker> cq)
	{
		this.socket = conn_sock;
		this.mp = mp;
		this.cq = cq;
	}

	public void run()
	{
		String from_user = null;
		ObjectInputStream ois = null;
		ConcurrentLinkedQueue<TimeStampedMessage> receive_queue = mp.getReceiveQueue();
		TimeStampedMessage msg = null;
		String[] src_dest = null;
		try
		{
			ois	= new ObjectInputStream(socket.getInputStream());
			while(true)
			{
				msg = (TimeStampedMessage)ois.readObject(); // it will lead to EOFException when from_user goes offline
				src_dest = msg.getSrc().split("\\$");
				if(src_dest.length == 2)
				{
					msg.setSrc(src_dest[0]);
					msg.setDest(src_dest[1]);
				}
				receive_queue.add(msg);
				from_user = msg.getSrc();
			}
		}
		catch(ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
		}
		catch(EOFException eof)
		{
			String msg_INFO = (from_user + " went offline!");
//			System.out.println(msg_INFO);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if(ois != null)
					ois.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
			cq.remove(this); // since this thread will end, remove it from worker thread list;
		}
	}
}
