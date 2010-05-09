package net.tevp.JourneyPlannerParser;
public enum RouteType
{
	LeastTime("LEASTTIME"),
	LeastChanges("LEASTINTERCHANGE"),
	LeastWalking("LEASTWALKING");

	private String details;

	private RouteType(String details)
	{
		this.details = details;
	}

	public String getDetails()
	{
		return details;
	}
}
