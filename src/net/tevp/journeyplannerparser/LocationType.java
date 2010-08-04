package net.tevp.journeyplannerparser;
public enum LocationType
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

	public JourneyLocation create(String data)
	{
		return new JourneyLocation(this, data);
	}
}


