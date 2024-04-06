package com.kieronquinn.app.smartspacer.plugin.controls.service;

interface IControlsSuiService {

    boolean ping() = 1;
    boolean isCompatible() = 2;

    int bindServicePriviliged(
        in IBinder applicationThread,
        in IBinder activityToken,
        in IBinder serviceConnection,
        in Intent intent,
        int flags
    ) = 3;

    void unbindService(int token) = 4;

    void startActivity(in Intent intent) = 5;

    void startPendingIntent(in PendingIntent pendingIntent) = 6;

    void forceStop(in String packageName, int userId) = 7;

    int stopService(in IBinder applicationThread, in Intent intent) = 8;

    void showPowerMenu() = 9;

    void destroy() = 16777114;

}