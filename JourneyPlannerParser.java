import java.util.regex.*;
import java.io.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

class JourneyPlannerParser
{
	public static void main(String [] args)
	{
		try
		{
			JourneyPlannerParser jpp = new JourneyPlannerParser(false);
			Vector<Journey> js;
			js = jpp.doJourney(JourneyLocation.postcode("E3 4AE"),JourneyLocation.postcode("SW7 2AZ"), new JourneyParameters());
			for (int i=0;i<js.size();i++)
			{
				System.out.println(i);
				System.out.println(js.get(i));
				System.out.println("");
			}
			assert js.size()!=0;
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}

	Pattern route, tds, alt, departing, strip_link;
	Pattern walk_to, tube_to, tube_direct, bus_to;
	Pattern transit_time, payonboard;

	boolean debug;

	JourneyPlannerParser(boolean _debug)
	{
		debug = _debug;

		route = Pattern.compile("<table class=\"routedetails\">(.+?)</table>", Pattern.DOTALL);
		tds = Pattern.compile("<td[^>]*?>(.*?)</td>", Pattern.DOTALL);
		alt = Pattern.compile("alt=\"([^\"]+)\"");
		departing = Pattern.compile("<strong>Departing:[^\n]+\n\\s+</strong>(\\S+)[^\n]*\n[^\n]*\n[^\\d]+(\\d+)[^\n]*\n\\s+(\\S+)\\s\n[^\n]*\n[^\\d]+(\\d+) at: (\\d+):(\\d+)</li>");
/*<strong>Departing: 
							</strong>Saturday 

  17 
    April

   2010 at: 23:54 */
		strip_link = Pattern.compile("<a href=\"[^\"]+\">([^<]+)</a>");
		walk_to = Pattern.compile("Walk to (.+?)<br");
		tube_to = Pattern.compile("(?:T|t)ake(?: the )?(.+?<br /><br />)<span class=\"zoneinfo\">(?:Z|z)one\\(s\\): ([\\d, ]+)</span>", Pattern.DOTALL);
		tube_direct = Pattern.compile("<span class=\"\\S+\">(.+?)</span> towards (.+?)<br>");
		bus_to = Pattern.compile("Take the Route (?:Express )?Bus ([A-Z\\d]+) from Stop:  ([\\S\\d]+)<br[^>]*> towards (.+?)<br");
		transit_time = Pattern.compile("time:\\s(\\d+).+?mins", Pattern.DOTALL);
		payonboard = Pattern.compile("<table cellspacing=\"0\".*?</table>");
	}

	Vector<Journey> doJourney(JourneyLocation start, JourneyLocation end, JourneyParameters params) throws ParseException
	{
		HashMap<String,String> m = new HashMap<String,String>();
		m.put("language","en");
		m.put("sessionID","0");
		m.put("requestID","0");
		m.put("ptOptionsActive","1");
		m.put("itOptionsActive","1");
		m.put("imparedOptionsActive","1");
		m.put("ptAdvancedOptions","1");
		m.put("advOptActive_2","1");
		m.put("advOpt_2","1");
		m.put("execInst","normal");
		m.put("command","");
		m.put("itdLPxx_request","");
		m.put("itdLPxx_view","");
		m.put("itdLPxx_tubeMap","");
		m.put("calculateDistance","1");
		m.put("imageFormat","png/pdf");
		m.put("imageOnly","1");
		m.put("imageWidth","705");
		m.put("imageHeight","500");
		m.put("calculateCO2","1");
		m.put("name_origin",start.data);
		m.put("nameState_origin","notidentified");
		m.put("nameDefaultText_origin","start");
		m.put("place_origin","London");
		m.put("type_origin",start.getTFLName());
		m.put("name_destination",end.data);
		m.put("nameState_destination","notidentified");
		m.put("nameDefaultText_destination","end");
		m.put("type_destination",end.getTFLName());
		m.put("place_destination","London");
		m.put("itdTripDateTimeDepArr","dep");
		m.put("itdDateDay","17");
		m.put("itdDateYearMonth","201004");
		m.put("itdTimeHour","23");
		m.put("itdTimeMinute","54");
		m.put("Submit","Search");
		m.put("routeType","LEASTTIME");
		m.put("name_via","Enter location+%28optional%29");
		m.put("nameState_via","notidentified");
		m.put("nameDefaultText_via","Enter location+%28optional%29");
		m.put("type_via","stop");
		m.put("place_via","London");
		m.put("placeDefaultText_via","London");
		m.put("includedMeans","checkbox");
		m.put("inclMOT_11","1");
		m.put("inclMOT_0","on");
		m.put("inclMOT_1","on");
		m.put("inclMOT_2","on");
		m.put("inclMOT_4","on");
		m.put("inclMOT_5","on");
		m.put("inclMOT_7","on");
		m.put("inclMOT_9","on");
		m.put("trITMOTvalue101","60");
		m.put("trITMOTvalue","20");
		m.put("trITMOT","100");
		m.put("changeSpeed","normal");
		m.put("tripSelection","on");
		m.put("itdLPxx_view","detail");
		m.put("ptOptionsActive","1");
		m.put("calculateDistance","1");
		m.put("tripSelector1","on");
		m.put("tripSelector2","on");
		m.put("tripSelector3","on");
		m.put("tripSelector4","on");
		m.put("tripSelector5","on");
		m.put("tripSelector6","on");
		m.put("tripSelector7","on");
		m.put("Submit","View selected");

		StringBuffer sb = new StringBuffer();
		for (String key: m.keySet())
		{
			if (sb.length()>0)
				sb.append("&");
			try {
				sb.append(String.format("%s=%s", key, URLEncoder.encode(m.get(key),"UTF-8")));
			}
			catch (UnsupportedEncodingException e)
			{
				throw new ParseException("Error trying to encode '"+m.get(key)+"'");
			}
		}
		if (debug)
			System.out.println(sb.toString());

		BufferedURLConnection buc = null;
		try 
		{
			buc = new BufferedURLConnection("http://journeyplanner.tfl.gov.uk/user/XSLT_TRIP_REQUEST2", sb.toString());
		}
		catch (IOException e)
		{
			throw new ParseException("IOException trying to get data from TfL: "+e.getMessage());
		}
		if (debug)
			System.out.println(buc.headers);

		if (debug)
		{
			try
			{
				FileOutputStream fs = new FileOutputStream("dump.html");
				fs.write(buc.outputData.getBytes(),0,buc.outputData.length());
				fs.close();
			}
			catch (FileNotFoundException e)
			{
				throw new ParseException("Failure to write dump.html for debug: "+e.getMessage());
			}
			catch (IOException e)
			{
				throw new ParseException("IOException while writing dump.html for debug: "+e.getMessage());
			}
		}

		return parseString(buc.outputData);
	}

	Vector<Journey> parseString(String data) throws ParseException
	{
		Vector<Journey> res = new Vector<Journey>();
		
		Matcher d = departing.matcher(data);
		d.find();
		//System.out.println(d.group(0));

		Calendar base = new GregorianCalendar();
		base.set(Calendar.DAY_OF_MONTH, Integer.parseInt(d.group(2)));
		DateFormatSymbols dfs = new DateFormatSymbols();
		
		base.set(Calendar.MONTH, Arrays.asList(dfs.getMonths()).indexOf(d.group(3)));
		base.set(Calendar.YEAR, Integer.parseInt(d.group(4)));
		base.set(Calendar.HOUR, Integer.parseInt(d.group(5)));
		base.set(Calendar.MINUTE, Integer.parseInt(d.group(6)));
		base.set(Calendar.SECOND, 0);
		//System.out.println(base.getTime());
		
		Matcher pb = payonboard.matcher(data);

		Matcher r = route.matcher(pb.replaceAll(""));
		while (r.find())
		{
			//System.out.println(r.group(1));
			if (debug)
				System.out.println("New Match\n=========");
			Matcher tdlist = tds.matcher(r.group(1));
			int type = 0;
			Journey j = new Journey();
			JourneySegment js = null;
			boolean end_of_journey = false;
			while (tdlist.find())
			{	
				if (!end_of_journey)
				{
					if (debug)
					{
						System.out.print("Type: ");
						System.out.print(type);
						System.out.print(" - ");
						System.out.println(tdlist.group(1));
						System.out.println("");
					}
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
									throw new ParseException("Unknown transit type: "+a.group(1));

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
								else
									js.time_start = (Date)j.last().time_end.clone();
							}
							else
							{
								if (tdlist.group(1).indexOf(":")!=-1)
								{
									Calendar ts = (Calendar)base.clone();
									ts.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tdlist.group(1).substring(0,2)));
									ts.set(Calendar.MINUTE, Integer.parseInt(tdlist.group(1).substring(3,5)));
									j.last().time_end = ts.getTime();
								}
								end_of_journey = true;
								j.corrections();
								res.add(j);
							}
							break;
						}
						case 1:
						{
							if (debug)
							{
								System.out.println("");
								System.out.println(tdlist.group(1));
							}
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
							}
							//System.out.println(js);
							break;
						}
						case 2:
						{
							if (debug)
								System.out.println(tdlist.group(1));
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
								else if (alts.group(1).equals("lift up"))
									js.impediments.add(Impediments.LiftUp);
								else
									throw new ParseException("Unknown impediment type: "+alts.group(1));
							}
							break;
						}

						case 3:
							//System.out.println(js);
							j.add(js);
							break;
					}
				}
				type = (type +1) % 4;
				if (end_of_journey && type == 0)
					end_of_journey = false;
			}
			assert js!=null;
		}
		assert res.size()!=0;
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
	StairsDown,
	LiftUp
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
		}
	}
	
	public JourneySegment last()
	{
		return this.get(size()-1);
	}
}

