package bin;
import java.util.concurrent.*;

public class WorkerQueue
{
	private ConcurrentLinkedQueue worker_queue = new ConcurrentLinkedQueue();

	public ConcurrentLinkedQueue getWorkerQueue(){return worker_queue;}
	private Clock clock;

	public Clock getClock(){return clock;}
	public void setClock(Clock clock){this.clock = clock;}
}
