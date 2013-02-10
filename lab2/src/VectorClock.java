package bin;
import java.util.Arrays;

public class VectorClock implements Clock<int[], TimeStampedMessage>, java.io.Serializable
{
	private int size;
	private int local_id;
	private int[] timestamp;
	public VectorClock(int size, int local_id)
	{
		this.size = size;
		this.local_id = local_id;
		this.timestamp = new int[size];
	}
	synchronized public int[] getTimeStamp(){return timestamp;}
	synchronized public int[] inc()
	{
		timestamp[local_id]++;
		return Arrays.copyOf(timestamp, size);
	}
	synchronized public void syncWith(TimeStampedMessage t_msg)
	{
		for(int i = 0; i < timestamp.length; i++)
		{
			timestamp[i] = Math.max(timestamp[i], ((int[])t_msg.getTimeStamp())[i]);
		}
		timestamp[local_id]++;
	}

	synchronized public void syncWithMClock(MulticastMessage mm_msg)
	{
		for(int i = 0; i < timestamp.length; i++)
		{
			timestamp[i] = Math.max(timestamp[i], (mm_msg.getMTimeStamp())[i]);
		}
		timestamp[local_id]++;
	}

	public boolean equal(int[] tm1, int[] tm2)
	{
		int i;
		for(i = 0; i < tm1.length; i++)
		{
			if(tm1[i] == tm2[i])
				continue;
			break;
		}
		if(i == tm1.length)
			return true;
		else
			return false;
	}

	private boolean less_than_equal(int[] tm1, int[] tm2)
	{
		int i;
		for(i = 0; i < tm1.length; i++)
		{
			if(tm1[i] <= tm2[i])
				continue;
			break;
		}
		if(i == tm1.length)
			return true;
		else
			return false;
	}

	private boolean less_than(int[] tm1, int[] tm2)
	{
		return (less_than_equal(tm1, tm2) && !equal(tm1, tm2));
	}

	public int compare(int[] tm1, int[] tm2)
	{
		if(less_than(tm1, tm2))
			return -1;
		else if(less_than(tm2, tm1))
			return 1;
		else   // tm1 == tm2 or tm1 || tm2
			return 0;
	}
}