class BufferedURLConnection
{
	boolean buffered;
	String inputData, filename;
	URLConnection uc = null;
	
	public Map<String, List<String>> headers;
	public String outputData;
	
	@SuppressWarnings("unchecked") 	
	public BufferedURLConnection(String url, String data) throws IOException
	{
		inputData = data;
		filename = String.format("%d-%d.cache", url.hashCode(), inputData.hashCode());
		if (new File(filename).exists())
		{
			ObjectInputStream rd = new ObjectInputStream(new FileInputStream(filename));
			try
			{
				headers = (Map<String, List<String>>)rd.readObject();
				outputData = (String)rd.readObject();
			}
			catch (ClassNotFoundException e)
			{
				/* really shouldn't happen, but re-throw just in case */
				throw new IOException("ClassNotFoundException! That's pretty damn weird: "+e.getMessage());
			}
			buffered = true;
			return;
		}
		uc = new URL(url).openConnection();
		if (inputData != "")
		{
			uc.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
			wr.write(inputData);
			wr.flush();
		}
		buffered = false;
		headers = uc.getHeaderFields();

		outputData = "";
		InputStream is = uc.getInputStream();
		while (true)
		{
			byte[] buffer = new byte[1024];
			int bytes = is.read(buffer, 0, 1024);
			if (bytes == -1)
				break;
			outputData += new String(buffer, 0, bytes);
		}

		ObjectOutputStream dumper = new ObjectOutputStream(new FileOutputStream(filename));
		dumper.writeObject(headers);
		dumper.writeObject(outputData);
		dumper.close();
	}

	public BufferedURLConnection(String url) throws IOException
	{
		this(url, "");
	}
}

enum LocationType
{
	Postcode("locator"),
	Stop("stop"),
	Address("address"),
	PlaceOfInterest("poi");

	private String tflname;

	private LocationType(String tflname)
	{
		this.tflname = tflname;
	}

	public String getTFLName()
	{
		return tflname;
	}
}


class JourneyLocation
{
	private LocationType type;
	public String data;
	
	JourneyLocation(LocationType lt, String _data)
	{
		type = lt;
		data = _data;
	}

	static JourneyLocation postcode(String data)
	{
		return new JourneyLocation(LocationType.Postcode, data);
	}

	public String getTFLName()
	{
		return type.getTFLName();
	}
}

class JourneyParameters
{
}

class ParseException extends Exception
{
	ParseException(String msg) {super(msg);}
}
