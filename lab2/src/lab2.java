package bin;
import java.io.*;
import java.util.ArrayList;

public class lab2
{
	private static final String PROMPT = "> ";

	public static void Usage()
	{
		System.out.println("Usage:(for exitting, input exit or quit) \n send <dest> <kind> <log>\tOR:\t receive\tOR:\tdebug\tOR:\tmulticast <group_id>\n");
	}

	public static String parseInput(String input)
	{
		if(input == null)return null;
		String[] temp = input.trim().split(" ");
		if(temp.length > 4)
		{
			Usage();
			return null;
		}
		if
		(
				(temp.length == 1 && !(temp[0].equals("receive")) && !(temp[0].equals("debug")) && !(temp[0].equals("groups")))
				||
				(temp.length == 2 && !(temp[0].equals("multicast")))
				||
				(temp.length == 3 && !(temp[0].equals("send")))
				|| 
				(temp.length == 4 && (!(temp[0].equals("send")) || !(temp[3].equals("log"))))
		)
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
			System.out.println("Usage: $java lab2 <conf_filename> <local_name>");
			System.exit(1);
		}
		WorkerQueue wq = new WorkerQueue();
		MessagePasser mp = new MessagePasser(args[0], args[1], wq);
		mp.init();
		wq.setClock(new ClockFactory(mp).getClock());
		wq.setMClock(new VectorClock(mp.getUsers().size(), mp.getLocalId()));
		Sender send = new Sender(mp);
		send.start();
		Receiver recv = new Receiver(mp, args[1], wq);
		recv.start();
		DeliverWorker dw = new DeliverWorker(mp, args[1], wq);
		dw.start();
		FileMonitor fm = new FileMonitor(args[0], args[1], mp, send, recv, wq, dw);
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
				if(input == null)
				{
					System.out.println("");
				}
				else
				{
					if(input.equals("quit") || input.equals("exit"))System.exit(1);
					if((input = parseInput(input)) != null)
					{
						if(input.equals("receive"))
						{
							ArrayList<TimeStampedMessage> tm_arr = mp.receive();
							if(tm_arr == null || tm_arr.size() == 0)
								System.out.println("No new message.");
							else
							{
								for(TimeStampedMessage tm: tm_arr)
								{
									System.out.println(tm);
								}
							}
						}
						else if(input.equals("debug"))
						{
							mp.check_status();
						}
						else if(input.equals("groups"))
						{
							mp.show_groups();
						}
						else if(input.startsWith("multicast"))
						{
							String[] temp = input.split(" ");
							try
							{
								int group_num = Integer.parseInt(temp[1]);
								int status = 0;
								if((status = mp.check_group(args[1], group_num)) == 0)
								{
									System.out.print("Kind: ");
									String kind = br.readLine();
									System.out.print("Input message: ");
									input = br.readLine();
		// -------------------------------------- need more work
		//							System.out.println("Your input: " + input);
									MulticastMessage r_msg = new MulticastMessage(args[1], null, kind, input, wq.getClock().inc(), wq.getClock());
									mp.R_multicast(r_msg, group_num);
								}
								else if(status == -1)
								{

									System.out.println("No such group: " + group_num + ", use command \"groups\" to check group information.");
								}
								else if(status == -2)
								{
									System.out.println(args[1] + " isn't in group " + group_num + ", use command \"groups\" to check group information.");
								}
							}
							catch(NumberFormatException nex)
							{
								System.out.println("<group_id> should be an integer.");
							}
						}
						else
						{
							String[] temp = input.split(" ");
							if(mp.getUsers().get(temp[1]) != null)
							{
								System.out.print("Input message: ");
								input = br.readLine();
								TimeStampedMessage tm = new TimeStampedMessage(args[1], temp[1], temp[2], input, wq.getClock().inc(), wq.getClock());
								tm.set_id(mp.incId());
								mp.send(tm);
								if(temp.length == 4)
								{
									TimeStampedMessage tm_2 = tm.deepCopy();
									tm_2.setSrc(tm_2.getSrc() + "$" + tm_2.getDest());
									tm_2.setDest("logger");
									mp.getSendQueue().put(tm_2);
								}
							}
							else
							{
								System.out.println("Error! No such user: " + temp[1]);
							}
						}
					}
					else
						System.out.println("");
				}
/*				}
				else
				{
					System.out.println("Configuration file has been changed, system rebooting now, will start in 5 seconds, please wait...");
					System.out.println("");
					try{Thread.sleep(500);}catch(InterruptedException iex){iex.printStackTrace();}
					wq.setLab0Flag();
				}*/
			}
		}
		catch(Exception ioe)
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
