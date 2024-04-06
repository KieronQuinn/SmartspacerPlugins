package android.app;

import android.app.IApplicationThread;
import android.app.ProfilerInfo;

interface IActivityTaskManager {
    int startActivity(in IApplicationThread caller, in String callingPackage,
                in String callingFeatureId, in Intent intent, in String resolvedType,
                in IBinder resultTo, in String resultWho, int requestCode,
                int flags, in ProfilerInfo profilerInfo, in Bundle options);
}