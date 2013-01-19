import java.io.*;

public class lab0
{
	private static final String PROMPT = "> ";

	public static void Usage()
	{
		System.out.println("Usage:(for exitting, input exit or quit) \n send <dest> <kind>\tOR:\t receive\n");
	}

	public static String parseInput(String input)
	{
		String[] temp = input.trim().split(" ");
		if(temp.length != 1 && temp.length != 3)
		{
			Usage();
			return null;
		}
		if((temp.length == 1 && !(temp[0].equals("receive"))) || (temp.length == 3 && !(temp[0].equals("send"))))
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
		MessagePasser mp = new MessagePasser(args[0], args[1]);
		mp.init();

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
				if(input.equals("quit") || input.equals("exit"))break;
				if((input = parseInput(input)) != null)
				{
					if(input.equals("receive"))
						System.out.println(mp.receive());
					else
					{
						String[] temp = input.split(" ");
						System.out.print("Input message: ");
						input = br.readLine();
						mp.send(new Message(args[1], temp[1], temp[2], input));
					}
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
