package bin;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
//import java.util.logging.*;

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
/*		Logger log = Logger.getLogger("receive_log");
		log.setUseParentHandlers(false);
		log.setLevel(Level.INFO);
		try
		{
			FileHandler fh = new FileHandler("logger.log");
			fh.setFormatter(new SimpleFormatter());
			log.addHandler(fh);
		}
		catch(SecurityException se){se.printStackTrace();}
		catch(IOException ioe){ioe.printStackTrace();}
*/
		ObjectInputStream ois = null;
		ConcurrentLinkedQueue<TimeStampedMessage> receive_queue = mp.getReceiveQueue();
		TimeStampedMessage msg = null;
		try
		{
			ois	= new ObjectInputStream(socket.getInputStream());
			while(true)
			{
				msg = (TimeStampedMessage)ois.readObject(); // it will lead to EOFException when from_user goes offline
//				log.info(msg.toString());
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
//			log.info(msg_INFO);
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
