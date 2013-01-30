package bin;
import java.io.*;
import java.util.ArrayList;

public class lab1
{
	private static final String PROMPT = "> ";

	public static void Usage()
	{
		System.out.println("Usage:(for exitting, input exit or quit) \n send <dest> <kind>\tOR:\t receive\tOR:\tdebug\n");
	}

	public static String parseInput(String input)
	{
		if(input == null)return null;
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
			System.out.println("Usage: $java lab1 <conf_filename> <local_name>");
			System.exit(1);
		}
		WorkerQueue wq = new WorkerQueue();
		MessagePasser mp = new MessagePasser(args[0], args[1], wq);
		mp.init();
		wq.setClock(new ClockFactory(mp).getClock());
		Sender send = new Sender(mp);
		send.start();
		Receiver recv = new Receiver(mp, args[1], wq);
		recv.start();
		FileMonitor fm = new FileMonitor(args[0], args[1], mp, send, recv, wq);
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
//				System.out.println("lab.flag = " + wq.getLab0Flag());
//				if(wq.getLab0Flag())
//				{
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
						else
						{
							String[] temp = input.split(" ");
							if(mp.getUsers().get(temp[1]) != null)
							{
								System.out.print("Input message: ");
								input = br.readLine();
								TimeStampedMessage tm = new TimeStampedMessage(args[1], temp[1], temp[2], input, wq.getClock().inc(), wq.getClock());
								mp.send(tm);
							}
							else
							{
								System.out.println("Error! No such user: " + temp[1]);
							}
						}
					}
					else
						System.out.println("");
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
