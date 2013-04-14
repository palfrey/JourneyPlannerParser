package net.tevp.journeyplannerparser;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableJourneyLocation extends JourneyLocation implements Parcelable
{
	ParcelableJourneyLocation(LocationType lt, String _data)
	{
		super(lt, _data);
	}

	ParcelableJourneyLocation(JourneyLocation jl)
	{
		super(jl.type, jl.data);
	}

	ParcelableJourneyLocation()
	{
		super(null, null);
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
