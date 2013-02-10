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
	
	public String getSrc(){return src;}
	public String getDest(){return dest;}
	public String getKind(){return kind;}
	public int getId(){return id;}
	public Object getData(){return data;}

	public void setSrc(String src){this.src = src;}
	public void setDest(String dest){this.dest = dest;}
	public void set_id(int id)
	{
		this.id = id;
	}

	public String toString()
	{
		return ("src:" + src + "|dest:" + dest + "|kind:" + kind + "|data:" + data + "|id:" + id);
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

}
