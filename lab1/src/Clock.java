package bin;

public interface Clock<T,R> extends java.io.Serializable
{
	public int compare(T tm1, T tm2);
	public T inc();
	public void syncWith(R t_msg);
	public T getTimeStamp();
	public boolean equal(T tm1, T tm2);
}
