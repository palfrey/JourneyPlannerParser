package net.tevp.JourneyPlannerParser;

public class JourneyLocation
{
	private LocationType type;
	public String data;
	
	JourneyLocation(LocationType lt, String _data)
	{
		type = lt;
		data = _data;
	}

	public String getTFLName()
	{
		return type.getTFLName();
	}
}


