package bin;
import java.io.*;

public class Message implements java.io.Serializable
{
	private String src;
	private String dest;
	private String kind;
	private Object data;
	private int id;

	public Message(String src, String dest, String kind, Object data)
	{
		this.src = src;
		this.dest = dest;
		this.kind = kind;
		this.data = data;
	}

	public Message deepCopy()
	{
		ByteArrayOutputStream bo = null;
		ByteArrayInputStream bi = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		Message new_msg = null;

		try
		{
			bo = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bo);
			oos.writeObject(this);

			bi = new ByteArrayInputStream(bo.toByteArray());
			ois = new ObjectInputStream(bi);
			new_msg = (Message)ois.readObject();
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

	public String getSrc(){return src;}
	public String getDest(){return dest;}
	public String getKind(){return kind;}
	public int getId(){return id;}
	public Object getData(){return data;}

	public void set_id(int id)
	{
		this.id = id;
	}

	public String toString()
	{
		return ("src:" + src + "|dest:" + dest + "|kind:" + kind + "|data:" + data + "|id:" + id);
	}

/*  to test deepCopy() function

	public static void main(String[] args)
	{
		Message m_1 = new Message("alice", "bob", "kind_1", "hehehehhehehehehehhehehehhehehehhehe");
		System.out.println("original m_1: " + m_1);
		Message m_2 = m_1.deepCopy();
		m_1.setSrc("hsy");
		m_1.setData("ha");
		System.out.println("after modify m_1: " + m_1);
		System.out.println("m_2: " + m_2);
		System.out.println("m_1.hashCode() = " + m_1.hashCode());
		System.out.println("m_2.hashCode() = " + m_2.hashCode());
	}
*/
}
