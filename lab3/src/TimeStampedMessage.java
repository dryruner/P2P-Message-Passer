package bin;
import java.io.*;
import java.util.Arrays;

public class TimeStampedMessage extends Message implements Serializable, Comparable<TimeStampedMessage>
{
	private Object timestamp;
	private Clock clock;

	public TimeStampedMessage(String src, String dest, String kind, Object data, Object timestamp, Clock clock)
	{
		super(src, dest, kind, data);
		this.timestamp = timestamp;
		this.clock = clock;
	}

	public Object getTimeStamp(){return timestamp;}
	public void setTimeStamp(Object timestamp){this.timestamp = timestamp;}
	public Clock getClock(){return clock;}

	public String toString()
	{
		if(clock.getClass().getName().equals("bin.LogicalClock"))
			return (super.toString() + "|timestamp:" + (Integer)this.timestamp);
		else if(clock.getClass().getName().equals("bin.VectorClock"))
		{
			return (super.toString() + "|timestamp:" + Arrays.toString((int[])timestamp));
		}
		else
			return null;
	}

	public int compareTo(TimeStampedMessage t_msg)
	{
		return clock.compare(timestamp, t_msg.getTimeStamp());
	}

	public boolean is_equal(TimeStampedMessage t_msg)
	{
		return clock.equal(timestamp, t_msg.getTimeStamp());
	}

	public TimeStampedMessage deepCopy()
	{
		ByteArrayOutputStream bo = null;
		ByteArrayInputStream bi = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		TimeStampedMessage new_msg = null;

		try
		{
			bo = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bo);
			oos.writeObject(this);

			bi = new ByteArrayInputStream(bo.toByteArray());
			ois = new ObjectInputStream(bi);
			new_msg = (TimeStampedMessage)ois.readObject();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if(bo != null)
					bo.close();
				if(bi != null)
					bi.close();
				if(oos != null)
					oos.close();
				if(ois != null)
					ois.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
			return new_msg;
		}
	}
}
