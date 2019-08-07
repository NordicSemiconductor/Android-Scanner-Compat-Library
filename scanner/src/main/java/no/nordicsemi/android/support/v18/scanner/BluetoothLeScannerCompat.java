/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.support.v18.scanner;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

/**
 * This class provides methods to perform scan related operations for Bluetooth LE devices. An
 * application can scan for a particular type of Bluetooth LE devices using {@link ScanFilter}. It
 * can also request different types of callbacks for delivering the result.
 * <p>
 * Use {@link BluetoothLeScannerCompat#getScanner()} to get an instance of the scanner.
 * <p>
 * Since version 1.3.0 of the Scanner Compar Library library,
 * {@link PendingIntentReceiver} and {@link ScannerService} will be added to AndroidManifest
 * whether the scanning with {@link PendingIntent} feature is used or not.
 * The {@link ScannerService} is used to emulate such scanning on platforms
 * before Oreo, while {@link PendingIntentReceiver} is used to translate from native
 * {@link android.bluetooth.le.ScanResult} to compat {@link ScanResult} and apply additional
 * filtering.
 * <p>
 * <b>Note:</b> Most of the scan methods here require
 * {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
 *
 * @see ScanFilter
 */
@SuppressWarnings("WeakerAccess")
public abstract class BluetoothLeScannerCompat {

	/**
	 * Extra containing a list of ScanResults. It can have one or more results if there was no
	 * error. In case of error, {@link #EXTRA_ERROR_CODE} will contain the error code and this
	 * extra will not be available.
	 */
	public static final String EXTRA_LIST_SCAN_RESULT =
			"android.bluetooth.le.extra.LIST_SCAN_RESULT";

	/**
	 * Optional extra indicating the error code, if any. The error code will be one of the
	 * SCAN_FAILED_* codes in {@link android.bluetooth.le.ScanCallback}.
	 */
	public static final String EXTRA_ERROR_CODE = "android.bluetooth.le.extra.ERROR_CODE";

	/**
	 * Optional extra indicating the callback type, which will be one of
	 * CALLBACK_TYPE_* constants in {@link android.bluetooth.le.ScanSettings}.
	 *
	 * @see android.bluetooth.le.ScanCallback#onScanResult(int, android.bluetooth.le.ScanResult)
	 */
	public static final String EXTRA_CALLBACK_TYPE = "android.bluetooth.le.extra.CALLBACK_TYPE";

	private static BluetoothLeScannerCompat instance;

	/**
	 * Returns the scanner compat object
	 * @return scanner implementation
	 */
	@NonNull
	public synchronized static BluetoothLeScannerCompat getScanner() {
		if (instance != null)
			return instance;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			return instance = new BluetoothLeScannerImplOreo();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			return instance = new BluetoothLeScannerImplMarshmallow();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return instance = new BluetoothLeScannerImplLollipop();
		return instance = new BluetoothLeScannerImplJB();
	}

	/* package */ BluetoothLeScannerCompat() {}

	/**
	 * Start Bluetooth LE scan with default parameters and no filters. The scan results will be
	 * delivered through {@code callback}.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 * An app must hold
	 * {@link Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_FINE_LOCATION} permission
	 * in order to get results.
	 *
	 * @param callback Callback used to deliver scan results.
	 * @throws IllegalArgumentException If {@code callback} is null.
	 */
	@SuppressWarnings("WeakerAccess")
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	public final void startScan(@NonNull final ScanCallback callback) {
		//noinspection ConstantConditions
		if (callback == null) {
			throw new IllegalArgumentException("callback is null");
		}
		final Handler handler = new Handler(Looper.getMainLooper());
		startScanInternal(Collections.<ScanFilter>emptyList(), new ScanSettings.Builder().build(),
				callback, handler);
	}

	/**
	 * Start Bluetooth LE scan. The scan results will be delivered through {@code callback}.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 * An app must hold
	 * {@link Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_FINE_LOCATION} permission
	 * in order to get results.
	 *
	 * @param filters {@link ScanFilter}s for finding exact BLE devices.
	 * @param settings Optional settings for the scan.
	 * @param callback Callback used to deliver scan results.
	 * @throws IllegalArgumentException If {@code settings} or {@code callback} is null.
	 */
	@SuppressWarnings("unused")
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	public final void startScan(@Nullable final List<ScanFilter> filters,
								@Nullable final ScanSettings settings,
								@NonNull  final ScanCallback callback) {
		//noinspection ConstantConditions
		if (callback == null) {
			throw new IllegalArgumentException("callback is null");
		}
		final Handler handler = new Handler(Looper.getMainLooper());
		startScanInternal(filters != null ? filters : Collections.<ScanFilter>emptyList(),
				settings != null ? settings : new ScanSettings.Builder().build(),
				callback, handler);
	}

