package net.tevp.JourneyPlannerParser;

import java.util.*;

public class JourneySegment
{
	public TransportType type;
	public Date time_start, time_end;
	public String loc_start, loc_end;
	public Vector<Route> routes = new Vector<Route>();
	public int minutes;
	public Vector<Impediments> impediments = new Vector<Impediments>();

	JourneySegment()
	{
		loc_start = loc_end = "";
		time_start = null;
		time_end = null;
	}

	public String toString()
	{
		String rs = "";
		for(Route r: routes)
		{
			if (rs != "")
				rs += " ";
			if (r.stop != null)
				rs += String.format("(%s @ stop %s towards %s)", r.thing, r.stop, r.towards);
			else
				rs += String.format("(%s towards %s)", r.thing, r.towards);
		}
		
		if (rs != "")
			rs = " - "+rs;
		return String.format("JourneySegment - %s : %s,%s - %s,%s%s",type,time_start,loc_start,time_end,loc_end,rs);
	}
}

