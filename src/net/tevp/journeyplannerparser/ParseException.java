package net.tevp.journeyplannerparser;

public class ParseException extends Exception
{
	ParseException(String msg) {super(msg);}
	ParseException(String msg, Throwable t) {super(msg);initCause(t);}
}
