package no.nordicsemi.android.support.v18.scanner;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A ScanCallback that will send a {@link PendingIntent} when callback's methods are called.
 */
/* package */ class PendingIntentExecutor extends ScanCallback {

	@NonNull private final PendingIntent callbackIntent;

	/** A temporary context given to the {@link android.content.BroadcastReceiver}. */
	@Nullable private Context context;
	/** The service using this executor. */
	@Nullable private Context service;

    private long lastBatchTimestamp;
    private long reportDelay;

    /**
     * Creates the {@link PendingIntent} executor that will be used from a
     * {@link android.content.BroadcastReceiver}. The {@link Context} may change in every
     * {@link android.content.BroadcastReceiver#onReceive(Context, Intent)} call, so it is not
     * kept here. Instead, a temporary context must be set with {@link #setTemporaryContext(Context)}
     * each time before the received results are handled and released after that to
     * prevent from keeping a string reference to the context by a static object.
     *
     * @param callbackIntent User's {@link PendingIntent} used in
     *                       {@link BluetoothLeScannerCompat#startScan(List, ScanSettings, Context, PendingIntent)}.
	 * @param settings       Scan settings specified by the user.
     */
	PendingIntentExecutor(@NonNull final PendingIntent callbackIntent,
						  @NonNull final ScanSettings settings) {
		this.callbackIntent = callbackIntent;
		this.reportDelay = settings.getReportDelayMillis();
	}

	/**
	 * Creates the {@link PendingIntent} executor that will be used from a {@link Service}.
	 * The service instance will be used as {@link Context} to send intents.
	 *
	 * @param callbackIntent User's {@link PendingIntent} used in
	 *                       {@link BluetoothLeScannerCompat#startScan(List, ScanSettings, Context, PendingIntent)}.
	 * @param settings       Scan settings specified by the user.
	 * @param service        The service that will scan for Bluetooth LE devices in background.
	 */
	PendingIntentExecutor(@NonNull final PendingIntent callbackIntent,
						  @NonNull final ScanSettings settings,
						  @NonNull final Service service) {
		this.callbackIntent = callbackIntent;
		this.reportDelay = settings.getReportDelayMillis();
		this.service = service;
	}

	/* package */ void setTemporaryContext(@Nullable final Context context) {
		this.context = context;
	}

	@Override
	public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
		final Context context = this.context != null ? this.context : this.service;
		if (context == null)
			return;

		try {
			final Intent extrasIntent = new Intent();
			extrasIntent.putExtra(BluetoothLeScannerCompat.EXTRA_CALLBACK_TYPE, callbackType);
			extrasIntent.putParcelableArrayListExtra(BluetoothLeScannerCompat.EXTRA_LIST_SCAN_RESULT,
					new ArrayList<>(Collections.singletonList(result)));
			callbackIntent.send(context, 0, extrasIntent);
		} catch (final PendingIntent.CanceledException e) {
			// Ignore
		}
	}

	@Override
	public void onBatchScanResults(@NonNull final List<ScanResult> results) {
		final Context context = this.context != null ? this.context : this.service;
		if (context == null)
			return;

        // On several phones the broadcast is sent twice for every batch.
        // Skip the second call if came to early.
        final long now = SystemClock.elapsedRealtime();
		if (lastBatchTimestamp > now - reportDelay + 5) {
            return;
        }
        lastBatchTimestamp = now;

		try {
			final Intent extrasIntent = new Intent();
			extrasIntent.putExtra(BluetoothLeScannerCompat.EXTRA_CALLBACK_TYPE,
					ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
			extrasIntent.putParcelableArrayListExtra(BluetoothLeScannerCompat.EXTRA_LIST_SCAN_RESULT,
					new ArrayList<Parcelable>(results));
			extrasIntent.setExtrasClassLoader(ScanResult.class.getClassLoader());
			callbackIntent.send(context, 0, extrasIntent);
		} catch (final PendingIntent.CanceledException e) {
			// Ignore
		}
	}

	@Override
	public void onScanFailed(final int errorCode) {
		final Context context = this.context != null ? this.context : this.service;
		if (context == null)
			return;

		try {
			final Intent extrasIntent = new Intent();
			extrasIntent.putExtra(BluetoothLeScannerCompat.EXTRA_ERROR_CODE, errorCode);
			callbackIntent.send(context, 0, extrasIntent);
		} catch (final PendingIntent.CanceledException e) {
			// Ignore
		}
	}
}