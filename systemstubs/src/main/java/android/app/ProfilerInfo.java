package android.app;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ProfilerInfo implements Parcelable {
    protected ProfilerInfo(Parcel in) {
        //Stub
    }

    public static final Creator<ProfilerInfo> CREATOR = new Creator<ProfilerInfo>() {
        @Override
        public ProfilerInfo createFromParcel(Parcel in) {
            return new ProfilerInfo(in);
        }

        @Override
        public ProfilerInfo[] newArray(int size) {
            return new ProfilerInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
    }
}
