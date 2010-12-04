package net.tevp.journeyplannerparser;

import java.util.*;

public class JourneyParameters
{
	public Date when;
	public Speed speed;
	public TimeType timeType;
	public RouteType routeType;
	public JourneyLocation via;

	public boolean useRail, useDLR, useTube, useTram, useBus, useCoach, useRiver;

	public JourneyParameters()
	{
		when = new Date();
		speed = Speed.normal;
		timeType = TimeType.Depart;
		routeType = RouteType.LeastTime;
		via = null;
		useRail = useDLR = useTube = useTram = useBus = useCoach = useRiver = true;
	}
}
