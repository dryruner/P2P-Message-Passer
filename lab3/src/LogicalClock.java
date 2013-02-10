package bin;

public class LogicalClock implements Clock<Integer, TimeStampedMessage>, java.io.Serializable
{
	private int timestamp = 0;
	public LogicalClock(){}
	synchronized public Integer inc()
	{
		return ++timestamp;
	}
	synchronized public Integer getTimeStamp(){return timestamp;}
	synchronized public void syncWith(TimeStampedMessage t_msg)
	{
		timestamp = Math.max(timestamp, (Integer)t_msg.getTimeStamp()) + 1;
	}
	public boolean equal(Integer tm1, Integer tm2)
	{
		return tm1 == tm2;
	}

	public int compare(Integer timestamp1, Integer timestamp2)
	{
		if(timestamp1 - timestamp2 < 0)
			return -1;
		else if(timestamp1 - timestamp2 > 0)
			return 1;
		else
			return 0;
	}
}
