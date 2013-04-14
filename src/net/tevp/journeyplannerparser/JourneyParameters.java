package net.tevp.journeyplannerparser;

import java.util.*;
import android.os.Parcel;
import android.os.Parcelable;

public class JourneyParameters implements Parcelable
{
	public Date when;
	public Speed speed;
	public TimeType timeType;
	public RouteType routeType;
	public JourneyLocation via;

	public boolean useRail, useDLR, useTube, useTram, useBus, useCoach, useRiver;

	public JourneyParameters()
	{
		when = new Date();
		speed = Speed.normal;
		timeType = TimeType.Depart;
		routeType = RouteType.LeastTime;
		via = null;
		useRail = useDLR = useTube = useTram = useBus = useCoach = useRiver = true;
	}

	@Override
	public void writeToParcel (Parcel dest, int flags)
	{
		dest.writeLong(when.getTime());
		dest.writeString(speed.name());
		dest.writeString(timeType.name());
		dest.writeString(routeType.name());
		dest.writeParcelable(via, flags);
		dest.writeBooleanArray(new boolean[] {useRail, useDLR, useTube, useTram, useBus, useCoach, useRiver});
	}

	@Override
	public int describeContents() { return 0;}

	public static final Parcelable.Creator<JourneyParameters> CREATOR = new Parcelable.Creator<JourneyParameters>() {
		public JourneyParameters createFromParcel(Parcel in) {
			JourneyParameters jp = new JourneyParameters();
			jp.when = new Date(in.readLong());
			jp.speed = Enum.valueOf(Speed.class, in.readString());
			jp.timeType = Enum.valueOf(TimeType.class, in.readString());
			jp.routeType = Enum.valueOf(RouteType.class, in.readString());
			jp.via = in.readParcelable(null);

			boolean[] pts = new boolean[7];
			in.readBooleanArray(pts);
			jp.useRail = pts[0];
			jp.useDLR = pts[1];
			jp.useTube = pts[2];
			jp.useTram = pts[3];
			jp.useBus = pts[4];
			jp.useCoach = pts[5];
			jp.useRiver = pts[6];
			return jp;
		}

		public JourneyParameters[] newArray(int size) {
			return new JourneyParameters[size];
		}
	};
}
