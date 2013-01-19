import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.*;

public class Testyaml
{
	private int i = 1;
	public static void main(String[] args)
	{
		Testyaml test = new Testyaml();
		System.out.println("i = " + test.i);
		FileInputStream fis = null;
		
		try{
			fis = new FileInputStream("config.yaml");
			
			Yaml yaml = new Yaml();
			Map<String, Object> data = (Map<String, Object>)yaml.load(fis);
			
			System.out.println(data.get("SendRules"));	
			ArrayList< Map<String, Object> > mm = (ArrayList< Map<String, Object> >)data.get("SendRules");
			for(Map<String, Object> m:mm)
			{
				if(m.get("EveryNth") != null)
					System.out.println(m.get("EveryNth"));
			}

			Object obj = 2;
			System.out.println((Integer)obj == 2);
			
/*			for(String key: data.keySet())
			{
				System.out.println(data.get(key));
			}
*/			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try{
				if(fis != null)
					fis.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
}
