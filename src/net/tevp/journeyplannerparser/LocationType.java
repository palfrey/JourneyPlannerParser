package net.tevp.journeyplannerparser;
public enum LocationType
{
	Postcode("locator"),
	Stop("stop"),
	Address("address"),
	PlaceOfInterest("poi"),
	Coordinates("coord");

	private String tflname;

	private LocationType(String tflname)
	{
		this.tflname = tflname;
	}

	public String getTFLName()
	{
		return tflname;
	}

	public JourneyLocation create(String data)
	{
		return new JourneyLocation(this, data);
	}

	static public JourneyLocation createCoordinate(double lat, double lon)
	{
		double[] grid = GridConverter.degreeToGrid (lat, lon);
		return new JourneyLocation(LocationType.Coordinates, String.format("%6d:%6d:TFLV", (int)grid[0], (int)(1000000-grid[1])));
	}
}
