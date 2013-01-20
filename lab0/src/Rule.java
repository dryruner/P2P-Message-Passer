package bin;

public class Rule
{
	private String action;
	private String src = null;
	private String dest = null;
	private String kind = null;
	private int id = -1;
	private int nth = -1;
	private int everynth = -1;

	private int matched = 0;

	public Rule(String action)
	{
		this.action = action;
	}

	public String getAction(){return action;}
	public String getSrc(){return src;}
	public String getDest(){return dest;}
	public String getKind(){return kind;}
	public int getId(){return id;}
	public int getNth(){return nth;}
	public int getEveryNth(){return everynth;}
	public int getMatched(){return matched;}

	public void setAction(String action){this.action = action;}
	public void setSrc(String src){this.src = src;}
	public void setDest(String dest){this.dest = dest;}
	public void setKind(String kind){this.kind = kind;}
	public void setId(int id){this.id = id;}
	public void setNth(int nth){this.nth = nth;}
	public void setEveryNth(int everynth){this.everynth = everynth;}

	public void addMatch(){matched++;}
	public void resetMatch(){matched = 0;}

	public String toString()
	{
		return  ("Action:" + action + "|Src:" + src + "|Dest:" + dest
				+ "|Kind:" + kind + "|ID:" + id + "|Nth:" + nth + "|EveryNth:"
				+ everynth + " |matched: " + matched + " time(s)");
	}
}
