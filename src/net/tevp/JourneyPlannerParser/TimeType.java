package net.tevp.JourneyPlannerParser;
public enum TimeType
{
	Depart("dep"),
	Arrive("arr");

	private String details;

	private TimeType(String details)
	{
		this.details = details;
	}

	public String getDetails()
	{
		return details;
	}
}

