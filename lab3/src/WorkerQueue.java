package bin;
import java.util.concurrent.*;

public class WorkerQueue
{
	private ConcurrentLinkedQueue worker_queue = new ConcurrentLinkedQueue();

	public ConcurrentLinkedQueue getWorkerQueue(){return worker_queue;}
	private Clock clock;
	private VectorClock m_clock;

	public Clock getClock(){return clock;}
	public void setClock(Clock clock){this.clock = clock;}

	public VectorClock getMClock(){return m_clock;}
	public void setMClock(VectorClock m_clock){this.m_clock = m_clock;}
}
