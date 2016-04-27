/*
 * Copyright (c) 2015, Nordic Semiconductor
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
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides methods to perform scan related operations for Bluetooth LE devices. An
 * application can scan for a particular type of Bluetooth LE devices using {@link ScanFilter}. It
 * can also request different types of callbacks for delivering the result.
 * <p>
 * Use {@link BluetoothLeScannerCompat#getScanner()} to get an instance of the scanner.
 * <p>
 * <b>Note:</b> Most of the scan methods here require
 * {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
 *
 * @see ScanFilter
 */
public abstract class BluetoothLeScannerCompat {
	private static BluetoothLeScannerCompat mInstance;
	private final Handler mHandler;

	/**
	 * Returns the scanner compat object
	 * @return scanner implementation
	 */
	public static BluetoothLeScannerCompat getScanner() {
		if (mInstance != null)
			return mInstance;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			return mInstance = new BluetoothLeScannerImplMarshmallow();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return mInstance = new BluetoothLeScannerImplLollipop();
		return mInstance = new BluetoothLeScannerImplJB();
	}

	/* package */ BluetoothLeScannerCompat() {
		mHandler = new Handler(Looper.getMainLooper());
	}

	/**
	 * Start Bluetooth LE scan with default parameters and no filters. The scan results will be
	 * delivered through {@code callback}.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 * An app must hold
	 * {@link Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_COARSE_LOCATION} or
	 * {@link Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION} permission
	 * in order to get results.
	 *
	 * @param callback Callback used to deliver scan results.
	 * @throws IllegalArgumentException If {@code callback} is null.
	 */
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	public void startScan(final ScanCallback callback) {
		if (callback == null) {
			throw new IllegalArgumentException("callback is null");
		}
		startScanInternal(null, new ScanSettings.Builder().build(), callback);
	}

	/**
	 * Start Bluetooth LE scan. The scan results will be delivered through {@code callback}.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 * An app must hold
	 * {@link Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_COARSE_LOCATION} or
	 * {@link Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION} permission
	 * in order to get results.
	 *
	 * @param filters {@link ScanFilter}s for finding exact BLE devices.
	 * @param settings Settings for the scan.
	 * @param callback Callback used to deliver scan results.
	 * @throws IllegalArgumentException If {@code settings} or {@code callback} is null.
	 */
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	public void startScan(final List<ScanFilter> filters, final ScanSettings settings, final ScanCallback callback) {
		if (settings == null || callback == null) {
			throw new IllegalArgumentException("settings or callback is null");
		}
		startScanInternal(filters, settings, callback);
	}

	/**
	 * Starts Bluetooth LE scan. Its implementation depends on the Android version.
	 *
	 * @param filters {@link ScanFilter}s for finding exact BLE devices.
	 * @param settings Settings for the scan.
	 * @param callback Callback used to deliver scan results.
	 */
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	/* package */ abstract void startScanInternal(final List<ScanFilter> filters, final ScanSettings settings, final ScanCallback callback);

	/**
	 * Stops an ongoing Bluetooth LE scan.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 *
	 * @param callback
	 */
	@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
	public abstract void stopScan(final ScanCallback callback);

	/**
	 * Flush pending batch scan results stored in Bluetooth controller. This will return Bluetooth
	 * LE scan results batched on bluetooth controller. Returns immediately, batch scan results data
	 * will be delivered through the {@code callback}.
	 *
	 * @param callback Callback of the Bluetooth LE Scan, it has to be the same instance as the one
	 *            used to start scan.
	 */
	public abstract void flushPendingScanResults(final ScanCallback callback);

