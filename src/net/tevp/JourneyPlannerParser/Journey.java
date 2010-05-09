package net.tevp.JourneyPlannerParser;

import java.util.*;

public class Journey extends Vector<JourneySegment>
{
	public String toString()
	{
		StringBuffer sb = new StringBuffer("Journey:");
		for (int i=0;i<size();i++)
		{
			sb.append(String.format("\n\t- %s", get(i)));
		}
		return sb.toString();
	}

	public void corrections()
	{
		for (int i=0;i<size();i++)
		{
			if (i!=size()-1 && get(i).loc_end == "")
				get(i).loc_end = get(i+1).loc_start;
			if (get(i).time_start == null && get(i).time_end!= null && get(i).minutes != 0)
			{
				Calendar cal = Calendar.getInstance();
				cal.setTime(get(i).time_end);
				cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - get(i).minutes);
				get(i).time_start = cal.getTime();
			}
			if (get(i).time_start != null && get(i).time_end == null && get(i).minutes != 0)
			{
				Calendar cal = Calendar.getInstance();
				cal.setTime(get(i).time_start);
				cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + get(i).minutes);
				get(i).time_end = cal.getTime();
			}
			if (i!=0 && get(i-1).time_end == null && get(i).time_start!=null)
				get(i-1).time_end = (Date)get(i).time_start.clone();
			if (i!=0 && get(i).time_start == null && get(i-1).time_end!=null)
				get(i).time_start = (Date)get(i-1).time_end.clone();
		}
	}
	
	public JourneySegment last()
	{
		return this.get(size()-1);
	}
}


