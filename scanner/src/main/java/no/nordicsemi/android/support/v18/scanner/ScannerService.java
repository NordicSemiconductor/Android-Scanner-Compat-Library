package no.nordicsemi.android.support.v18.scanner;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScannerService extends Service {
    /* package */ final static String EXTRA_PENDING_INTENT = "no.nordicsemi.android.support.v18.EXTRA_PENDING_INTENT";
    /* package */ final static String EXTRA_FILTERS = "no.nordicsemi.android.support.v18.EXTRA_FILTERS";
    /* package */ final static String EXTRA_SETTINGS = "no.nordicsemi.android.support.v18.EXTRA_SETTINGS";
    /* package */ final static String EXTRA_START = "no.nordicsemi.android.support.v18.EXTRA_START";

    private HashMap<PendingIntent, ScanCallback> mCallbacks;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mCallbacks = new HashMap<>();
        mHandler = new Handler();
    }

    @Override
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final PendingIntent callbackIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT);
        final boolean start = intent.getBooleanExtra(EXTRA_START, false);

        if (start && callbackIntent != null && !mCallbacks.containsKey(callbackIntent)) {
            final ArrayList<ScanFilter> filters = intent.getParcelableArrayListExtra(EXTRA_FILTERS);
            final ScanSettings settings = intent.getParcelableExtra(EXTRA_SETTINGS);
            startScan(filters, settings, callbackIntent);
        } else if (!start && callbackIntent != null && mCallbacks.containsKey(callbackIntent)) {
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
        for (final ScanCallback callback : mCallbacks.values()) {
            scannerCompat.stopScan(callback);
        }
        mCallbacks.clear();
        mCallbacks = null;
        mHandler = null;
        super.onDestroy();
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    private void startScan(@Nullable final List<ScanFilter> filters,
                           @NonNull final ScanSettings settings,
                           @NonNull final PendingIntent callbackIntent) {
        final ScanCallback callback = new ScanCallback() {
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
        };
        mCallbacks.put(callbackIntent, callback);

        final BluetoothLeScannerCompat scannerCompat = BluetoothLeScannerCompat.getScanner();
        scannerCompat.startScanInternal(filters, settings, callback, mHandler);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    private void stopScan(@NonNull final PendingIntent callbackIntent) {
        final ScanCallback callback = mCallbacks.remove(callbackIntent);

        final BluetoothLeScannerCompat scannerCompat = BluetoothLeScannerCompat.getScanner();
        scannerCompat.stopScan(callback);

        if (mCallbacks.isEmpty()) {
            stopSelf();
        }
    }
}
