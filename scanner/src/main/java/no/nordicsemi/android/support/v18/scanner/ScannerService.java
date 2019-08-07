package no.nordicsemi.android.support.v18.scanner;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

/**
 * A service that will emulate
 * {@link android.bluetooth.le.BluetoothLeScanner#startScan(List, android.bluetooth.le.ScanSettings, PendingIntent)}
 * on Android versions before Oreo.
 * <p>
 * To start the service call
 * {@link BluetoothLeScannerCompat#startScan(List, ScanSettings, Context, PendingIntent)}.
 * It will be stopped automatically when the last scan has been stopped using
 * {@link BluetoothLeScannerCompat#stopScan(Context, PendingIntent)}.
 * <p>
 * As this service will run and scan in background it is recommended to use
 * {@link ScanSettings#SCAN_MODE_LOW_POWER} mode and set filter to lower power consumption.
 */
public class ScannerService extends Service {
    private static final String TAG = "ScannerService";

    /* package */ static final String EXTRA_PENDING_INTENT = "no.nordicsemi.android.support.v18.EXTRA_PENDING_INTENT";
    /* package */ static final String EXTRA_FILTERS = "no.nordicsemi.android.support.v18.EXTRA_FILTERS";
    /* package */ static final String EXTRA_SETTINGS = "no.nordicsemi.android.support.v18.EXTRA_SETTINGS";
    /* package */ static final String EXTRA_START = "no.nordicsemi.android.support.v18.EXTRA_START";

    @NonNull private final Object LOCK = new Object();

    private HashMap<PendingIntent, ScanCallback> callbacks;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        callbacks = new HashMap<>();
        handler = new Handler();
    }

    @Override
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final PendingIntent callbackIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT);
        final boolean start = intent.getBooleanExtra(EXTRA_START, false);
        final boolean stop = !start;

        if (callbackIntent == null) {
            boolean shouldStop;
            synchronized (LOCK) {
                shouldStop = callbacks.isEmpty();
            }
            if (shouldStop)
                stopSelf();
            return START_NOT_STICKY;
        }

        boolean knownCallback;
        synchronized (LOCK) {
            knownCallback = callbacks.containsKey(callbackIntent);
        }

        if (start && !knownCallback) {
            final ArrayList<ScanFilter> filters = intent.getParcelableArrayListExtra(EXTRA_FILTERS);
            final ScanSettings settings = intent.getParcelableExtra(EXTRA_SETTINGS);
            startScan(filters != null ? filters : Collections.<ScanFilter>emptyList(),
                    settings != null ? settings : new ScanSettings.Builder().build(),
                    callbackIntent);
        } else if (stop && knownCallback) {
            stopScan(callbackIntent);
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        // Forbid binding
        return null;
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // Stopping self here would cause the service to be killed when user removes the task
        // from Recents. This is not the behavior found in Oreo+.
        // Related issue: https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library/issues/45

        // Even with this line removed, the service will stop receiving devices when the phone
        // enters Doze mode.
        // Find out more here: https://developer.android.com/training/monitoring-device-state/doze-standby

        // stopSelf();
    }

    @Override
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    public void onDestroy() {
        final BluetoothLeScannerCompat scannerCompat = BluetoothLeScannerCompat.getScanner();
        for (final ScanCallback callback : callbacks.values()) {
            try {
                scannerCompat.stopScan(callback);
            } catch (final Exception e) {
                // Ignore
            }
        }
        callbacks.clear();
        callbacks = null;
        handler = null;
        super.onDestroy();
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    private void startScan(@NonNull final List<ScanFilter> filters,
                           @NonNull final ScanSettings settings,
                           @NonNull final PendingIntent callbackIntent) {
        final PendingIntentExecutor executor =
                new PendingIntentExecutor(callbackIntent, settings, this);
        synchronized (LOCK) {
            callbacks.put(callbackIntent, executor);
        }

        try {
            final BluetoothLeScannerCompat scannerCompat = BluetoothLeScannerCompat.getScanner();
            scannerCompat.startScanInternal(filters, settings, executor, handler);
        } catch (final Exception e) {
            Log.w(TAG, "Starting scanning failed", e);
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    private void stopScan(@NonNull final PendingIntent callbackIntent) {
        ScanCallback callback;
        boolean shouldStop;
        synchronized (LOCK) {
            callback = callbacks.remove(callbackIntent);
            shouldStop = callbacks.isEmpty();
        }
        if (callback == null)
            return;

        try {
            final BluetoothLeScannerCompat scannerCompat = BluetoothLeScannerCompat.getScanner();
            scannerCompat.stopScan(callback);
        } catch (final Exception e) {
            Log.w(TAG, "Stopping scanning failed", e);
        }

        if (shouldStop)
            stopSelf();
    }
}
