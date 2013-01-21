package bin;
import java.io.*;
import java.util.*;

public class User
{
	private String name;
	private String ip;
	private int port;
	/* User will cache all its client sockets' OutputStream */

	public User(String Name)
	{
		name = Name;
	}

	public String getName(){return name;}
	public String getIp(){return ip;}
	public int getPort(){return port;}

	public void setIp(String ip){this.ip = ip;}
	public void setPort(int port){this.port = port;}
		
	public String toString()
	{
		return ("Name:" + name + "|IP:" + ip + "|Port:" + port); 
	}
}
