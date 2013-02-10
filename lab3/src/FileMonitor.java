package bin;
import java.util.concurrent.*;
import java.io.File;

public class FileMonitor extends Thread
{
	private final String conf_filename;
	private final String local_name;
	private final MessagePasser mp;
	private Sender send;
	private Receiver recv;
	private DeliverWorker dw;
	private WorkerQueue wq;
	private MutualExclusion me;

	public FileMonitor(String conf_filename, String local_name, MessagePasser mp, Sender send, Receiver recv, WorkerQueue wq, DeliverWorker dw, MutualExclusion me)
	{
		this.conf_filename = conf_filename;
		this.local_name = local_name;
		this.mp = mp;
		this.send = send;
		this.recv = recv;
		this.wq = wq;
		this.dw = dw;
		this.me = me;
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
				/* shutdown Sender thread, there's no need to kill Receiver thread, because receiver 
				   thread binds to the machine IP address of the running program. It doesn't make sense when
				   one could modify his own IP address without exitting the current running program.
				 */
				send.setFlag();
				send.interrupt();
				dw.setFlag();
				dw.interrupt();
				me.setFlag();
				me.interrupt();

				ConcurrentLinkedQueue worker_queue = wq.getWorkerQueue();
				/* shutdown all the ReceiverWorker threads */
				synchronized(worker_queue)
				{
					while(!worker_queue.isEmpty())
					{
						ReceiverWorker temp_rw = (ReceiverWorker)worker_queue.poll();
						temp_rw.setFlag();
						temp_rw.interrupt();
					}
				}
				/* restart initing MessagePasser, Sender and Receiver threads */
				mp.init();
				wq.setClock(new ClockFactory(mp).getClock());
				wq.setMClock(new VectorClock(mp.getUsers().size(), mp.getLocalId()));
				send = new Sender(mp);
				send.start();
				dw = new DeliverWorker(mp, local_name, wq);
				dw.start();
				me = new MutualExclusion(mp, local_name);
				me.start();
			}
			else
			{
				Thread.yield();
			}
		}
	}
}
