package no.nordicsemi.android.support.v18.scanner;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
    /* package */ final static String EXTRA_PENDING_INTENT = "no.nordicsemi.android.support.v18.EXTRA_PENDING_INTENT";
    /* package */ final static String EXTRA_FILTERS = "no.nordicsemi.android.support.v18.EXTRA_FILTERS";
    /* package */ final static String EXTRA_SETTINGS = "no.nordicsemi.android.support.v18.EXTRA_SETTINGS";
    /* package */ final static String EXTRA_START = "no.nordicsemi.android.support.v18.EXTRA_START";

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
            if (shouldStop) {
                stopSelf();
            }
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
        stopSelf();
    }

    @Override
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    public void onDestroy() {
        final BluetoothLeScannerCompat scannerCompat = BluetoothLeScannerCompat.getScanner();
        for (final ScanCallback callback : callbacks.values()) {
            scannerCompat.stopScan(callback);
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
        final ScanCallback callback = new PendingIntentExecutor(callbackIntent);
        synchronized (LOCK) {
            callbacks.put(callbackIntent, callback);
        }

        final BluetoothLeScannerCompat scannerCompat = BluetoothLeScannerCompat.getScanner();
        scannerCompat.startScanInternal(filters, settings, callback, handler);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    private void stopScan(@NonNull final PendingIntent callbackIntent) {
        ScanCallback callback;
        boolean shouldStop;
        synchronized (LOCK) {
            callback = callbacks.remove(callbackIntent);
            shouldStop = callbacks.isEmpty();
        }

        if (callback == null) {
            return;
        }

        final BluetoothLeScannerCompat scannerCompat = BluetoothLeScannerCompat.getScanner();
        scannerCompat.stopScan(callback);

        if (shouldStop) {
            stopSelf();
        }
    }

    private class PendingIntentExecutor extends ScanCallback {

        @NonNull private final PendingIntent callbackIntent;

        PendingIntentExecutor(@NonNull final PendingIntent callbackIntent) {
            this.callbackIntent = callbackIntent;
        }

        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
            try {
                final Intent extrasIntent = new Intent();
                extrasIntent.putExtra(BluetoothLeScannerCompat.EXTRA_CALLBACK_TYPE, callbackType);
                final ArrayList<ScanResult> results = new ArrayList<>(1);
                results.add(result);
                extrasIntent.putParcelableArrayListExtra(BluetoothLeScannerCompat.EXTRA_LIST_SCAN_RESULT, results);
                callbackIntent.send(ScannerService.this, 0, extrasIntent);
            } catch (final PendingIntent.CanceledException e) {
                // ignore
            }
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {
            try {
                final Intent extrasIntent = new Intent();
                extrasIntent.putExtra(BluetoothLeScannerCompat.EXTRA_CALLBACK_TYPE,
                        ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
                extrasIntent.putParcelableArrayListExtra(BluetoothLeScannerCompat.EXTRA_LIST_SCAN_RESULT,
                        new ArrayList<Parcelable>(results));
                callbackIntent.send(ScannerService.this, 0, extrasIntent);
            } catch (final PendingIntent.CanceledException e) {
                // ignore
            }
        }

        @Override
        public void onScanFailed(final int errorCode) {
            try {
                final Intent extrasIntent = new Intent();
                extrasIntent.putExtra(BluetoothLeScannerCompat.EXTRA_ERROR_CODE, errorCode);
                callbackIntent.send(ScannerService.this, 0, extrasIntent);
            } catch (final PendingIntent.CanceledException e) {
                // ignore
            }
        }
    }
}
