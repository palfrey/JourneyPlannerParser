package net.tevp.journeyplannerparser;

public class JourneyLocation
{
	protected LocationType type;
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

	public String toString()
	{
		return String.format("JourneyLocation <%s, '%s'>", type, data);
	}
}
