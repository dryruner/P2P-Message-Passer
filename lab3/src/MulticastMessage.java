package bin;
import java.io.*;
import java.util.Arrays;

public class MulticastMessage extends TimeStampedMessage implements Serializable//, Comparable<MulticastMessage>
{
	private int[] m_timestamp;/* vector used in multicast */
	private int seq_num;
	private int group_id; // to which group this msg is sent to
	private int from_local_id;

	/* Ack is also a MulticastMessage, if kind == M_NACK, then missing msgs; if kind == M_ACK, then an acknowledge; else just normal multicasted msg */
	public MulticastMessage(String src, String dest, String kind, Object data, Object timestamp, Clock clock, int group_id)
	{
		super(src, dest, kind, data, timestamp, clock);
		this.group_id = group_id;
	}

	public int[] getMTimeStamp(){return m_timestamp;}
	public int getSeqNum(){return seq_num;}
	public void setSeqNum(int seq_num){this.seq_num = seq_num;}
	public void setMTimeStamp(int[] m_timestamp){this.m_timestamp = m_timestamp;}
	public int getGroupId(){return group_id;}
	public void setFromLocalId(int from_local_id){this.from_local_id = from_local_id;}
	public int getFromLocalId(){return from_local_id;}

	public String toString()
	{
		return (super.toString() + "|SN:" + seq_num + "|group_id:" + group_id + "|m_timestamp:" + Arrays.toString(m_timestamp));
	}
/*
	public int compareTo(MulticastMessage m_msg)
	{
		return VectorClock.compare(m_timestamp, m_msg.getMTimeStamp());
	}
*/
	public MulticastMessage deepCopy()
	{
		ByteArrayOutputStream bo = null;
		ByteArrayInputStream bi = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		MulticastMessage new_msg = null;

		try
		{
			bo = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bo);
			oos.writeObject(this);

			bi = new ByteArrayInputStream(bo.toByteArray());
			ois = new ObjectInputStream(bi);
			new_msg = (MulticastMessage)ois.readObject();
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