	/**
	 * Start Bluetooth LE scan. The scan results will be delivered through {@code callback}.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 * An app must hold
	 * {@link Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_FINE_LOCATION} permission
	 * in order to get results.
	 *
	 * @param filters {@link ScanFilter}s for finding exact BLE devices.
	 * @param settings Optional settings for the scan.
	 * @param callback Callback used to deliver scan results.
	 * @param handler  Optional handler used to deliver results.
	 * @throws IllegalArgumentException If {@code settings} or {@code callback} is null.
	 */
	@SuppressWarnings("unused")
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	public final void startScan(@Nullable final List<ScanFilter> filters,
								@Nullable final ScanSettings settings,
								@NonNull  final ScanCallback callback,
								@Nullable final Handler handler) {
		//noinspection ConstantConditions
		if (callback == null) {
			throw new IllegalArgumentException("callback is null");
		}
		startScanInternal(filters != null ? filters : Collections.<ScanFilter>emptyList(),
				settings != null ? settings : new ScanSettings.Builder().build(),
				callback, handler != null ? handler : new Handler(Looper.getMainLooper()));
	}

	/**
	 * Stops an ongoing Bluetooth LE scan.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 *
	 * @param callback The callback used to start scanning.
	 */
	@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
	public final void stopScan(@NonNull final ScanCallback callback) {
		//noinspection ConstantConditions
		if (callback == null) {
			throw new IllegalArgumentException("callback is null");
		}
		stopScanInternal(callback);
	}

	/**
	 * Starts Bluetooth LE scan. Its implementation depends on the Android version.
	 *
	 * @param filters {@link ScanFilter}s for finding exact BLE devices.
	 * @param settings Settings for the scan.
	 * @param callback Callback used to deliver scan results.
	 * @param handler  Handler used to deliver results.
	 */
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	/* package */ abstract void startScanInternal(@NonNull List<ScanFilter> filters,
												  @NonNull ScanSettings settings,
												  @NonNull ScanCallback callback,
												  @NonNull Handler handler);

	/**
	 * Stops an ongoing Bluetooth LE scan. Its implementation depends on the Android version.
	 *
	 * @param callback The callback used to start scanning.
	 */
	@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
	/* package */ abstract void stopScanInternal(@NonNull ScanCallback callback);

	/**
	 * Start Bluetooth LE scan using a {@link PendingIntent}. The scan results will be delivered
	 * via the PendingIntent. On platforms before Oreo this will start {@link ScannerService}
	 * which will scan in background using given settings.
	 * <p>
	 * This method of scanning is intended to work in background. Long running scanning may
	 * consume a lot of battery, so it is recommended to use low power mode in settings,
	 * offloaded filtering and batching. However, the library may emulate batching, filtering or
	 * callback types {@link ScanSettings#CALLBACK_TYPE_FIRST_MATCH} and
	 * {@link ScanSettings#CALLBACK_TYPE_MATCH_LOST} if they are not supported.
	 * <p>
	 * A {@link PendingIntentReceiver} and {@link ScannerService} will be added to AndroidManifest
	 * whether this feature is used or not.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 * An app must hold
	 * {@link Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_FINE_LOCATION} permission
	 * in order to get results.
	 * <p>
	 * When the PendingIntent is delivered, the Intent passed to the receiver or activity will
	 * contain one or more of the extras {@link #EXTRA_CALLBACK_TYPE}, {@link #EXTRA_ERROR_CODE} and
	 * {@link #EXTRA_LIST_SCAN_RESULT} to indicate the result of the scan.
	 *
	 * @param filters        {@link ScanFilter}s for finding exact BLE devices.
	 * @param settings       Optional settings for the scan.
	 * @param context        Context used to start {@link ScannerService}.
	 * @param callbackIntent The PendingIntent to deliver the result to.
	 * @throws IllegalArgumentException If {@code settings} or {@code callback} is null.
	 */
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	public final void startScan(@Nullable final List<ScanFilter> filters,
								@Nullable final ScanSettings settings,
								@NonNull  final Context context,
								@NonNull  final PendingIntent callbackIntent) {
		//noinspection ConstantConditions
		if (callbackIntent == null) {
			throw new IllegalArgumentException("callbackIntent is null");
		}
		//noinspection ConstantConditions
		if (context == null) {
			throw new IllegalArgumentException("context is null");
		}
		startScanInternal(filters != null ? filters : Collections.<ScanFilter>emptyList(),
				settings != null ? settings : new ScanSettings.Builder().build(),
				context, callbackIntent);
	}

