package net.tevp.JourneyPlannerParser;

import java.util.*;
import java.util.regex.*;

public class Journey extends Vector<JourneySegment>
{
	public String toString()
	{
		StringBuffer sb = new StringBuffer("Journey: "+formattedMinutes());
		for (int i=0;i<size();i++)
		{
			sb.append(String.format("\n\t- %s", get(i)));
		}
		return sb.toString();
	}

	public String formattedMinutes()
	{
		int minutes = getMinutes();
		if (minutes == 1)
			return "1 minute";
		else if (minutes < 60)
			return String.format("%d minutes", minutes);
		else if (minutes < 120)
			return String.format("1 hour and %d minutes", minutes - 60);
		else
			return String.format("%d hours and %d minutes", minutes/60, minutes%60);
	}

	public int getMinutes()
	{
		return (int)((last().time_end.getTime()-get(0).time_start.getTime())/1000)/60;
	}

	private String stripString(String inp)
	{
		Pattern noSpaces = Pattern.compile("\\s");
		String[] bits = noSpaces.split(inp);
		StringBuffer sb = new StringBuffer();
		for (String s: bits)
		{
			sb.append(s);
			sb.append(" ");
		}
		return sb.toString().trim();
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

			get(i).loc_start = stripString(get(i).loc_start);
			get(i).loc_end = stripString(get(i).loc_end);
		}
	}
	
	public JourneySegment last()
	{
		return this.get(size()-1);
	}
}


