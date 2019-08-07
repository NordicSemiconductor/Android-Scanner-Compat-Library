package no.nordicsemi.android.support.v18.scanner;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * This receiver, registered in AndroidManifest, will translate received
 * {@link android.bluetooth.le.ScanResult}s into compat {@link ScanResult}s and will send
 * a {@link PendingIntent} registered by the user with those converted data. It will also apply
 * any filters, perform batching or emulate callback types
 * {@link ScanSettings#CALLBACK_TYPE_FIRST_MATCH} and
 * {@link ScanSettings#CALLBACK_TYPE_MATCH_LOST} on devices that do not support it.
 */
public class PendingIntentReceiver extends BroadcastReceiver {

	/* package */ static final String ACTION = "no.nordicsemi.android.support.v18.ACTION_FOUND";
	/* package */ static final String EXTRA_PENDING_INTENT = "no.nordicsemi.android.support.v18.EXTRA_PENDING_INTENT";
	/* package */ static final String EXTRA_FILTERS = "no.nordicsemi.android.support.v18.EXTRA_FILTERS";
	/* package */ static final String EXTRA_SETTINGS = "no.nordicsemi.android.support.v18.EXTRA_SETTINGS";
	/* package */ static final String EXTRA_USE_HARDWARE_BATCHING = "no.nordicsemi.android.support.v18.EXTRA_USE_HARDWARE_BATCHING";
	/* package */ static final String EXTRA_USE_HARDWARE_FILTERING = "no.nordicsemi.android.support.v18.EXTRA_USE_HARDWARE_FILTERING";
	/* package */ static final String EXTRA_USE_HARDWARE_CALLBACK_TYPES = "no.nordicsemi.android.support.v18.EXTRA_USE_HARDWARE_CALLBACK_TYPES";
	/* package */ static final String EXTRA_MATCH_LOST_TIMEOUT = "no.nordicsemi.android.support.v18.EXTRA_MATCH_LOST_TIMEOUT";
	/* package */ static final String EXTRA_MATCH_LOST_INTERVAL = "no.nordicsemi.android.support.v18.EXTRA_MATCH_LOST_INTERVAL";
	/* package */ static final String EXTRA_MATCH_MODE = "no.nordicsemi.android.support.v18.EXTRA_MATCH_MODE";
	/* package */ static final String EXTRA_NUM_OF_MATCHES = "no.nordicsemi.android.support.v18.EXTRA_NUM_OF_MATCHES";

	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	public void onReceive(final Context context, final Intent intent) {
		// Ensure we are ok.
		if (context == null || intent == null)
			return;

		// Find the target pending intent.
		final PendingIntent callbackIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT);
		if (callbackIntent == null)
			return;

		// Filters and settings have been set as native objects, otherwise they could not be
		// serialized by the system scanner.
		final ArrayList<android.bluetooth.le.ScanFilter> nativeScanFilters =
				intent.getParcelableArrayListExtra(EXTRA_FILTERS);
		final android.bluetooth.le.ScanSettings nativeScanSettings = intent.getParcelableExtra(EXTRA_SETTINGS);
		if (nativeScanFilters == null || nativeScanSettings == null)
			return;

		// Some ScanSettings parameters are only on compat version and need to be sent separately.
		final boolean useHardwareBatchingIfSupported = intent.getBooleanExtra(EXTRA_USE_HARDWARE_BATCHING, true);
		final boolean useHardwareFilteringIfSupported = intent.getBooleanExtra(EXTRA_USE_HARDWARE_FILTERING, true);
		final boolean useHardwareCallbackTypesIfSupported = intent.getBooleanExtra(EXTRA_USE_HARDWARE_CALLBACK_TYPES, true);
		final long matchLostDeviceTimeout = intent.getLongExtra(EXTRA_MATCH_LOST_TIMEOUT, ScanSettings.MATCH_LOST_DEVICE_TIMEOUT_DEFAULT);
		final long matchLostTaskInterval = intent.getLongExtra(EXTRA_MATCH_LOST_INTERVAL, ScanSettings.MATCH_LOST_TASK_INTERVAL_DEFAULT);
		final int matchMode = intent.getIntExtra(EXTRA_MATCH_MODE, ScanSettings.MATCH_MODE_AGGRESSIVE);
		final int numOfMatches = intent.getIntExtra(EXTRA_NUM_OF_MATCHES, ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);

		// Convert native objects to compat versions.
		final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
		final BluetoothLeScannerImplOreo scannerImpl = (BluetoothLeScannerImplOreo) scanner;
		final ArrayList<ScanFilter> filters = scannerImpl.fromNativeScanFilters(nativeScanFilters);
		final ScanSettings settings = scannerImpl.fromNativeScanSettings(nativeScanSettings,
				useHardwareBatchingIfSupported,
				useHardwareFilteringIfSupported,
				useHardwareCallbackTypesIfSupported,
				matchLostDeviceTimeout, matchLostTaskInterval,
				matchMode, numOfMatches);

		// Check device capabilities and create a wrapper that will send a PendingIntent.
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		final boolean offloadedBatchingSupported = adapter.isOffloadedScanBatchingSupported();
		final boolean offloadedFilteringSupported = adapter.isOffloadedFilteringSupported();

		// Obtain or create a PendingIntentExecutorWrapper. A static instance (obtained from a
		// static BluetoothLeScannerCompat singleton) is necessary as it allows to keeps
		// track of found devices and emulate batching and callback types if those are not
		// supported or a compat version was forced.

		BluetoothLeScannerImplOreo.PendingIntentExecutorWrapper wrapper;
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (scanner) {
			try {
				wrapper = scannerImpl.getWrapper(callbackIntent);
			} catch (final IllegalStateException e) {
				// Scanning has been stopped.
				return;
			}
			if (wrapper == null) {
				// Wrapper has not been created, or was created, but the app was then killed
				// and must be created again. Some information will be lost (batched devices).
				wrapper = new BluetoothLeScannerImplOreo.PendingIntentExecutorWrapper(offloadedBatchingSupported,
						offloadedFilteringSupported, filters, settings, callbackIntent);
				scannerImpl.addWrapper(callbackIntent, wrapper);
			}
		}

		// The context may change each time. Set the one time temporary context that will be used
		// to send PendingIntent. It will be released after the results were handled.
		wrapper.executor.setTemporaryContext(context);

		// Check what results were received and send them to PendingIntent.
		final List<android.bluetooth.le.ScanResult> nativeScanResults =
				intent.getParcelableArrayListExtra(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT);
		if (nativeScanResults != null) {
			final ArrayList<ScanResult> results = scannerImpl.fromNativeScanResults(nativeScanResults);

			if (settings.getReportDelayMillis() > 0) {
				wrapper.handleScanResults(results);
			} else if (!results.isEmpty()) {
				final int callbackType = intent.getIntExtra(BluetoothLeScanner.EXTRA_CALLBACK_TYPE,
						ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
				wrapper.handleScanResult(callbackType, results.get(0));
			}
		} else {
			final int errorCode = intent.getIntExtra(BluetoothLeScanner.EXTRA_ERROR_CODE, 0);
			if (errorCode != 0) {
				wrapper.handleScanError(errorCode);
			}
		}

		// Release the temporary context reference, so that static executor does not hold a
		// reference to a context.
		wrapper.executor.setTemporaryContext(null);
	}
}
