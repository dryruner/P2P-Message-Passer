package bin;
import java.util.concurrent.*;

public class WorkerQueue
{
//	private boolean lab0_flag = true;
	private ConcurrentLinkedQueue worker_queue = new ConcurrentLinkedQueue();

	public ConcurrentLinkedQueue getWorkerQueue(){return worker_queue;}
//	public boolean getLab0Flag(){return lab0_flag;}
/*	public void setLab0Flag()
	{
		if(lab0_flag)
			lab0_flag = false;
		else
			lab0_flag = true;
	}
*/
}
