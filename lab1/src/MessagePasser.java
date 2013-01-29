package bin;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import java.net.Socket;
import org.yaml.snakeyaml.Yaml;

public class MessagePasser
{
	private final String conf_filename;
	private final String local_name;
	private String clock_type;
	private int local_id;
	private int id = 0;
	private BlockingQueue<TimeStampedMessage> send_queue = new LinkedBlockingQueue<TimeStampedMessage>();
	private ConcurrentLinkedQueue<TimeStampedMessage> receive_queue = new ConcurrentLinkedQueue<TimeStampedMessage>();
	private Queue<TimeStampedMessage> delay_send_queue = new LinkedList<TimeStampedMessage>();
	private ConcurrentLinkedQueue<TimeStampedMessage> delay_receive_queue = new ConcurrentLinkedQueue<TimeStampedMessage>();
	private HashMap<String, User> users = new HashMap<String, User>();// all users
	private ArrayList<Rule> SendRules = new ArrayList<Rule>();
	private ArrayList<Rule> ReceiveRules = new ArrayList<Rule>();
	private HashMap<String, ObjectOutputStream> cached_output_streams = new HashMap<String, ObjectOutputStream>();

	public MessagePasser(String conf_filename, String local_name)
	{
		this.conf_filename = conf_filename;
		this.local_name = local_name;
	}

	public BlockingQueue<TimeStampedMessage> getSendQueue(){return send_queue;}
	public HashMap<String, User> getUsers(){return users;}
	public ConcurrentLinkedQueue<TimeStampedMessage> getReceiveQueue(){return receive_queue;}
	public ConcurrentLinkedQueue<TimeStampedMessage> getDelayReceiveQueue(){return delay_receive_queue;}
	public HashMap<String, ObjectOutputStream> getCachedOutputStreams(){return cached_output_streams;}
	public ArrayList<Rule> getSendRules(){return SendRules;}
	public ArrayList<Rule> getReceiveRules(){return ReceiveRules;}
	public String getClockType(){return clock_type;}
	public int getLocalId(){return local_id;}
	public void setLocalId(int id){local_id = id;}
	/* A helper function used in init(), or when the configuration file is modified */
	public void load_config()
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(conf_filename);
			Yaml yaml = new Yaml();
			Map<String, Object> data = (Map<String, Object>)yaml.load(fis);
			this.clock_type = (String)data.get("ClockType");
			ArrayList<HashMap<String, Object> > config = (ArrayList<HashMap<String, Object> >)data.get("Configuration");
			int i = 0;
			for(HashMap<String, Object> mm : config)
			{
				String Name = (String)mm.get("Name");
				if(Name.equals(local_name))
					setLocalId(i);
				User uu = new User(Name);
				uu.setIp((String)mm.get("IP"));
				uu.setPort((Integer)mm.get("Port"));
				users.put(Name, uu);
				i++;
			}
			if(!users.containsKey(local_name))
			{
				System.err.println("local_name: " + local_name + " isn't in " + conf_filename + ", please check again!");
				System.exit(1);
			}
			ArrayList<HashMap<String, Object> > send_rule_arr = (ArrayList<HashMap<String, Object> >)data.get("SendRules");
			
			for(HashMap<String, Object> mm : send_rule_arr)
			{
				String action = (String)mm.get("Action");
				Rule r = new Rule(action);
				for(String key: mm.keySet())
				{
					if(key.equals("Src"))
						r.setSrc((String)mm.get(key));
					if(key.equals("Dest"))
						r.setDest((String)mm.get(key));
					if(key.equals("Kind"))
						r.setKind((String)mm.get(key));
					if(key.equals("ID"))
						r.setId((Integer)mm.get(key));
					if(key.equals("Nth"))
						r.setNth((Integer)mm.get(key));
					if(key.equals("EveryNth"))
						r.setEveryNth((Integer)mm.get(key));
				}
				SendRules.add(r);
			}

			ArrayList<HashMap<String, Object> > receive_rule_arr = (ArrayList<HashMap<String, Object> >)data.get("ReceiveRules");
			for(HashMap<String, Object> mm : receive_rule_arr)
			{
				String action = (String)mm.get("Action");
				Rule r = new Rule(action);
				for(String key: mm.keySet())
				{
					if(key.equals("Src"))
						r.setSrc((String)mm.get(key));
					if(key.equals("Dest"))
						r.setDest((String)mm.get(key));
					if(key.equals("Kind"))
						r.setKind((String)mm.get(key));
					if(key.equals("ID"))
						r.setId((Integer)mm.get(key));
					if(key.equals("Nth"))
						r.setNth((Integer)mm.get(key));
					if(key.equals("EveryNth"))
						r.setEveryNth((Integer)mm.get(key));
				}
				ReceiveRules.add(r);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if(fis != null)fis.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}

