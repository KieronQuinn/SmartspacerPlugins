package android.app;

import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.os.IBinder;

public interface IActivityManager extends android.os.IInterface {

    abstract class Stub extends android.os.Binder implements android.app.IServiceConnection {
        public static IActivityManager asInterface(android.os.IBinder obj) {
            throw new RuntimeException("Stub!");
        }
    }

    int bindIsolatedService(
            IApplicationThread caller,
            IBinder token,
            Intent service,
            String resolvedType,
            IServiceConnection connection,
            int flags,
            String instanceName,
            String callingPackage,
            int userId);

    //Android 13
    int bindServiceInstance(
            IApplicationThread caller,
            IBinder token,
            Intent service,
            String resolvedType,
            IServiceConnection connection,
            int flags,
            String instanceName,
            String callingPackage,
            int userId);

    //Android 14
    int bindServiceInstance(
            IApplicationThread caller,
            IBinder token,
            Intent service,
            String resolvedType,
            IServiceConnection connection,
            long flags,
            String instanceName,
            String callingPackage,
            int userId);

    boolean unbindService(IServiceConnection connection);
    int stopService(IApplicationThread caller, Intent service, String resolvedType, int userId);
    void forceStopPackage(String packageName, int userId);

}