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
	private WorkerQueue wq;
	private BlockingQueue send_queue = new LinkedBlockingQueue();
	private BlockingQueue<TimeStampedMessage> receive_queue = new LinkedBlockingQueue<TimeStampedMessage>();
	private Queue delay_send_queue = new LinkedList();
	private ConcurrentLinkedQueue<TimeStampedMessage> delay_receive_queue = new ConcurrentLinkedQueue<TimeStampedMessage>();
	private ConcurrentLinkedQueue<TimeStampedMessage> app_receive_queue = new ConcurrentLinkedQueue<TimeStampedMessage>();
	private ConcurrentLinkedQueue<MulticastMessage> holdback_queue = new ConcurrentLinkedQueue<MulticastMessage>();

	private HashMap<String, User> users = new HashMap<String, User>();// all users
	private ArrayList<Rule> SendRules = new ArrayList<Rule>();
	private ArrayList<Rule> ReceiveRules = new ArrayList<Rule>();
	private HashMap<String, ObjectOutputStream> cached_output_streams = new HashMap<String, ObjectOutputStream>();
	private int seq_num = 0;
	private HashMap<String, Integer> R_qg = new HashMap<String, Integer>();
	private HashMap<Integer, MulticastMessage> sent_msgs = new HashMap<Integer, MulticastMessage>();
	private HashMap<Integer, ArrayList<String> > groups = new HashMap<Integer, ArrayList<String> >();

	public MessagePasser(String conf_filename, String local_name, WorkerQueue wq)
	{
		this.conf_filename = conf_filename;
		this.local_name = local_name;
		this.wq = wq;
	}

	public BlockingQueue<TimeStampedMessage> getSendQueue(){return send_queue;}
	public HashMap<String, User> getUsers(){return users;}
	public BlockingQueue<TimeStampedMessage> getReceiveQueue(){return receive_queue;}
	public ConcurrentLinkedQueue<TimeStampedMessage> getAppReceiveQueue(){return app_receive_queue;}
	public ConcurrentLinkedQueue<TimeStampedMessage> getDelayReceiveQueue(){return delay_receive_queue;}
	public ConcurrentLinkedQueue<MulticastMessage> getHoldbackQueue(){return holdback_queue;}
	public HashMap<Integer, MulticastMessage> getSentMsgs(){return sent_msgs;}
	public HashMap<String, ObjectOutputStream> getCachedOutputStreams(){return cached_output_streams;}
	public ArrayList<Rule> getSendRules(){return SendRules;}
	public ArrayList<Rule> getReceiveRules(){return ReceiveRules;}
	public String getClockType(){return clock_type;}
	public int getLocalId(){return local_id;}
	public void setLocalId(int id){local_id = id;}
	public int incId(){return ++id;}
	public HashMap<String, Integer> getRqg(){return R_qg;}
	public int getSeqNum(){return seq_num;}

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
				R_qg.put(Name, -1);  // R_qg["name"] = -1
				if(Name.equals(local_name))
				{
					setLocalId(i);
				}
				User uu = new User(Name);
				uu.setIp((String)mm.get("IP"));
				uu.setPort((Integer)mm.get("Port"));
				users.put(Name, uu);
				i++;
				groups.put(i, (ArrayList<String>)mm.get("Group"));
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

	public void multicast_config()
	{
//		m_clock = new VectorClock(users.size(), local_id);
	}

	public void init()
	{
		this.id = 0;
		this.seq_num = 0;
		send_queue.clear();
		receive_queue.clear();
		delay_send_queue.clear();
		delay_receive_queue.clear();
		holdback_queue.clear();
		users.clear();
		SendRules.clear();
		ReceiveRules.clear();
		cached_output_streams.clear();

		load_config();
		multicast_config();
	}

	public ArrayList<TimeStampedMessage> receive()
	{
		ArrayList<TimeStampedMessage> tm_arr = new ArrayList<TimeStampedMessage>();
		while(!app_receive_queue.isEmpty())
		{
			TimeStampedMessage t_msg = app_receive_queue.poll();
			wq.getClock().syncWith(t_msg);
			tm_arr.add(t_msg);// if ConcurrentLinkedQueue is empty, cq.poll return null; cq.poll() is atomic operation
		}
		return tm_arr;
	}

	public void R_multicast(MulticastMessage r_msg, int group_num)throws InterruptedException
	{
		r_msg.setSeqNum(seq_num);
		r_msg.setMTimeStamp(wq.getMClock().inc());
		sent_msgs.put(seq_num, r_msg);
		seq_num++;
		for(String dest: groups.get(group_num))
		{
			MulticastMessage t_rmsg = r_msg.deepCopy();
			t_rmsg.setDest(dest);
			send(t_rmsg);
//			System.out.println("multicasted to: " + t_rmsg.getDest());
//			send_queue.put(t_rmsg);
		}
	}

	public void send(Message message)
	{
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
					TimeStampedMessage new_m = null;
					if(message instanceof TimeStampedMessage)
						new_m = ((TimeStampedMessage)message).deepCopy();
					else if(message instanceof MulticastMessage)
						new_m = ((MulticastMessage)message).deepCopy();
					new_m.set_id(++id);
					new_m.setTimeStamp(wq.getClock().inc());
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

	/* 0 - valid; -1: no such group; -2: node_name isn't in such group */
	public int check_group(String node_name, int group_num)
	{
		if(group_num <= 0 || group_num > groups.size())
			return -1;
		if(groups.get(group_num).contains(node_name))
			return 0;
		else
			return -2;
	}

	public void show_groups()
	{
		System.out.println("Groups: ");
		for(Integer i: groups.keySet())
		{
			System.out.println("Group " + i + ": " + groups.get(i));
		}
	}

	/* function used for debugging information */
	public void check_status()
	{
		Clock clock = wq.getClock();
		if(clock instanceof LogicalClock)
			System.out.println("Local timestamp: " + clock.getTimeStamp());
		else if(clock instanceof VectorClock)
			System.out.println("local timestamp: " + Arrays.toString((int[])clock.getTimeStamp()));
		System.out.print("Local muticast timestamp: ");
		System.out.println(Arrays.toString(wq.getMClock().getTimeStamp()));
		System.out.println("sent multicast messages: ");
		for(Integer num: sent_msgs.keySet())
		{
			System.out.println("SN: " + num + " | msg: " + sent_msgs.get(num));
		}
		System.out.println("R_qg: " + R_qg + "|SN: " + seq_num);
		
		System.out.println("holdback_queue: ");
		for(MulticastMessage r_msg: holdback_queue)
		{
			System.out.println(r_msg);
		}
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
		for(Object m: send_queue)
		{
			System.out.println(m);
		}
		System.out.println("delay_send_queue:");
		for(Object m: delay_send_queue)
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
	public Rule CheckRule(Message message, int type)
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
