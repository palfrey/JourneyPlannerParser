package net.tevp.journeyplannerparser;

import java.util.regex.*;
import java.io.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;
import java.util.*;
import java.net.URLEncoder;

public class JourneyPlannerParser
{
	public static void main(String [] args)
	{
		try
		{
			JourneyPlannerParser jpp = new JourneyPlannerParser(true);
			Vector<Journey> j;
			JourneyParameters jp = new JourneyParameters();
			jp.when = new GregorianCalendar(2013, 3, 14, 11, 36).getTime();
			jp.speed = Speed.fast;
			//j = jpp.doJourney(LocationType.Postcode.create("E3 4AE"),LocationType.Postcode.create("SW7 2AZ"), jp);
			j = jpp.doJourney(LocationType.Postcode.create("SE4 2DS"),LocationType.Postcode.create("BR3 1JD"), jp);
			//j = jpp.doJourney(LocationType.Postcode.create("n19 3qn"),LocationType.Postcode.create("n19 3qn"), jp);
			//js = jpp.doJourney(LocationType.Stop.create("Kings Cross"),LocationType.Postcode.create("E8 1JH"), jp);
			for (int i=0;i<j.size();i++)
			{
				System.out.println(i);
				System.out.println(j.get(i));
				System.out.println("");
				assert j.get(i).getMinutes()<100;
				assert j.get(i).getMinutes()>0;
				for (JourneySegment js: j.get(i))
					assert js.time_start.getTime() < js.time_end.getTime();
			}
			assert j.size()!=0;
		}
		catch (AmbiguousLocationException e)
		{
			System.out.println(e.original);
			System.out.println(e.options);
			e.printStackTrace();
		}
		catch (DodgyLocationException e)
		{
			System.out.println(e.original);
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}

	Pattern route, tds, alt, motion, strip_link;
	Pattern walk_to, tube_to, tube_direct, bus_to, replacement_bus_to, rail_to, boat_to, tram_to;
	Pattern transit_time, payonboard;
	Pattern fieldset, legend, option;
	Pattern tflerror, strip_span, timeMatch;
	static String unhappylocation = "id=\"from-postcode\" disabled=\"disabled";

	boolean debug;

	public JourneyPlannerParser(boolean _debug)
	{
		debug = _debug;

		route = Pattern.compile("<table class=\"routedetails cb\">(.+?)</table><div id=\"journeytext", Pattern.DOTALL);
		tds = Pattern.compile("<td[^>]*?>(.*?)</td>(?!<td>\\d+)", Pattern.DOTALL);
		alt = Pattern.compile("alt=\"([^\"]+)\"");
		motion = Pattern.compile("<strong>(?:Leaving|Arriving)</strong> on \\S+ (\\d+) (\\S+) (\\d+) at (\\d+):(\\d+)");
		// Leaving</strong> on Sat 13 Apr 2013 at 10:32
		strip_link = Pattern.compile("<a[^>]+href=\"[^\"]+\">([^<]+)</a>");
		walk_to = Pattern.compile("Walk to ([^<]+)", Pattern.DOTALL);
		tube_to = Pattern.compile("(?:T|t)ake(?: the )?(.+<br />)", Pattern.DOTALL);
		tube_direct = Pattern.compile("<span class=\"[^\"]+\">([^<]+)</span> towards ([^\\s].+?)<br");
		bus_to = Pattern.compile("Route (?:Express )?Bus ([A-Z\\d]+) from Stop:  ([\\S\\d]+)<br[^>]*> towards (.+?)<br");
		replacement_bus_to = Pattern.compile("Take Route (.+?) towards ([^<]+)<br", Pattern.DOTALL);

		rail_to = Pattern.compile("Take.+?<b>(.+?)</b> towards ([^<]+)<br", Pattern.DOTALL);
		boat_to = Pattern.compile("Boat\\s+Thames Clipper towards ([^<]+)<br");
		tram_to = Pattern.compile("Take(.+?) towards ([^<]+)<br", Pattern.DOTALL);
		
		strip_span = Pattern.compile("<span[^>]+>([^<]+)</span>");
		transit_time = Pattern.compile("(\\d+).+?mins", Pattern.DOTALL);
		payonboard = Pattern.compile("<table cellspacing=\"0\".*?</table>");

		fieldset = Pattern.compile("<fieldset(.*?)</fieldset>", Pattern.DOTALL);
		legend = Pattern.compile("<legend>(.*?)</legend>");
		option = Pattern.compile("<option[^>]+>(.*?)</option>");
			
		tflerror = Pattern.compile("<p class=\"routealert-red-full\">([^<]+)</p>");

		timeMatch = Pattern.compile("(\\d{2}):(\\d{2})");
	}

	public Vector<Journey> doJourney(JourneyLocation start, JourneyLocation end, JourneyParameters params) throws ParseException
	{
		return runAsyncJourney(doAsyncJourney(start,end,params));
	}

	public static JourneyQuery doAsyncJourney(JourneyLocation start, JourneyLocation end, JourneyParameters params)
	{
		JourneyQuery jq = new JourneyQuery();
		jq.start = start;
		jq.end = end;
		jq.params = params;
		return jq;	
	}

	public Vector<Journey> runAsyncJourney(JourneyQuery jq) throws ParseException
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
		m.put("name_origin",jq.start.data);
		m.put("nameState_origin","notidentified");
		m.put("nameDefaultText_origin","start");
		m.put("place_origin","London");
		m.put("type_origin",jq.start.getTFLName());
		m.put("name_destination",jq.end.data);
		m.put("nameState_destination","notidentified");
		m.put("nameDefaultText_destination","end");
		m.put("type_destination",jq.end.getTFLName());
		m.put("place_destination","London");
		m.put("itdTripDateTimeDepArr",jq.params.timeType.getDetails());

		Calendar time = new GregorianCalendar();
		time.setTime(jq.params.when);
		m.put("itdDateDay",String.format("%02d", time.get(Calendar.DAY_OF_MONTH)));
		m.put("itdDateYearMonth", String.format("%4d%02d", time.get(Calendar.YEAR),time.get(Calendar.MONTH)+1));
		m.put("itdTimeHour",String.format("%02d", time.get(Calendar.HOUR_OF_DAY)));
		m.put("itdTimeMinute",String.format("%02d", time.get(Calendar.MINUTE)));

		m.put("Submit","Search");
		m.put("routeType",jq.params.routeType.getDetails());
		m.put("nameDefaultText_via","Enter location+%28optional%29");
		m.put("nameState_via","notidentified");
		if (jq.params.via == null)
		{
			m.put("name_via","Enter location+%28optional%29");
			m.put("type_via","stop");
		}
		else
		{
			m.put("name_via",jq.params.via.data);
			m.put("type_via",jq.params.via.getTFLName());
		}
		m.put("place_via","London");
		m.put("placeDefaultText_via","London");
		m.put("includedMeans","checkbox");
		m.put("inclMOT_11","1");
		m.put("inclMOT_0",jq.params.useRail?"on":"off");
		m.put("inclMOT_1",jq.params.useDLR?"on":"off");
		m.put("inclMOT_2",jq.params.useTube?"on":"off");
		m.put("inclMOT_4",jq.params.useTram?"on":"off");
		m.put("inclMOT_5",jq.params.useBus?"on":"off");
		m.put("inclMOT_7",jq.params.useCoach?"on":"off");
		m.put("inclMOT_9",jq.params.useRiver?"on":"off");
		m.put("trITMOTvalue101","60");
		m.put("trITMOTvalue","20");
		m.put("trITMOT","100");
		m.put("changeSpeed",jq.params.speed.toString());
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
		for (String key: new TreeSet<String>(m.keySet()))
		{
			if (sb.length()>0)
				sb.append("&");
			try {
				sb.append(String.format("%s=%s", key, URLEncoder.encode(m.get(key),"UTF-8")));
			}
			catch (UnsupportedEncodingException e)
			{
				throw new ParseException("Error trying to encode '"+m.get(key)+"'", e);
			}
		}
		if (debug)
			System.out.println(sb.toString());

		BufferedURLConnection buc = null;
		try 
		{
			buc = new BufferedURLConnection("http://journeyplanner.tfl.gov.uk/user/XSLT_TRIP_REQUEST2", sb.toString(), debug);
		}
		catch (IOException e)
		{
			throw new ConnectionException("IOException trying to get data from TfL: "+e.getMessage(), e);
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
				throw new ParseException("Failure to write dump.html for debug: "+e.getMessage(), e);
			}
			catch (IOException e)
			{
				throw new ParseException("IOException while writing dump.html for debug: "+e.getMessage(), e);
			}
		}

