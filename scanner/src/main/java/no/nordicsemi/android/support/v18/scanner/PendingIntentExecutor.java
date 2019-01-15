package no.nordicsemi.android.support.v18.scanner;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A ScanCallback that will send given {@link PendingIntent} when callback's method is called.
 * The context must be set using {@link #setContext(Context)} before calling any method.
 */
/* package */ class PendingIntentExecutor extends ScanCallback {

	@NonNull
	private final PendingIntent callbackIntent;

	@Nullable private Context context;

	PendingIntentExecutor(@NonNull final PendingIntent callbackIntent) {
		this.callbackIntent = callbackIntent;
	}

    /**
     * Context has to be set before any of {@link ScanCallback} methods is called.
     * The context is required to send the pending intent.
     *
     * @param context The service or broadcast receiver's context.
     */
	/* package */ void setContext(@NonNull final Context context) {
		this.context = context;
	}

	@Override
	public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
        final Context context = this.context;
        if (context == null)
            return;

		try {
			final Intent extrasIntent = new Intent();
			extrasIntent.putExtra(BluetoothLeScannerCompat.EXTRA_CALLBACK_TYPE, callbackType);
			final ArrayList<ScanResult> results = new ArrayList<>(1);
			results.add(result);
			extrasIntent.putParcelableArrayListExtra(BluetoothLeScannerCompat.EXTRA_LIST_SCAN_RESULT, results);
			callbackIntent.send(context, 0, extrasIntent);
		} catch (final PendingIntent.CanceledException e) {
			// ignore
		}
	}

	@Override
	public void onBatchScanResults(@NonNull final List<ScanResult> results) {
        final Context context = this.context;
        if (context == null)
            return;

		try {
			final Intent extrasIntent = new Intent();
			extrasIntent.putExtra(BluetoothLeScannerCompat.EXTRA_CALLBACK_TYPE,
					ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
			extrasIntent.putParcelableArrayListExtra(BluetoothLeScannerCompat.EXTRA_LIST_SCAN_RESULT,
					new ArrayList<Parcelable>(results));
			callbackIntent.send(context, 0, extrasIntent);
		} catch (final PendingIntent.CanceledException e) {
			// ignore
		}
	}

	@Override
	public void onScanFailed(final int errorCode) {
        final Context context = this.context;
        if (context == null)
            return;

		try {
			final Intent extrasIntent = new Intent();
			extrasIntent.putExtra(BluetoothLeScannerCompat.EXTRA_ERROR_CODE, errorCode);
			callbackIntent.send(context, 0, extrasIntent);
		} catch (final PendingIntent.CanceledException e) {
			// ignore
		}
	}
}