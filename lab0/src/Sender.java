package bin;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.*;

/* act as a "sender" to send Messages in send_queue if it's not empty */
public class Sender extends Thread
{
	private MessagePasser mp;
	private boolean flag = true;

	public Sender(MessagePasser mp)
	{
		this.mp = mp;
	}

	public void setFlag(){flag = false;}

	public void run()
	{
		Message mm = null;
		User uu = null;
		Socket ss = null;
		ObjectOutputStream oos = null;
		BlockingQueue<Message> bq = mp.getSendQueue();
		HashMap<String, ObjectOutputStream> cached_output_streams = mp.getCachedOutputStreams();
		
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


		while(flag)
		{
			try
			{
				mm = bq.take(); // will block if necessary
				uu = mp.getUsers().get(mm.getDest()); //send message to that user
			}
			catch(InterruptedException iex)
			{
				if(!flag)
					break;
				else
					iex.printStackTrace();
			}
			
			try
			{
				oos = cached_output_streams.get(uu.getName());
				if(oos == null)
				{
					ss = new Socket(uu.getIp(), uu.getPort());
					oos = new ObjectOutputStream(ss.getOutputStream());
					cached_output_streams.put(uu.getName(), oos);
				}
				oos.writeObject(mm);
				/* log the Message object just send out */
				log.info(mm.toString());
				oos.flush();
			}
			catch(ConnectException cex)
			{
				System.out.println("Error! remote node " + mm.getDest() + " is not online!");
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
			catch(SocketException sex)
			{
				//sex.printStackTrace();
				ObjectOutputStream temp_oos = cached_output_streams.remove(uu.getName());
				try{
					if(temp_oos != null)
						temp_oos.close();
				}
				catch(IOException ioe)
				{
					; // remote receiver offline, temp_oos.close() error, do nothing
					//ioe.printStackTrace();
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
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