		return parseString(jq.start,jq.end,buc.outputData);
	}

	public Vector<Journey> parseString(String data) throws ParseException
	{
		return parseString(null, null, data);
	}

	private int normaliseHour(int hour, Calendar base)
	{
		int baseHour = base.get(Calendar.HOUR_OF_DAY);
		if (debug)
			System.out.println("hour is "+hour+" and base hour is "+baseHour);

		if (hour<baseHour-6)
		{
			hour += 24;
			if (debug)
				System.out.println("add a day");
		}
		else if (baseHour+12 < hour)
		{
			if (debug)
				System.out.println("delete a day as hour is "+hour);
			hour -= 24; // there's been a wraparound...
		}
		return hour;
	}

	public Vector<Journey> parseString(JourneyLocation start, JourneyLocation end, String data) throws ParseException
	{
		Matcher error = tflerror.matcher(data);
		
		Vector<Journey> res = new Vector<Journey>();
		
		Matcher d = motion.matcher(data);
		if (!d.find())
		{
			if (data.indexOf(unhappylocation)!=-1) // very messed up location
				throw new DodgyLocationException();
			Matcher field = fieldset.matcher(data);
			if (field.find())
			{
				AmbiguousLocationException ale = new AmbiguousLocationException();
				Matcher leg = legend.matcher(field.group(1));
				if (leg.find())
				{
					if (leg.group(1).compareTo("From")==0)
						ale.original = start;
					else if (leg.group(1).compareTo("Travelling to...")==0)
						ale.original = end;
					else
					{	
						if (error.find())
							throw new TFLRequestException(error.group(1));
						throw new ParseException(leg.group(1));
					}
				}
				else
				{
					if (error.find())
						throw new TFLRequestException(error.group(1));
					throw new ParseException(field.group(1));
				}

				Matcher opts = option.matcher(field.group(1));
				ale.options = new Vector<String>();
				while (opts.find())
				{
					ale.options.add(opts.group(1));
				}
				if (ale.options.size()==0)
				{
					if (error.find())
					{
						if (error.group(1).compareTo("Journey Planner has noticed that the start is too close to the destination.")==0)
							throw new TooCloseException(start);
					}
					throw new DodgyLocationException(ale.original);
				}
					
				throw ale;
			}
		}

		if (error.find())
			throw new TFLRequestException(error.group(1));

		Calendar base = new GregorianCalendar();
		base.set(Calendar.DAY_OF_MONTH, Integer.parseInt(d.group(1)));
		DateFormatSymbols dfs = new DateFormatSymbols();
		
		base.set(Calendar.MONTH, Arrays.asList(dfs.getShortMonths()).indexOf(d.group(2)));
		base.set(Calendar.YEAR, Integer.parseInt(d.group(3)));
		base.set(Calendar.HOUR_OF_DAY, Integer.parseInt(d.group(4)));
		base.set(Calendar.MINUTE, Integer.parseInt(d.group(5)));
		base.set(Calendar.SECOND, 0);
		if (debug)
			System.out.println(base.getTime());
		
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
				if (!end_of_journey || type==0) // do an extra type 0 to get the end name
				{
					while (tdlist.group(1).indexOf("javascript:mdvJpMaps")!=-1)
					{
						if (debug) {
							System.out.print("Skipping: ");
							System.out.println(tdlist.group(1));
						}
						if (!tdlist.find())
							break;
					}
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
								else if (a.group(1).equals("Rail"))
									js.type = TransportType.Rail;
								else if (a.group(1).equals("River"))
									js.type = TransportType.River;
								else if (a.group(1).equals("DLR"))
									js.type = TransportType.DLR;
								else if (a.group(1).equals("Tram"))
									js.type = TransportType.Tram;
								else
									throw new ParseException("Unknown transit type: "+a.group(1));

								if (tdlist.group(1).indexOf(":")!=-1)
								{
									if (debug)
										System.out.println("time parse: "+tdlist.group(1));
									Calendar ts = Calendar.getInstance();
									ts.setTime(base.getTime());
									int hour = normaliseHour(Integer.parseInt(tdlist.group(1).substring(0,2)), base);
									ts.set(Calendar.HOUR_OF_DAY, hour);
									ts.set(Calendar.MINUTE, Integer.parseInt(tdlist.group(1).substring(3,5)));
									js.time_start = ts.getTime();
									
									ts = Calendar.getInstance();
									ts.setTime(base.getTime());
									try
									{
										int len = tdlist.group(1).length();
										hour = normaliseHour(Integer.parseInt(tdlist.group(1).substring(len-5,len-3)), base);
										ts.set(Calendar.HOUR_OF_DAY, hour);
										ts.set(Calendar.MINUTE, Integer.parseInt(tdlist.group(1).substring(len-2,len)));
										js.time_end = ts.getTime();

										if (j.last()!=null && j.last().time_end == null)
											j.last().time_end = (Date)js.time_start.clone();
									}
									catch (NumberFormatException e)
									{
										assert tdlist.group(1).substring(5).indexOf(":")==-1;
									}
								}
								else
								{
									if (j.last()!=null && j.last().time_end != null)
										js.time_start = (Date)j.last().time_end.clone();
								}
							}
							else
							{
								Matcher tm = timeMatch.matcher(tdlist.group(1));
								if (tm.find())
								{
									Calendar ts = Calendar.getInstance();
									ts.setTime(base.getTime());
									int hour = normaliseHour(Integer.parseInt(tm.group(1)), base);
									ts.set(Calendar.HOUR_OF_DAY, hour);
									ts.set(Calendar.MINUTE, Integer.parseInt(tm.group(2)));
									j.last().time_end = ts.getTime();
								}
								end_of_journey = true;
								if (j.size()==1 && j.get(0).time_start == null)
									j.get(0).time_start = base.getTime();
								j.corrections();
								res.add(j);
							}
							break;
						}
						case 1:
						{
							String segment = tdlist.group(1);
							if (segment.indexOf("<br")!=-1)
								segment = segment.substring(0,segment.indexOf("<br"));
							if (segment.indexOf("<a")!=-1)
							{
								Matcher rep = strip_link.matcher(segment);
								rep.find();
								segment = rep.group(1);
							}
							if (segment.indexOf("Stop:")!=-1)
							{
								// FIXME: do something with the stop data
								segment = segment.substring(0, segment.indexOf("Stop:"));
							}
							js.loc_start = segment.replaceAll("[^A-Za-z ]+$","");
							if (end_of_journey)
							{
								js.loc_end = js.loc_start;
								js.loc_start = "";
								j.corrections();
								break;
							}
							if (debug)
								System.out.println("Type: " + js.type);
							switch (js.type)
							{
								case Walk:
								{
									Matcher w = walk_to.matcher(tdlist.group(1));
									w.find();
									String loc_end = w.group(1);
									if (loc_end.indexOf("Stop:")!=-1)
									{
										Route ro = new Route();
										ro.stop = loc_end.substring(loc_end.lastIndexOf(" ")).trim();
										js.routes.add(ro);
										loc_end = loc_end.substring(0, loc_end.indexOf("Stop:")).trim();
									}
									js.loc_end = loc_end;
									break;
								}
								case Tube:
								case DLR:
								{
									Matcher t = tube_to.matcher(tdlist.group(1));
									t.find();
									Matcher t2 = tube_direct.matcher(t.group(1));
									if (debug)
										System.out.println("Searching for tube from: "+t.group(1));
									while (t2.find())
									{
										Route ro = new Route();
										ro.thing = t2.group(1);
										ro.towards = t2.group(2);
										if (ro.towards.codePointAt(0) == 160) // non-breaking space
										{
											ro.towards = ro.towards.substring(1);
										}
										ro.stop = null;
										js.routes.add(ro);
									}
									break;
								}
								case Bus:
								{
									Matcher b;
									if (tdlist.group(1).indexOf("replacement")!=-1)
									{
										b = replacement_bus_to.matcher(tdlist.group(1));
										while (b.find())
										{
											Route ro = new Route();
											ro.thing = b.group(1);
											ro.stop = null;
											ro.towards = b.group(2);
											js.routes.add(ro);
										}
									}
									else
									{
										b = bus_to.matcher(tdlist.group(1));
										while (b.find())
										{
											Route ro = new Route();
											ro.thing = b.group(1);
											ro.stop = b.group(2);
											ro.towards = b.group(3);
											js.routes.add(ro);
										}
									}
									assert js.routes.size()>0;
									break;
								}
								case Rail:
								{
									Matcher ra = rail_to.matcher(tdlist.group(1));
									while (ra.find())
									{
										Route ro = new Route();
										if (ra.group(1).indexOf("<span")!=-1)
										{
											Matcher span = strip_span.matcher(ra.group(1));
											span.find();
											ro.thing = span.group(1);
										}
										else
											ro.thing = ra.group(1);
										ro.towards = ra.group(2);
										js.routes.add(ro);
									}
									assert js.routes.size()>0;
									break;
								}
								case River:
								{
									Matcher ba = boat_to.matcher(tdlist.group(1));
									while (ba.find())
									{
										Route ro = new Route();
										ro.stop = ba.group(1);
										js.routes.add(ro);
									}
									assert js.routes.size()>0;
									break;
								}
								case Tram:
								{
									Matcher ba = tram_to.matcher(tdlist.group(1));
									while (ba.find())
									{
										Route ro = new Route();
										ro.stop = ba.group(1).replace("\u00A0"," ").trim();
										ro.towards = ba.group(2);
										js.routes.add(ro);
									}
									assert js.routes.size()>0;
									break;
								}
								default:
									throw new ParseException(tdlist.group(1));
							}
							//System.out.println(js);
							break;
						}
						case 2:
						{
							if (tdlist.group(1).indexOf("Pay before you board") != -1)
							{
								if (debug)
									System.out.println("Skipping: "+ tdlist.group(1));
								tdlist.find();
							}
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
								else if (alts.group(1).equals("lift down"))
									js.impediments.add(Impediments.LiftDown);
								else if (alts.group(1).equals("escalator up"))
									js.impediments.add(Impediments.EscalatorUp);
								else if (alts.group(1).equals("escalator down"))
									js.impediments.add(Impediments.EscalatorDown);
								else if (alts.group(1).equals("ramp down"))
									js.impediments.add(Impediments.RampDown);
								else if (alts.group(1).equals("ramp up"))
									js.impediments.add(Impediments.RampUp);
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


