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
}
