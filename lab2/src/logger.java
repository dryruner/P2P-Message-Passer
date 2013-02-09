package bin;
import java.io.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;

public class logger
{
	private static final String PROMPT = "logger > ";
	private static final String log_filename = "log/log.txt";
	public static void Usage()
	{
		System.out.println("Usage: dump (for exitting, input exit or quit)\n");
	}

	public static void main(String[] args)
	{
		if(args.length != 2 || args[1].equals("logger") == false)
		{
			System.out.println("Usage: $java -classpath lib/snakeyaml-1.11.jar:. bin.logger <conf_filename> logger");
			System.exit(1);
		}
		MessagePasser mp = new MessagePasser(args[0], args[1], null);
		mp.init();
		logListener ll = new logListener(mp, args[1]);
		ll.start();

		String input = null;
		BufferedReader br = null;
		FileWriter fw = null;
		ArrayList<TimeStampedMessage> log_msg = new ArrayList<TimeStampedMessage>();
		Usage();

		try{
			br = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				System.out.print(PROMPT);
				/* get user input */
				input = br.readLine();
				if(input != null && (input.equals("quit") || input.equals("exit")))System.exit(1);
				if(input != null)
				{
					if(input.equals("dump"))
					{
						ArrayList<TimeStampedMessage> tm_arr = mp.receive();
						if(tm_arr.size() != 0)
							log_msg.addAll(tm_arr);
						
						Collections.sort(log_msg);
						fw = new FileWriter(log_filename);
						boolean flag = false;
						for(int i = 0; i < log_msg.size(); i++)
						{
							if(flag == false && i+1 < log_msg.size())
							{
								if(log_msg.get(i).compareTo(log_msg.get(i+1)) == 0 && log_msg.get(i).is_equal(log_msg.get(i+1)) == false) // concurrent msg
								{
									flag = true;
									System.out.println("{");
								}
							}
							System.out.println(i + ": " + log_msg.get(i));
							if(flag == true && i+1 < log_msg.size())
							{
								if(log_msg.get(i).compareTo(log_msg.get(i+1)) != 0) // not concurrent msg
								{
									flag = false;
									System.out.println("}");
								}
							}
							if(flag == true && i+1 == log_msg.size())
							{
								flag = false;
								System.out.print("}");
								System.out.println("");
							}
							fw.write(log_msg.get(i).toString() + "\n");
						}
						if(fw != null)
							fw.close();
					}
					else
						Usage();
				}
				else
					System.out.println("");
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
