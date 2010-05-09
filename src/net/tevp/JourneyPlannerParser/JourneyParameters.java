package net.tevp.JourneyPlannerParser;

import java.util.*;

public class JourneyParameters
{
	public Date when;
	public Speed speed;
	public TimeType timeType;
	public RouteType routeType;

	public JourneyParameters()
	{
		when = new Date();
		speed = Speed.normal;
		timeType = TimeType.Depart;
		routeType = RouteType.LeastTime;
	}
}


