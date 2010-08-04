package net.tevp.journeyplannerparser;

import java.util.Vector;

public class JourneyQuery
{
	JourneyLocation start, end;
	JourneyParameters params;
	JourneyPlannerParser jpp;

	public Vector<Journey> run() throws ParseException
	{
		return jpp.runAsyncJourney(this);
	}
}
