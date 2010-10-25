/*
 * Class to convert latitude/longitude to British Grid.
 * 
 * As far as I know, this will only yield exact results if
 * the input latitude and longitude are to the OSGB datum.
 * If lat/lon to a WGS84 datum are used, inaccuracies of 
 * up to 100 metres can be incurred.
 * I am not 100% sure about this though since I copied this
 * code from Chuck Gantz's C++ code which you can find at
 * http://www.bangor.ac.uk/is/iss025/osgbfaq.htm.
 *
 * This Java class written by Frederik Ramm <frederik@remote.org>,
 * April 2001, free to use for anybody & anything, and no
 * warranty of any kind.
 * 
 * @author Frederik Ramm
 * @version 1.0
 */
package net.tevp.journeyplannerparser;

public class GridConverter
{

	private final static double lonOrigin = -2;
	private final static double latOrigin = 49;
	private final static double lonOriginRad = Math.toRadians (lonOrigin);
	private final static double latOriginRad = Math.toRadians (latOrigin);

	private final static double majorAxis = 6377563.396;
	private final static double minorAxis = 6356256.91;
	private final static double eccSquared = (majorAxis * majorAxis - minorAxis * minorAxis) / (majorAxis * majorAxis);
	private final static double k0 = 0.9996012717;

	private final static double m0 = calculateM (latOriginRad, lonOriginRad);

	private final static double eccPrimeSquared = (eccSquared)/(1-eccSquared);

	/**
	 * Returns full easting and northing for the given coordinates.
	 * (That's basically the x and y distance from some point near the Scilly isles,
	 * and always two positive values).
	 *
	 * param lat latitude 
	 * param lon longitude (negative = west)
	 * @return array with two elements for easting and northing
	 */
	public static double[] degreeToGrid (double lat, double lon)
	{

		double latRad = Math.toRadians (lat);
		double lonRad = Math.toRadians (lon);
		double n = majorAxis / Math.sqrt (1 - eccSquared * (pow(Math.sin(latRad),2)));
		double t = pow (Math.tan (latRad), 2);
		double c = eccPrimeSquared * pow (Math.cos (latRad),2);
		double a = Math.cos (latRad) * (lonRad - lonOriginRad);
		double m = calculateM (latRad, lonRad);
		
		double easting = k0 * n * (a + (1 - t + c) * pow (a, 3) / 6
			+ (5 - 18 * t + t * 2 + 72 * c - 58 * eccPrimeSquared) * pow (a, 5) / 120)
			+ 400000;
		double northing = k0 * (m - m0 + n * Math.tan (latRad) 
			* (a * a / 2 + (5 - t + 9 * c + 4 * c * 2) * pow (a, 4) / 24 
				+ (61 - 58 * t + t * t + 600 * c - 330 * eccPrimeSquared) * pow (a, 6) / 720))
			- 100000;

		return new double[] { easting, northing };
	}
		
	public static Object[] degreeToSquare (double lat, double lon)
	{
		double[] full = degreeToGrid (lat, lon);
		int easting = (int) (full[0] + 0.5);
		int northing = (int) (full[1] + 0.5);
		int posx = easting / 500000;
		int posy = northing / 500000;
		String gridSquares = "VWXYZQRSTULMNOPFGHJKABCDE";
		StringBuffer returnSquare = new StringBuffer ();
		
		returnSquare.append (gridSquares.charAt (posx + posy * 5 + 7));
		posx = (easting % 500000) / 100000;
		posy = (northing % 500000) / 100000;
		returnSquare.append (gridSquares.charAt (posx + posy * 5));
		
		return (new Object[] {
			returnSquare.toString (),
			new Integer (easting % 100000),
			new Integer (northing % 100000)
		});
	}

	/**
	 * "m" is for "magic". Or not?
 	 */
	private static double calculateM (double latRad, double lonRad) {
		return 
			majorAxis * (
				(1 - eccSquared/4 - 3*pow (eccSquared, 2)/64 - 5*pow (eccSquared, 3)/256) * latRad 
				- (3*eccSquared/8 + 3*pow (eccSquared, 2)/32 + 45*pow (eccSquared, 3)/1024) * Math.sin(2 * latRad) 
				+ (15*pow (eccSquared, 2)/256 + 45*pow (eccSquared, 3)/1024) * Math.sin(4*latRad) 
				- (35*pow (eccSquared, 3)/3072) * Math.sin (6*latRad)
				);
	}

	private static double pow (double val, int exp)
	{
		if (exp == 1) return val;
		return val * pow (val, exp - 1);
	}

	/**
	 * For testing. Call the class from the command line with two parameters,
	 * first latitude, then longitude.
	 */
	public static void main (String arg[])
	{
		if (arg.length != 2)
		{
			System.out.println("Usage: java GridConverter lat lon");
			System.out.println("  where lat, lon = latitude and longitude to convert");
			System.out.println("  (use negative longitude for west)");
			System.exit (1);
		}

		double lat = Double.parseDouble (arg[0]);
		double lon = Double.parseDouble (arg[1]);

		double[] grid = degreeToGrid (lat, lon);
		
		System.out.println ("latitude:          " + lat);
		System.out.println ("longitude:         " + lon);
		System.out.println ();
		System.out.println ("full easting:      " + grid[0]);
		System.out.println ("full northing:     " + grid[1]);
		System.out.println ();

		Object[] square = degreeToSquare (lat, lon);
		System.out.println ("square designator: " + square[0]);
		System.out.println ("square easting:    " + square[1]);
		System.out.println ("square northing:   " + square[2]);

	}

}
