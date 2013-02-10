package bin;

public class ClockFactory
{
	private MessagePasser mp;
	private Clock clock = null; /* cache clock */
	public ClockFactory(MessagePasser mp)
	{
		this.mp = mp;
	}

	public Clock getClock()
	{
		if(clock == null)
		{
			if(mp.getClockType().equals("Logical"))
			{
				clock = new LogicalClock();
			}
			else if(mp.getClockType().equals("Vector"))
			{
				clock = new VectorClock(mp.getUsers().size(), mp.getLocalId());
			}
			else
			{
				System.err.println("Clock type error!");
				System.exit(1);
			}
		}
		return clock;
	}
}