	/**
	 * Stops an ongoing Bluetooth LE scan.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 *
	 * @param context        Context used to stop {@link ScannerService}.
	 * @param callbackIntent The PendingIntent that was used to start the scan.
	 */
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	public final void stopScan(@NonNull final Context context,
							   @NonNull final PendingIntent callbackIntent) {
		//noinspection ConstantConditions
		if (callbackIntent == null) {
			throw new IllegalArgumentException("callbackIntent is null");
		}
		//noinspection ConstantConditions
		if (context == null) {
			throw new IllegalArgumentException("context is null");
		}
		stopScanInternal(context, callbackIntent);
	}

	/**
	 * Starts Bluetooth LE scan using PendingIntent.
	 * Its implementation depends on the Android version.
	 *
	 * @param filters        {@link ScanFilter}s for finding exact BLE devices.
	 * @param settings       Settings for the scan.
	 * @param context        Context used to start {@link ScannerService}.
	 * @param callbackIntent The PendingIntent to deliver the result to.
	 */
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	/* package */ abstract void startScanInternal(@NonNull List<ScanFilter> filters,
												  @NonNull ScanSettings settings,
												  @NonNull Context context,
												  @NonNull PendingIntent callbackIntent);

	/**
	 * Stops an ongoing Bluetooth LE scan.
	 *
	 * @param context        Context used to stop {@link ScannerService}.
	 * @param callbackIntent The PendingIntent that was used to start the scan.
	 */
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	/* package */ abstract void stopScanInternal(@NonNull Context context,
												 @NonNull PendingIntent callbackIntent);

	/**
	 * Flush pending batch scan results stored in Bluetooth controller. This will return Bluetooth
	 * LE scan results batched on Bluetooth controller. Returns immediately, batch scan results data
	 * will be delivered through the {@code callback}.
	 *
	 * @param callback Callback of the Bluetooth LE Scan, it has to be the same instance as the one
	 *            used to start scan.
	 */
	@SuppressWarnings("unused")
	public abstract void flushPendingScanResults(@NonNull ScanCallback callback);

