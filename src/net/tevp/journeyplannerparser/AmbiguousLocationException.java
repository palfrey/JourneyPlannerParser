package net.tevp.journeyplannerparser;

import java.util.Vector;

public class AmbiguousLocationException extends DodgyLocationException
{
	public Vector<String> options;
	AmbiguousLocationException() {super("Ambiguous location specified!");}
}
