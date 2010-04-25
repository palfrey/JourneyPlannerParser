import java.util.regex.*;
import java.io.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;
import java.util.*;

class JourneyPlannerParser
{
	public static void main(String [] args)
	{
		JourneyPlannerParser jpp = new JourneyPlannerParser();
		try {
			FileReader fr = new FileReader(args[0]);
			int length = (int)new File(args[0]).length();
			char[] buffer = new char[length];
			fr.read(buffer, 0, length);
			Vector<Journey> js = jpp.parseString(new String(buffer));
			for (int i=0;i<js.size();i++)
			{
				System.out.println(js.get(i));
				System.out.println("");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	Pattern route, tds, alt, departing, strip_link;
	Pattern walk_to, tube_to, tube_direct, bus_to;
	Pattern transit_time;

	JourneyPlannerParser()
	{
		route = Pattern.compile("<table class=\"routedetails\">(.+?)</table>");
		tds = Pattern.compile("<td[^>]*?>(.*?)</td>");
		alt = Pattern.compile("alt=\"([^\"]+)\"");
		departing = Pattern.compile("<strong>Departing:&nbsp;\n\\s+</strong>(\\S+)&nbsp;\n\n\\s+(\\d+)&nbsp;\\s+(\\S+)\n\n\\s+ &nbsp;(\\d+) at: (\\d+):(\\d+)</li>");
		strip_link = Pattern.compile("<a href=\"[^\"]+\">([^<]+)</a>");
		walk_to = Pattern.compile("Walk to (.+?)<br>");
		tube_to = Pattern.compile("(?:T|t)ake the (.+?<br><br>)<span class=\"zoneinfo\">(?:Z|z)one\\(s\\): ([\\d, ]+)</span>");
		tube_direct = Pattern.compile("<span class=\"\\S+\">(.+?)</span> towards (.+?)<br>");
		bus_to = Pattern.compile("Take the Route Bus (\\d+) from Stop:  (\\S)<br> towards (.+?)<br><br>");
		transit_time = Pattern.compile("time: (\\d+) mins");
	}

	Vector<Journey> parseString(String data) throws Exception
	{
		Vector<Journey> res = new Vector<Journey>();
		
		Matcher d = departing.matcher(data);
		d.find();

		Calendar base = new GregorianCalendar();
		base.set(Calendar.DAY_OF_MONTH, Integer.parseInt(d.group(2)));
		DateFormatSymbols dfs = new DateFormatSymbols();
		
		base.set(Calendar.MONTH, Arrays.asList(dfs.getMonths()).indexOf(d.group(3)));
		base.set(Calendar.YEAR, Integer.parseInt(d.group(4)));
		base.set(Calendar.HOUR, Integer.parseInt(d.group(5)));
		base.set(Calendar.MINUTE, Integer.parseInt(d.group(6)));
		//System.out.println(base.getTime());

		Matcher r = route.matcher(data);
		while (r.find())
		{
			//System.out.println(s.group(1));
			//System.out.println("New Match\n=========");
			Matcher tdlist = tds.matcher(r.group(1));
			int type = 0;
			Journey j = new Journey();
			JourneySegment js = null;
			boolean end_of_journey = false;
			while (tdlist.find())
			{	
				if (!end_of_journey)
				{
					switch(type)
					{
						case 0: // time and type
						{
							//System.out.println(tdlist.group(1));
							Matcher a = alt.matcher(tdlist.group(1));
							if (a.find())
							{
								js = new JourneySegment();
								if (a.group(1).equals("Walk"))
									js.type = TransportType.Walk;
								else if (a.group(1).equals("Tube"))
									js.type = TransportType.Tube;
								else if (a.group(1).equals("Bus"))
									js.type = TransportType.Bus;
								else
									throw new Exception(a.group(1));

								if (tdlist.group(1).indexOf(":")!=-1)
								{
									Calendar ts = (Calendar)base.clone();
									ts.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tdlist.group(1).substring(0,2)));
									ts.set(Calendar.MINUTE, Integer.parseInt(tdlist.group(1).substring(3,5)));
									js.time_start = ts.getTime();
									int len = tdlist.group(1).length();
									ts.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tdlist.group(1).substring(len-5,len-3)));
									ts.set(Calendar.MINUTE, Integer.parseInt(tdlist.group(1).substring(len-2,len)));
									js.time_end = ts.getTime();
								}
								//System.out.println(js);
							}
							else
							{
								//System.out.println("end of journey");
								end_of_journey = true;
								j.corrections();
								res.add(j);
							}
							break;
						}
						case 1:
						{
							//System.out.println(tdlist.group(1));
							String segment = tdlist.group(1);
							segment = segment.substring(0,segment.indexOf("<br"));
							if (segment.indexOf("<a")!=-1)
							{
								Matcher rep = strip_link.matcher(segment);
								rep.find();
								segment = rep.group(1);
							}
							js.loc_start = segment;
							switch (js.type)
							{
								case Walk:
								{
									Matcher w = walk_to.matcher(tdlist.group(1));
									w.find();
									js.loc_end = w.group(1);
									break;
								}
								case Tube:
								{
									Matcher t = tube_to.matcher(tdlist.group(1));
									t.find();
									Matcher t2 = tube_direct.matcher(t.group(1));
									while (t2.find())
									{
										Route ro = new Route();
										ro.thing = t2.group(1);
										ro.towards = t2.group(2);
										js.routes.add(ro);
									}
									break;
								}
								case Bus:
								{
									Matcher b = bus_to.matcher(tdlist.group(1));
									b.find();
									Route ro = new Route();
									ro.thing = b.group(1)+ " - "+b.group(2);
									ro.towards = b.group(3);
									js.routes.add(ro);
									break;
								}

								default:
									throw new Exception(segment);
							}
							//System.out.println(js);
							break;
						}
						case 2:
						{
							//System.out.println(tdlist.group(1));
							Matcher time = transit_time.matcher(tdlist.group(1));
							time.find();
							js.minutes = Integer.parseInt(time.group(1));
							Matcher alts = alt.matcher(tdlist.group(1));
							while (alts.find())
							{
								if (alts.group(1).equals("stairs up"))
									js.impediments.add(Impediments.StairsUp);
								else if (alts.group(1).equals("stairs down"))
									js.impediments.add(Impediments.StairsDown);
								else
									throw new Exception(alts.group(1));
							}
							break;
						}

						case 3:
							//System.out.println(js);
							j.add(js);
							break;
							
						default:
							System.out.print("Type: ");
							System.out.print(type);
							System.out.print(" - ");
							System.out.println(tdlist.group(1));
							System.out.println("");
							throw new Exception();
							//break;
					}
				}
				type = (type +1) % 4;
				if (end_of_journey && type == 0)
					end_of_journey = false;
			}
		}
		return res;
	}
}

enum TransportType
{
	Walk,
	Bus,
	Tube,
	Tram,
	DLR,
	Overground
}

class Route
{
	public String thing;
	public String towards;
}

enum Impediments
{
	StairsUp,
	StairsDown
}

class JourneySegment
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
		time_start = new Date();
		time_end = new Date();
	}

	public String toString()
	{
		return "JourneySegment - "+type.toString()+" : "+time_start.toString()+","+loc_start.toString()+" - "+time_end.toString()+","+loc_end.toString();
	}
}

class Journey extends Vector<JourneySegment>
{
	public String toString()
	{
		StringBuffer sb = new StringBuffer("Journey -");
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
		}
	}

}
