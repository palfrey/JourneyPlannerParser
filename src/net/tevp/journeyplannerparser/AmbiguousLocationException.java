package net.tevp.journeyplannerparser;

import java.util.Vector;

public class AmbiguousLocationException extends ParseException
{
	public Vector<String> options;
	public JourneyLocation original;
	AmbiguousLocationException() {super("Ambiguous location specified!");}
}
