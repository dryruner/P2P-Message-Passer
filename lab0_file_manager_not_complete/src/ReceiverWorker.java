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
	private ConcurrentLinkedQueue<ReceiverWorker> worker_queue;
	private boolean flag = true;

	public void setFlag(){flag = false;}

	public ReceiverWorker(Socket conn_sock, MessagePasser mp)
	{
		this.socket = conn_sock;
		this.mp = mp;
		this.worker_queue = mp.getWorkerQueue();
		worker_queue.add(this);  // add to the worker thread list
	}

	public void run()
	{
		String from_user = null;
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
		ConcurrentLinkedQueue<Message> receive_queue = mp.getReceiveQueue();
		ConcurrentLinkedQueue<Message> delay_receive_queue = mp.getDelayReceiveQueue();
		Message msg = null;
		try
		{
			ois	= new ObjectInputStream(socket.getInputStream());
			while(flag)
			{
				msg = (Message)ois.readObject(); // it will lead to EOFException when from_user goes offline
				from_user = msg.getSrc();
				/* log the received msg before checking Receive Rules */
				log.info(msg.toString());
				Rule matched_rule = mp.CheckRule(msg, 1);
				if(matched_rule != null)
				{
					if(matched_rule.getAction().equals("drop"))
						;
					else if(matched_rule.getAction().equals("duplicate"))
					{
						matched_rule.addMatch();
						receive_queue.add(msg);
						synchronized(delay_receive_queue)
						{
							while(!delay_receive_queue.isEmpty())
							{
								receive_queue.add(delay_receive_queue.poll());
							}
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
					synchronized(delay_receive_queue)
					{
						while(!delay_receive_queue.isEmpty())
						{
							receive_queue.add(delay_receive_queue.poll());
						}
					}
				}
			}
		}
		catch(ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
		}
		catch(EOFException eof)
		{
//			eof.printStackTrace();
			String msg_INFO = (from_user + " went offline!");
			System.out.println(msg_INFO);
			log.info(msg_INFO);
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
			worker_queue.remove(this); // since this thread will end, remove it from worker thread list;
		}
	}
}
