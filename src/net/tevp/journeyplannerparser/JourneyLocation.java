package net.tevp.journeyplannerparser;

import android.os.Parcel;
import android.os.Parcelable;

public class JourneyLocation implements Parcelable
{
	protected LocationType type;
	public String data;
	
	JourneyLocation(LocationType lt, String _data)
	{
		type = lt;
		data = _data;
	}

	JourneyLocation()
	{
	}

	public String getTFLName()
	{
		return type.getTFLName();
	}

	public String toString()
	{
		return String.format("JourneyLocation <%s, '%s'>", type, data);
	}

	@Override
	public void writeToParcel (Parcel dest, int flags)
	{
		if (type == null)
			dest.writeString("");
		else
			dest.writeString(type.name());
		dest.writeString(data);
	}

	@Override
	public int describeContents() { return 0;}

	public static final Parcelable.Creator<JourneyLocation> CREATOR = new Parcelable.Creator<JourneyLocation>() {
        public JourneyLocation createFromParcel(Parcel in) {
			String lt = in.readString();
			String data = in.readString();
			if (lt.length() == 0)
            	return new JourneyLocation(null, null);
            return new JourneyLocation(Enum.valueOf(LocationType.class, lt), data);
        }

        public JourneyLocation[] newArray(int size) {
            return new JourneyLocation[size];
        }
    };
}