	/* package */ class ScanCallbackWrapper {
		private final List<ScanFilter> mFilters;
		private final ScanSettings mScanSettings;
		private final ScanCallback mScanCallback;
		private final List<ScanResult> mScanResults;
		private final List<String> mDevicesInBatch;

		/** A task, called periodically, that notifies about match lost. */
		private MatchLostNotifierTask mMatchLostNotifierTask;
		/** A collection of scan result of devices in range. */
		private final Map<String, ScanResult> mDevicesInRange;

		private final Runnable mFlushPendingScanResultsTask = new Runnable() {
			@Override
			public void run() {
				flushPendingScanResults();
				mHandler.postDelayed(this, mScanSettings.getReportDelayMillis());
			}
		};

		/* package */ ScanCallbackWrapper(final List<ScanFilter> filters, final ScanSettings settings, final ScanCallback callback) {
			mFilters = filters;
			mScanSettings = settings;
			mScanCallback = callback;

			// Emulate other callback types
			if (settings.getCallbackType() != ScanSettings.CALLBACK_TYPE_ALL_MATCHES && !settings.getUseHardwareCallbackTypesIfSupported()) {
				mDevicesInRange = new HashMap<>();
			} else
				mDevicesInRange = null;

			// Emulate batching
			final long delay = settings.getReportDelayMillis();
			if (delay > 0) {
				mScanResults = new ArrayList<>();
				mDevicesInBatch = new ArrayList<>();
				mHandler.postDelayed(mFlushPendingScanResultsTask, delay);
			} else {
				mScanResults = null;
				mDevicesInBatch = null;
			}
		}

		/* package */ void close() {
			if (mScanResults != null) {
				mHandler.removeCallbacks(mFlushPendingScanResultsTask);
			}
			if (mDevicesInRange != null) {
				mDevicesInRange.clear();
			}
			if (mMatchLostNotifierTask != null) {
				mHandler.removeCallbacks(mMatchLostNotifierTask);
				mMatchLostNotifierTask = null;
			}
		}

		/* package */ ScanSettings getScanSettings() {
			return mScanSettings;
		}

		/* package */ List<ScanFilter> getScanFilters() {
			return mFilters;
		}

		/* package */ ScanCallback getScanCallback() {
			return mScanCallback;
		}

		/* package */ void flushPendingScanResults() {
			if (mScanResults != null) {
				synchronized (mScanResults) {
					mScanCallback.onBatchScanResults(mScanResults);
					mScanResults.clear();
					mDevicesInBatch.clear();
				}
			}
		}

		private class MatchLostNotifierTask implements Runnable {
			private final List<ScanResult> mMatchLostScanResults = new ArrayList<>();

			@Override
			public void run() {
				final long now = SystemClock.elapsedRealtimeNanos();

				final Collection<ScanResult> results = mDevicesInRange.values();
				for (final ScanResult result : results) {
					if (result.getTimestampNanos() < now - mScanSettings.getMatchLostDeviceTimeout()) {
						mMatchLostScanResults.add(result);
					}
				}
				if (!mMatchLostScanResults.isEmpty()) {
					for (final ScanResult result : mMatchLostScanResults) {
						mDevicesInRange.remove(result.getDevice().getAddress());
						onFoundOrLost(false, result);
					}
					mMatchLostScanResults.clear();
				}
				mHandler.postDelayed(mMatchLostNotifierTask, mScanSettings.getMatchLostTaskInterval());
			}
		}

		/* package */ void handleScanResult(final ScanResult scanResult) {
			if (mFilters != null && !mFilters.isEmpty() && !matches(scanResult))
				return;

			final String deviceAddress = scanResult.getDevice().getAddress();

			// Notify if a new device was found and callback type is FIRST MATCH
			if (mDevicesInRange != null) { // -> Callback type != ScanSettings.CALLBACK_TYPE_ALL_MATCHES
				// Save the fist result or update tle old one with new data
				final ScanResult previousResult = mDevicesInRange.put(deviceAddress, scanResult);
				if (previousResult == null) {
					if ((mScanSettings.getCallbackType() & ScanSettings.CALLBACK_TYPE_FIRST_MATCH) > 0)
						onFoundOrLost(true, scanResult);
				}

				// In case user wants to be notified about match lost, we need to start a task that will check periodically
				if ((mScanSettings.getCallbackType() & ScanSettings.CALLBACK_TYPE_MATCH_LOST) > 0 && mMatchLostNotifierTask == null) {
					mMatchLostNotifierTask = new MatchLostNotifierTask();
					mHandler.postDelayed(mMatchLostNotifierTask, mScanSettings.getMatchLostTaskInterval());
				}
			} else {
				// A callback type may not contain CALLBACK_TYPE_ALL_MATCHES and any other value. If mDevicesInRange is empty
				// Report delay > 0 means we are emulating hardware batching. Otherwise handleScanResults(List) is called, not this method.
				if (mScanSettings.getReportDelayMillis() > 0) {
					synchronized (mScanResults) {
						if (!mDevicesInBatch.contains(deviceAddress)) {  // add only the first record from the device, others will be skipped
							mScanResults.add(scanResult);
							mDevicesInBatch.add(deviceAddress);
						}
					}
					return;
				}
				onScanResult(scanResult);
			}
		}

		/* package */ void handleScanResults(final List<ScanResult> results, final boolean offloadedFilteringSupported) {
			List<ScanResult> filteredResults = results;

			if (mFilters != null && (!offloadedFilteringSupported || !mScanSettings.getUseHardwareFilteringIfSupported())) {
				filteredResults = new ArrayList<>();
				for (final ScanResult result : results)
					if (matches(result))
						filteredResults.add(result);
			}

			onBatchScanResults(filteredResults);
		}

		private boolean matches(final ScanResult result) {
			for (final ScanFilter filter : mFilters) {
				if (filter.matches(result))
					return true;
			}
			return false;
		}

		private void onScanResult(final ScanResult scanResult) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mScanCallback.onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, scanResult);
				}
			});
		}

		private void onBatchScanResults(final List<ScanResult> results) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mScanCallback.onBatchScanResults(results);
				}
			});
		}

		private void onFoundOrLost(final boolean onFound, final ScanResult scanResult) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (onFound) {
						mScanCallback.onScanResult(ScanSettings.CALLBACK_TYPE_FIRST_MATCH, scanResult);
					} else {
						mScanCallback.onScanResult(ScanSettings.CALLBACK_TYPE_MATCH_LOST, scanResult);
					}
				}
			});
		}

		/* package */ void onScanManagerErrorCallback(final int errorCode) {
			postCallbackError(mScanCallback, errorCode);
		}
	}

	private void postCallbackError(final ScanCallback callback, final int errorCode) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				callback.onScanFailed(errorCode);
			}
		});
	}
}
