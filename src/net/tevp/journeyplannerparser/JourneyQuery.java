package net.tevp.journeyplannerparser;

import java.util.Vector;
import android.os.Parcel;
import android.os.Parcelable;

public class JourneyQuery implements Parcelable
{
	JourneyLocation start, end;
	public JourneyParameters params;
	
	@Override
	public void writeToParcel (Parcel dest, int flags)
	{
		dest.writeParcelable(start, flags);
		dest.writeParcelable(end, flags);
		dest.writeParcelable(params, flags);
	}

	@Override
	public int describeContents() { return 0;}

    public static final Parcelable.Creator<JourneyQuery> CREATOR = new Parcelable.Creator<JourneyQuery>() {
        public JourneyQuery createFromParcel(Parcel in) {
            JourneyQuery jq = new JourneyQuery();
			ClassLoader cl = JourneyLocation.class.getClassLoader();
			jq.start = in.readParcelable(cl);
			jq.end = in.readParcelable(cl);
			jq.params = in.readParcelable(cl);
			return jq;
        }

        public JourneyQuery[] newArray(int size) {
            return new JourneyQuery[size];
        }
    };
}
