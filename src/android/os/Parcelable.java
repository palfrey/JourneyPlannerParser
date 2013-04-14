// stub to allow command line running

package android.os;

public interface Parcelable {
	void writeToParcel (Parcel dest, int flags);
	int describeContents();

	public class Creator<T> {
	}
}
