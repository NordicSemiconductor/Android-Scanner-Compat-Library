package no.nordicsemi.android.support.v18.scanner;

import android.app.PendingIntent;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

public class PendingIntentReceiver extends BroadcastReceiver {
    public static final String ACTION = "no.nordicsemi.android.support.v18.ACTION_FOUND";
    public static final String EXTRA_PENDING_INTENT = "no.nordicsemi.android.support.v18.EXTRA_PENDING_INTENT";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(final Context context, final Intent intent) {
        // Ensure we are ok
        if (context == null || intent == null)
            return;

        // Ensure that the PendingIntent was added to the Intent
        final PendingIntent callingIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT);
        if (callingIntent == null)
            return;

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final BluetoothLeScannerImplOreo scannerImpl = (BluetoothLeScannerImplOreo) scanner;

        // Ensure the wrapper was registered
        final BluetoothLeScannerImplOreo.ScanCallbackWrapperOreo wrapper = scannerImpl.getWrapper(callingIntent);
        if (wrapper == null)
            return;

        // Set the context. Broadcast Receiver may be executed with different contexts.
        wrapper.executor.setContext(context);

        final List<android.bluetooth.le.ScanResult> nativeScanResults =
                intent.getParcelableArrayListExtra(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT);
        if (nativeScanResults != null) {
            final List<ScanResult> results = scannerImpl.fromNativeScanResults(nativeScanResults);

            if (wrapper.scanSettings.getReportDelayMillis() > 0) {
                wrapper.handleScanResults(results);
            } else {
                final int callbackType = intent.getIntExtra(BluetoothLeScanner.EXTRA_CALLBACK_TYPE,
                        ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
                if (!results.isEmpty()) {
                    wrapper.handleScanResult(callbackType, results.get(0));
                }
            }
        }

        final int errorCode = intent.getIntExtra(BluetoothLeScanner.EXTRA_ERROR_CODE, 0);
        if (errorCode != 0) {
            wrapper.handleScanError(errorCode);
        }
    }
}