	/* package */ static class ScanCallbackWrapper {

		@NonNull private final Object LOCK = new Object();

		private final boolean emulateFiltering;
		private final boolean emulateBatching;
		private final boolean emulateFoundOrLostCallbackType;
		private boolean scanningStopped;

		@NonNull final List<ScanFilter> filters;
		@NonNull final ScanSettings scanSettings;
		@NonNull final ScanCallback scanCallback;
		@NonNull final Handler handler;

		@NonNull private final List<ScanResult> scanResults = new ArrayList<>();

		@NonNull private final Set<String> devicesInBatch = new HashSet<>();

		/** A collection of scan result of devices in range. */
		@NonNull private final Map<String, ScanResult> devicesInRange = new HashMap<>();

		@NonNull
		private final Runnable flushPendingScanResultsTask = new Runnable() {
			@Override
			public void run() {
				if (!scanningStopped) {
					flushPendingScanResults();
					handler.postDelayed(this, scanSettings.getReportDelayMillis());
				}
			}
		};

		/** A task, called periodically, that notifies about match lost. */
		@NonNull
		private final Runnable matchLostNotifierTask = new Runnable() {
			@Override
			public void run() {
				final long now = SystemClock.elapsedRealtimeNanos();

				synchronized (LOCK) {
					final Iterator<ScanResult> iterator = devicesInRange.values().iterator();
					while (iterator.hasNext()) {
						final ScanResult result = iterator.next();
						if (result.getTimestampNanos() < now - scanSettings.getMatchLostDeviceTimeout()) {
							iterator.remove();
							handler.post(new Runnable() {
								@Override
								public void run() {
									scanCallback.onScanResult(ScanSettings.CALLBACK_TYPE_MATCH_LOST, result);
								}
							});
						}
					}

					if (!devicesInRange.isEmpty()) {
						handler.postDelayed(this, scanSettings.getMatchLostTaskInterval());
					}
				}
			}
		};

		/* package */ ScanCallbackWrapper(final boolean offloadedBatchingSupported,
										  final boolean offloadedFilteringSupported,
										  @NonNull final List<ScanFilter> filters,
										  @NonNull final ScanSettings settings,
										  @NonNull final ScanCallback callback,
										  @NonNull final Handler handler) {
			this.filters = Collections.unmodifiableList(filters);
			this.scanSettings = settings;
			this.scanCallback = callback;
			this.handler = handler;
			this.scanningStopped = false;

			// Emulate other callback types
			final boolean callbackTypesSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
			emulateFoundOrLostCallbackType = settings.getCallbackType() != ScanSettings.CALLBACK_TYPE_ALL_MATCHES
					&& (!callbackTypesSupported || !settings.getUseHardwareCallbackTypesIfSupported());

			// Emulate filtering
			emulateFiltering = !filters.isEmpty() && (!offloadedFilteringSupported || !settings.getUseHardwareFilteringIfSupported());

			// Emulate batching
			final long delay = settings.getReportDelayMillis();
			emulateBatching = delay > 0 && (!offloadedBatchingSupported || !settings.getUseHardwareBatchingIfSupported());
			if (emulateBatching) {
				handler.postDelayed(flushPendingScanResultsTask, delay);
			}
		}

		/* package */ void close() {
			scanningStopped = true;
			handler.removeCallbacksAndMessages(null);
			synchronized (LOCK) {
				devicesInRange.clear();
				devicesInBatch.clear();
				scanResults.clear();
			}
		}

		/* package */ void flushPendingScanResults() {
			if (emulateBatching && !scanningStopped) {
				synchronized (LOCK) {
					scanCallback.onBatchScanResults(new ArrayList<>(scanResults));
					scanResults.clear();
					devicesInBatch.clear();
				}
			}
		}

		/* package */ void handleScanResult(final int callbackType,
											@NonNull final ScanResult scanResult) {
			if (scanningStopped || !filters.isEmpty() && !matches(scanResult))
				return;

			final String deviceAddress = scanResult.getDevice().getAddress();

			// Notify if a new device was found and callback type is FIRST MATCH
			if (emulateFoundOrLostCallbackType) { // -> Callback type != ScanSettings.CALLBACK_TYPE_ALL_MATCHES
				ScanResult previousResult;
				boolean firstResult;
				synchronized (devicesInRange) {
					// The periodic task will be started only on the first result
					firstResult = devicesInRange.isEmpty();
					// Save the first result or update the old one with new data
					previousResult = devicesInRange.put(deviceAddress, scanResult);
				}

				if (previousResult == null) {
					if ((scanSettings.getCallbackType() & ScanSettings.CALLBACK_TYPE_FIRST_MATCH) > 0) {
						scanCallback.onScanResult(ScanSettings.CALLBACK_TYPE_FIRST_MATCH, scanResult);
					}
				}

				// In case user wants to be notified about match lost, we need to start a task that
				// will check the timestamp periodically
				if (firstResult) {
					if ((scanSettings.getCallbackType() & ScanSettings.CALLBACK_TYPE_MATCH_LOST) > 0) {
						handler.removeCallbacks(matchLostNotifierTask);
						handler.postDelayed(matchLostNotifierTask, scanSettings.getMatchLostTaskInterval());
					}
				}
			} else {
				// A callback type may not contain CALLBACK_TYPE_ALL_MATCHES and any other value.
				// If devicesInRange is empty, report delay > 0 means we are emulating hardware
				// batching. Otherwise handleScanResults(List) is called, not this method.
				if (emulateBatching) {
					synchronized (LOCK) {
						if (!devicesInBatch.contains(deviceAddress)) {  // add only the first record from the device, others will be skipped
							scanResults.add(scanResult);
							devicesInBatch.add(deviceAddress);
						}
					}
					return;
				}

				scanCallback.onScanResult(callbackType, scanResult);
			}
		}

		/* package */ void handleScanResults(@NonNull final List<ScanResult> results) {
			if (scanningStopped)
				return;

			List<ScanResult> filteredResults = results;

			if (emulateFiltering) {
				filteredResults = new ArrayList<>();
				for (final ScanResult result : results)
					if (matches(result))
						filteredResults.add(result);
			}

			scanCallback.onBatchScanResults(filteredResults);
		}

		/* package */ void handleScanError(final int errorCode) {
			scanCallback.onScanFailed(errorCode);
		}

		private boolean matches(@NonNull final ScanResult result) {
			for (final ScanFilter filter : filters) {
				if (filter.matches(result))
					return true;
			}
			return false;
		}
	}
}
