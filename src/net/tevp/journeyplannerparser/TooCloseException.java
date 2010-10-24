package net.tevp.journeyplannerparser;

import java.util.Vector;

public class TooCloseException extends DodgyLocationException
{
	TooCloseException(JourneyLocation loc) {super("Start and destination are too close to each other");original=loc;}
}
