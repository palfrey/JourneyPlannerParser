package net.tevp.JourneyPlannerParser;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;

class BufferedURLConnection
{
	boolean buffered;
	String inputData, filename;
	URLConnection uc = null;
	
	public Map<String, List<String>> headers;
	public String outputData;
	
	@SuppressWarnings("unchecked") 	
	public BufferedURLConnection(String url, String data) throws IOException
	{
		inputData = data;
		filename = String.format("%d-%d.cache", url.hashCode(), inputData.hashCode());
		if (new File(filename).exists())
		{
			ObjectInputStream rd = new ObjectInputStream(new FileInputStream(filename));
			try
			{
				headers = (Map<String, List<String>>)rd.readObject();
				outputData = (String)rd.readObject();
			}
			catch (ClassNotFoundException e)
			{
				/* really shouldn't happen, but re-throw just in case */
				throw new IOException("ClassNotFoundException! That's pretty damn weird: "+e.getMessage());
			}
			buffered = true;
			return;
		}
		uc = new URL(url).openConnection();
		if (inputData != "")
		{
			uc.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
			wr.write(inputData);
			wr.flush();
		}
		buffered = false;
		headers = uc.getHeaderFields();

		outputData = "";
		InputStream is = uc.getInputStream();
		while (true)
		{
			byte[] buffer = new byte[1024];
			int bytes = is.read(buffer, 0, 1024);
			if (bytes == -1)
				break;
			outputData += new String(buffer, 0, bytes);
		}

		ObjectOutputStream dumper = new ObjectOutputStream(new FileOutputStream(filename));
		dumper.writeObject(headers);
		dumper.writeObject(outputData);
		dumper.close();
	}

	public BufferedURLConnection(String url) throws IOException
	{
		this(url, "");
	}
}


