package bin;
import java.util.concurrent.*;
import java.io.File;

public class FileMonitor extends Thread
{
	private final String conf_filename;
	private final String local_name;
	private lab0 ll;
	private final MessagePasser mp;
	private Sender send;
	private Receiver recv;

	public FileMonitor(String conf_filename, String local_name, lab0 ll, MessagePasser mp, Sender send, Receiver recv)
	{
		this.conf_filename = conf_filename;
		this.local_name = local_name;
		this.ll = ll;
		this.mp = mp;
		this.send = send;
		this.recv = recv;
		this.setDaemon(true);
	}

	public void run()
	{
		File ff = new File(conf_filename);
		long last_modify = ff.lastModified();
		while(true)
		{
			long temp = ff.lastModified();
			if(temp != last_modify)
			{
				last_modify = temp;
				/* shutdown Sender, Receiver threads and give a hint to Command line interface */
//				ll.flag = false;
				System.out.println("ll: " + ll);
				send.setFlag();
				send.interrupt();
				recv.setFlag();
				recv.interrupt();
				
				ConcurrentLinkedQueue<ReceiverWorker> worker_queue = mp.getWorkerQueue();
				/* shutdown all the ReceiverWorker threads */
				synchronized(worker_queue)
				{
					for(ReceiverWorker worker: worker_queue)
					{
						worker.setFlag();
						worker.interrupt();
					}
					try{Thread.sleep(500);}catch(InterruptedException iex){iex.printStackTrace();}
				}
				/* restart initing MessagePasser, Sender and Receiver threads */
				mp.init();
				send = new Sender(mp);
				send.start();
				recv = new Receiver(mp, local_name);
			}
			else
			{
				Thread.yield();
			}
		}
	}
}
