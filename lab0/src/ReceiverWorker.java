package bin;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.*;

public class ReceiverWorker extends Thread
{
	private Socket socket;
	private MessagePasser mp;

	public ReceiverWorker(Socket conn_sock, MessagePasser mp)
	{
		this.socket = conn_sock;
		this.mp = mp;
	}

	public void run()
	{
		Logger log = Logger.getLogger("receive_log");
		log.setUseParentHandlers(false);
		log.setLevel(Level.INFO);
		try
		{
			/* receive log file */
			FileHandler fh = new FileHandler("log/receive.log");
			fh.setFormatter(new SimpleFormatter());
			log.addHandler(fh);
		}
		catch(SecurityException se){se.printStackTrace();}
		catch(IOException ioe){ioe.printStackTrace();}

		ObjectInputStream ois = null;
		try{	
			ois	= new ObjectInputStream(socket.getInputStream());
			Message msg = null;
			msg = (Message)ois.readObject();
			
			/* log the received msg before checking Receive Rules */
			log.info(msg.toString());

			ConcurrentLinkedQueue<Message> receive_queue = mp.getReceiveQueue();
			Queue<Message> delay_receive_queue = mp.getDelayReceiveQueue();
			Rule matched_rule = mp.CheckRule(msg, 1);
			if(matched_rule != null)
			{
				if(matched_rule.getAction().equals("drop"))
					;
				else if(matched_rule.getAction().equals("duplicate"))
				{
					matched_rule.addMatch();
					receive_queue.add(msg);
					while(!delay_receive_queue.isEmpty())
					{
						receive_queue.add(delay_receive_queue.poll());
					}
					Message new_m = msg.deepCopy();
					receive_queue.add(new_m);
				}
				else if(matched_rule.getAction().equals("delay"))
				{
					delay_receive_queue.add(msg);
				}
			}
			else
			{
				receive_queue.add(msg);
				while(!delay_receive_queue.isEmpty())
				{
					receive_queue.add(delay_receive_queue.poll());
				}
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch(ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
		}
		finally
		{
			try
			{
				if(ois != null)
					ois.close();
				if(socket != null)
					socket.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
}
