package se.thefarm.greger.pokeflutego.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.util.List;

import se.thefarm.greger.pokeflutego.R;

public class WakeService extends Service
{
    private Handler handler = new Handler();
    private static final String TAG = "WakeService";
    private Runnable runnable;
    public static boolean isServiceRunning = false;
    private boolean run = true;
    PowerManager.WakeLock wl = null;
    private BroadcastReceiver receiver;

    private static final String POKEMON_PACKAGE = "com.nianticlabs.pokemongo";

    public WakeService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        isServiceRunning = true;
        IntentFilter lockFilter = new IntentFilter();
        lockFilter.addAction(Intent.ACTION_SCREEN_ON);
        lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (intent != null && intent.getAction() != null)
                {
                    if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
                    {
                        // Screen is on but not unlocked (if any locking mechanism present)
                        Log.d(TAG, "onReceive: Screen on");
                        run = true;
                        handler.postDelayed(runnable, 5000);
                    }
                    else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
                    {
                        // Screen is locked
                        Log.d(TAG, "onReceive: Screen off");
                        run = false;
                        controlWakeLock(false);
                    }
                }
            }
        };
        registerReceiver(receiver, lockFilter);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "prutt");
        AndroidProcesses.setLoggingEnabled(false);

        if(pm.isScreenOn())
            run = true;

        runnable = new Runnable()
        {
            public void run()
            {
                if (!run)
                    return;

                controlWakeLock(isPokemonGoForeground());

                if (run)
                    handler.postDelayed(runnable, 8000);
            }
        };

        handler.postDelayed(runnable, 8000);
    }

    protected void controlWakeLock(boolean keepScreenOn)
    {
        if (keepScreenOn && !wl.isHeld())
        {
            Notification not = new NotificationCompat.Builder(this)
                    .setOngoing(true)
                    .setContentTitle("PokefluteGo")
                    .setContentText("PokefluteGo is keeping the screen awake")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, not);

            Log.d(TAG, "controlWakeLock: Enabling wakelock");
            wl.acquire();
        }
        else if (!keepScreenOn && wl.isHeld())
        {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);

            Log.d(TAG, "controlWakeLock: Disabling wakelock");
            wl.release();
        }
    }

    protected boolean isPokemonGoForeground()
    {
        List<AndroidAppProcess> proclist = AndroidProcesses.getRunningForegroundApps(this);
        for (AndroidAppProcess proc : proclist)
        {
            if (proc.getPackageName().equals(POKEMON_PACKAGE))
                return true;
        }
        return false;
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "onDestroy: Service destroyed");
        isServiceRunning = false;
        run = false;

        if(wl.isHeld())
            wl.release();
        unregisterReceiver(receiver);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);

        super.onDestroy();
    }
}
