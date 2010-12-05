package net.tevp.journeyplannerparser;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Vector;

public class ParcelableJourneyQuery extends JourneyQuery implements Parcelable
{
	public ParcelableJourneyQuery(JourneyQuery jq)
	{
		start = jq.start;
		end = jq.end;
		params = jq.params;
	}
	
	@Override
	public void writeToParcel (Parcel dest, int flags)
	{
		dest.writeParcelable(new ParcelableJourneyLocation(start), flags);
		dest.writeParcelable(new ParcelableJourneyLocation(end), flags);
		dest.writeParcelable(new ParcelableJourneyParameters(params), flags);
	}

	@Override
	public int describeContents() { return 0;}

    public static final Parcelable.Creator<ParcelableJourneyQuery> CREATOR = new Parcelable.Creator<ParcelableJourneyQuery>() {
        public ParcelableJourneyQuery createFromParcel(Parcel in) {
            JourneyQuery jq = new JourneyQuery();
			ClassLoader cl = JourneyQuery.class.getClassLoader();
			jq.start = in.readParcelable(cl);
			jq.end = in.readParcelable(cl);
			jq.params = in.readParcelable(cl);
			return new ParcelableJourneyQuery(jq);
        }

        public ParcelableJourneyQuery[] newArray(int size) {
            return new ParcelableJourneyQuery[size];
        }
    };
}
