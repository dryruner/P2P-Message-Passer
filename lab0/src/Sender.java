package bin;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;


/* act as a "sender" to send Messages in send_queue if it's not empty */
public class Sender extends Thread
{
	private MessagePasser mp;

	public Sender(MessagePasser mp)
	{
		this.mp = mp;
	}

	public void run()
	{
		Message mm = null;
		BlockingQueue<Message> bq = mp.getSendQueue();
		User uu = null;
		Logger log = Logger.getLogger("send_log");
		log.setUseParentHandlers(false);
		log.setLevel(Level.INFO);
		try
		{
			FileHandler fh = new FileHandler("log/send.log");
			fh.setFormatter(new SimpleFormatter());
			log.addHandler(fh);
		}
		catch(SecurityException se){se.printStackTrace();}
		catch(IOException ioe){ioe.printStackTrace();}

		while(true)
		{
			try
			{
				mm = bq.take(); // will block if necessary
				uu = mp.getUsers().get(mm.getDest()); //send message to that user
			}
			catch(InterruptedException iex)
			{
				iex.printStackTrace();
			}
			Socket ss = null;
			ObjectOutputStream oos = null;
			try
			{
				ss = new Socket(uu.getIp(), uu.getPort());
				oos = new ObjectOutputStream(ss.getOutputStream());
				oos.writeObject(mm);
				/* log the Message object just send out */
				log.info(mm.toString());
				oos.flush();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			finally
			{	
				try
				{
					if(oos != null)
						oos.close();
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
}
