package bin;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ReceiverWorker extends Thread
{
	private Socket socket;
	private MessagePasser mp;
	private ConcurrentLinkedQueue worker_queue;
	private boolean flag = true;
	private WorkerQueue wq;
	private Clock clock;

	public void setFlag(){flag = false;}

	public ReceiverWorker(Socket conn_sock, MessagePasser mp, WorkerQueue wq)
	{
		this.socket = conn_sock;
		this.mp = mp;
		this.wq = wq;
		this.clock = wq.getClock();
	}

	public void run()
	{
		String from_user = null;
		ObjectInputStream ois = null;
		BlockingQueue<TimeStampedMessage> receive_queue = mp.getReceiveQueue();
		ConcurrentLinkedQueue<TimeStampedMessage> delay_receive_queue = mp.getDelayReceiveQueue();
		TimeStampedMessage msg = null;
		try
		{
			ois	= new ObjectInputStream(socket.getInputStream());
			while(true)
			{
				msg = (TimeStampedMessage)ois.readObject(); // it will lead to EOFException when from_user goes offline
				if(!flag)
					break;
				else
				{
					from_user = msg.getSrc();
					Rule matched_rule = mp.CheckRule(msg, 1);
					if(matched_rule != null)
					{
						if(matched_rule.getAction().equals("drop"))
							;
						else if(matched_rule.getAction().equals("duplicate"))
						{
							matched_rule.addMatch();
							receive_queue.put(msg);
							synchronized(delay_receive_queue)
							{
								while(!delay_receive_queue.isEmpty())
								{
									receive_queue.put(delay_receive_queue.poll());
								}
							}
							TimeStampedMessage new_m = null;
							if(msg instanceof TimeStampedMessage)
								new_m = msg.deepCopy();
							else if(msg instanceof MulticastMessage)
								new_m = ((MulticastMessage)msg).deepCopy();
							receive_queue.put(new_m);
						}
						else if(matched_rule.getAction().equals("delay"))
						{
							delay_receive_queue.add(msg);
						}
					}
					else
					{
						receive_queue.put(msg);
						synchronized(delay_receive_queue)
						{
							while(!delay_receive_queue.isEmpty())
							{
								receive_queue.put(delay_receive_queue.poll());
							}
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
			String msg_INFO = (from_user + " went offline!");
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
			wq.getWorkerQueue().remove(this); // since this thread will end, remove it from worker thread list;
		}
	}
}
