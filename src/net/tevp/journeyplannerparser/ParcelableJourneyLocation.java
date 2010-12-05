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
		super(null, "");
	}
	
	@Override
	public void writeToParcel (Parcel dest, int flags)
	{
		if (type == null)
			dest.writeString("");
		else
		{
			dest.writeString(type.name());
			dest.writeString(data);
		}
	}

	@Override
	public int describeContents() { return 0;}

	public static final Parcelable.Creator<ParcelableJourneyLocation> CREATOR = new Parcelable.Creator<ParcelableJourneyLocation>() {
        public ParcelableJourneyLocation createFromParcel(Parcel in) {
			String type = in.readString();
			if (type.length() == 0)
				return null; // assume an all-null object
            return new ParcelableJourneyLocation(Enum.valueOf(LocationType.class, type), in.readString());
        }

        public ParcelableJourneyLocation[] newArray(int size) {
            return new ParcelableJourneyLocation[size];
        }
    };
}
