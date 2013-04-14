package net.tevp.journeyplannerparser;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.*;

public class ParcelableJourneyParameters extends JourneyParameters implements Parcelable
{
	ParcelableJourneyParameters(JourneyParameters jp)
	{
		when = jp.when;
		speed = jp.speed;
		timeType = jp.timeType;
		routeType = jp.routeType;
		via = jp.via;
		useRail = jp.useRail;
		useDLR = jp.useDLR;
		useTube = jp.useTube;
		useTram = jp.useTram;
		useBus = jp.useBus;
		useCoach = jp.useCoach;
		useRiver = jp.useRiver;
	}

	@Override
	public void writeToParcel (Parcel dest, int flags)
	{
		dest.writeLong(when.getTime());
		dest.writeString(speed.name());
		dest.writeString(timeType.name());
		dest.writeString(routeType.name());
		if (via == null)
			dest.writeParcelable(new ParcelableJourneyLocation(), flags);
		else
			dest.writeParcelable(new ParcelableJourneyLocation(via), flags);
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