	public void init()
	{
		this.id = 0;
		send_queue.clear();
		receive_queue.clear();
		delay_send_queue.clear();
		delay_receive_queue.clear();
		users.clear();
		SendRules.clear();
		ReceiveRules.clear();
		cached_output_streams.clear();

		load_config();
	}

	public TimeStampedMessage receive()
	{
		TimeStampedMessage r_cq = receive_queue.poll(); // if ConcurrentLinkedQueue is empty, cq.poll return null; cq.poll() is atomic operation
		return r_cq;
	}

	public void send(TimeStampedMessage message)
	{
		message.set_id(++id);
		Rule matched_rule = CheckRule(message, 0); // check message with send rules
		try
		{
			if(matched_rule != null) // matches an action, do somethng
			{
				if(matched_rule.getAction().equals("drop")) // drop action
					; // drop it, i.e. do nothing
				else if(matched_rule.getAction().equals("duplicate")) // duplicate action
				{
					matched_rule.addMatch(); // as handout indicated, duplicated msg also matches the rule once!
					send_queue.put(message);
					while(!delay_send_queue.isEmpty())
					{
						send_queue.put(delay_send_queue.poll());
					}
					TimeStampedMessage new_m = message.deepCopy();
					new_m.set_id(++id);
					new_m.setTimeStamp(new_m.getClock().inc());
					send_queue.put(new_m);
				}
				else if(matched_rule.getAction().equals("delay"))
				{
					delay_send_queue.add(message);
				}
			}
			else  // message doesn't match any rule, add it to send_queue, and after that also need to check whether delay queue is empty or not
			{
				send_queue.put(message);
				while(!delay_send_queue.isEmpty())
				{
					send_queue.put(delay_send_queue.poll());
				}
			}
		}
		catch(InterruptedException iex)
		{
			iex.printStackTrace();
		}
	}

	/* function used for debugging information */
	public void check_status()
	{
		System.out.println("SendRules:");
		for(Rule rr: SendRules)
		{
			System.out.println(rr);
		}
		System.out.println("ReceiveRules:");
		for(Rule rr: ReceiveRules)
		{
			System.out.println(rr);
		}
		System.out.println("send_queue:");
		for(TimeStampedMessage m: send_queue)
		{
			System.out.println(m);
		}
		System.out.println("delay_send_queue:");
		for(TimeStampedMessage m: delay_send_queue)
		{
			System.out.println(m);
		}
		System.out.println("receive_queue:");
		for(TimeStampedMessage m: receive_queue)
		{
			System.out.println(m);
		}
		System.out.println("delay_receive_queue:");
		for(TimeStampedMessage m: delay_receive_queue)
		{
			System.out.println(m);
		}
	}

	/** A helper function that checks whether or not a message matches a rule. 
	 @param type: 0 - check message with SendRules; 1 - check message with ReceiveRules
	 @return action name of the rule which this message matches with; or null if this message matches with no action.
	 */
	public Rule CheckRule(TimeStampedMessage message, int type)
	{
		ArrayList<Rule> rule_arr = null;
		if(type == 0)
			rule_arr = SendRules;
		else if(type == 1)
			rule_arr = ReceiveRules;
		else
		{
			System.err.println("error use of CheckRule with type = " + type);
			System.exit(1);
		}
		for(Rule rule: rule_arr)
		{
			if((rule.getSrc() != null) && !(rule.getSrc().equals(message.getSrc())))
				continue;//not match, check next rule
			else if((rule.getDest() != null) && !(rule.getDest().equals(message.getDest())))
				continue;
			else if((rule.getKind() != null) && !(rule.getKind().equals(message.getKind())))
				continue;
			else if( (rule.getId() > 0) && (rule.getId() != message.getId()) )
				continue;

//			synchronized(rule)
//			{
				rule.addMatch(); // already matched rule!
				if((rule.getNth() > 0) && (rule.getNth() != rule.getMatched()) )
					continue;
				else if( (rule.getEveryNth() > 0) && (rule.getMatched() % rule.getEveryNth()) != 0)
					continue;
//			}
			return rule;  // match this rule
		}
		return null;  // if no rules match, return null
	}
}
