package bin;
import java.io.*;

public class lab0
{
	private static final String PROMPT = "> ";
	public static boolean flag = true; // 1 stands for running

	public void setFlag(){flag = false;}

	public static void Usage()
	{
		System.out.println("Usage:(for exitting, input exit or quit) \n send <dest> <kind>\tOR:\t receive\tOR:\tdebug\n");
	}

	public static String parseInput(String input)
	{
		String[] temp = input.trim().split(" ");
		if(temp.length != 1 && temp.length != 3)
		{
			Usage();
			return null;
		}
		if((temp.length == 1 && !(temp[0].equals("receive")) && !(temp[0].equals("debug"))) || (temp.length == 3 && !(temp[0].equals("send"))))
		{
			Usage();
			return null;
		}
		return input.trim();
	}

	public static void main(String[] args)
	{
		if(args.length != 2)
		{
			System.out.println("Usage: $java lab0 <conf_filename> <local_name>");
			System.exit(1);
		}
		lab0 ll = new lab0();
		MessagePasser mp = new MessagePasser(args[0], args[1]);
		mp.init();
		Sender send = new Sender(mp);
		send.start();
		Receiver recv = new Receiver(mp, args[1]);
		recv.start();
		FileMonitor fm = new FileMonitor(args[0], args[1], ll, mp, send, recv);
		fm.start();

		String input = null;
		BufferedReader br = null;
		Usage();
		try{
			br = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				System.out.print(PROMPT);
				/* get user input */
				input = br.readLine();
				if(flag)
				{
					if(input.equals("quit") || input.equals("exit"))System.exit(1);
					if((input = parseInput(input)) != null)
					{
						if(input.equals("receive"))
						{
							Message mm = mp.receive();
							if(mm == null)
								System.out.println("No new message.");
							else
							{
								System.out.println("Received message (" + mm.getSrc() + "," + mm.getId() + ") from " + mm.getSrc() + " to " + mm.getDest() + ".|Kind: " + mm.getKind() + "|Content: " + (String)mm.getData());
								System.out.println(mp.getReceiveQueue().size() + " more message(s)");
							}
						}
						else if(input.equals("debug"))
						{
							mp.check_status();
						}
						else
						{
							String[] temp = input.split(" ");
							System.out.print("Input message: ");
							input = br.readLine();
							mp.send(new Message(args[1], temp[1], temp[2], input));
						}
					}
				}
				else
				{
					flag = true;
					System.out.println("Configuration file has been changed, system rebooting now, will start in 5 seconds, please wait...");
					System.out.println("");
					try{Thread.sleep(5000);}catch(InterruptedException iex){iex.printStackTrace();}
				}
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try{
				if(br != null)
					br.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
}
