package bin;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.locks.*;

public class MutualExclusion extends Thread
{
	private boolean flag = true;
	private int replys_collected = 0;
	private final int DESIRED_REPLYS;
	private MessagePasser mp;
	private String local_name;
	private Queue<Message> request_queue = new LinkedList<Message>();

	public MutualExclusion(MessagePasser mp, String local_name)
	{
		this.mp = mp;
		DESIRED_REPLYS = mp.getGroups().get(mp.getLocalId()).size();
		this.local_name = local_name;
	}

	public void setFlag(){flag = false;}

	public void run()
	{
		Message msg = null;
		BlockingQueue<Message> cs_reply_queue = mp.getCsReplyQueue();
		Lock cs_lock = mp.getCsLock();
		Condition cs_cond = mp.getCsCondition();
		while(flag)
		{
			try
			{
				msg = cs_reply_queue.take();
			}
			catch(InterruptedException iex)
			{
				if(!flag)
					break;
				else
					iex.printStackTrace();
			}
			if(msg.getKind().equals("CS_REPLY")) // on receipt of reply msg
			{
				replys_collected++;
//				System.out.println("received reply: " + msg);
				if(replys_collected == DESIRED_REPLYS)
				{
					replys_collected = 0;
					cs_lock.lock();
					mp.setCsState(CS_STATE.CS_HELD);
					cs_cond.signal();
					cs_lock.unlock();
				}
			}
			else if(msg.getKind().equals("CS_REQUEST")) // on receipt of request msg
			{
//				System.out.println("receive CS_REQUEST: " + msg);
				cs_lock.lock();
				if(mp.getCsState().equals(CS_STATE.CS_HELD) || mp.getVoted() == true)
				{
					request_queue.add(msg);
//					System.out.println("request queued!");
				}
				else
				{
					mp.send(new Message(local_name, msg.getSrc(), "CS_REPLY", null));
					mp.setVoted(true);
				}
				cs_lock.unlock();
			}
			else if(msg.getKind().equals("CS_RELEASED")) // on receipt of release msg
			{
				if(!request_queue.isEmpty())
				{
					Message tmp = request_queue.remove();
					mp.send(new Message(local_name, tmp.getSrc(), "CS_REPLY", null));
					mp.setVoted(true);
				}
				else
					mp.setVoted(false);
			}
			else // this should never happen
			{
				System.out.println("Panic, shit happens! Which should never happen happened! received msg: " + msg);
			}
		}

	}
}
