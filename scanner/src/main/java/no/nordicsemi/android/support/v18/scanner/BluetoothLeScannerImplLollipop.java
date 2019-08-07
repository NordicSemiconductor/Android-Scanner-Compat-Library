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
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

@SuppressWarnings({"deprecation", "WeakerAccess"})
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
/* package */ class BluetoothLeScannerImplLollipop extends BluetoothLeScannerCompat {

	@NonNull private final Map<ScanCallback, ScanCallbackWrapperLollipop> wrappers = new HashMap<>();

	/* package */ BluetoothLeScannerImplLollipop() {}

	@Override
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	/* package */ void startScanInternal(@NonNull final List<ScanFilter> filters,
										 @NonNull final ScanSettings settings,
										 @NonNull final ScanCallback callback,
										 @NonNull final Handler handler) {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
		if (scanner == null)
			throw new IllegalStateException("BT le scanner not available");

		final boolean offloadedBatchingSupported = adapter.isOffloadedScanBatchingSupported();
		final boolean offloadedFilteringSupported = adapter.isOffloadedFilteringSupported();

		ScanCallbackWrapperLollipop wrapper;

		synchronized (wrappers) {
			if (wrappers.containsKey(callback)) {
				throw new IllegalArgumentException("scanner already started with given callback");
			}
			wrapper = new ScanCallbackWrapperLollipop(offloadedBatchingSupported,
					offloadedFilteringSupported, filters, settings, callback, handler);
			wrappers.put(callback, wrapper);
		}

		final android.bluetooth.le.ScanSettings nativeScanSettings = toNativeScanSettings(adapter, settings, false);
		List<android.bluetooth.le.ScanFilter> nativeScanFilters = null;
		if (!filters.isEmpty() && offloadedFilteringSupported && settings.getUseHardwareFilteringIfSupported())
			nativeScanFilters = toNativeScanFilters(filters);

		scanner.startScan(nativeScanFilters, nativeScanSettings, wrapper.nativeCallback);
	}

	@Override
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	/* package */ void stopScanInternal(@NonNull final ScanCallback callback) {
		ScanCallbackWrapperLollipop wrapper;
		synchronized (wrappers) {
			wrapper = wrappers.remove(callback);
		}
		if (wrapper == null)
			return;

		wrapper.close();

		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter != null) {
			final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
			if (scanner != null)
				scanner.stopScan(wrapper.nativeCallback);
		}
	}

	@Override
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
		/* package */ void startScanInternal(@NonNull final List<ScanFilter> filters,
											 @NonNull final ScanSettings settings,
											 @NonNull final Context context,
											 @NonNull final PendingIntent callbackIntent) {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
		if (scanner == null)
			throw new IllegalStateException("BT le scanner not available");

		final Intent service = new Intent(context, ScannerService.class);
		service.putParcelableArrayListExtra(ScannerService.EXTRA_FILTERS, new ArrayList<>(filters));
		service.putExtra(ScannerService.EXTRA_SETTINGS, settings);
		service.putExtra(ScannerService.EXTRA_PENDING_INTENT, callbackIntent);
		service.putExtra(ScannerService.EXTRA_START, true);
		context.startService(service);
	}

	@Override
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
		/* package */ void stopScanInternal(@NonNull final Context context,
											@NonNull final PendingIntent callbackIntent) {
		final Intent service = new Intent(context, ScannerService.class);
		service.putExtra(ScannerService.EXTRA_PENDING_INTENT, callbackIntent);
		service.putExtra(ScannerService.EXTRA_START, false);
		context.startService(service);
	}

	@Override
	@RequiresPermission(Manifest.permission.BLUETOOTH)
	public void flushPendingScanResults(@NonNull final ScanCallback callback) {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		//noinspection ConstantConditions
		if (callback == null) {
			throw new IllegalArgumentException("callback cannot be null!");
		}

		ScanCallbackWrapperLollipop wrapper;
		synchronized (wrappers) {
			wrapper = wrappers.get(callback);
		}

		if (wrapper == null) {
			throw new IllegalArgumentException("callback not registered!");
		}

		final ScanSettings settings = wrapper.scanSettings;
		if (adapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported()) {
			final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
			if (scanner == null)
				return;
			scanner.flushPendingScanResults(wrapper.nativeCallback);
		} else {
			wrapper.flushPendingScanResults();
		}
	}

	@NonNull
	/* package */ android.bluetooth.le.ScanSettings toNativeScanSettings(@NonNull final BluetoothAdapter adapter,
																		 @NonNull final ScanSettings settings,
																		 final boolean exactCopy) {
		final android.bluetooth.le.ScanSettings.Builder builder =
				new android.bluetooth.le.ScanSettings.Builder();

		if (exactCopy || adapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported())
			builder.setReportDelay(settings.getReportDelayMillis());

		if (settings.getScanMode() != ScanSettings.SCAN_MODE_OPPORTUNISTIC) {
			builder.setScanMode(settings.getScanMode());
		} else {
			// SCAN MORE OPPORTUNISTIC is not supported on Lollipop.
			// Instead, SCAN_MODE_LOW_POWER will be used.
			builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
		}

		settings.disableUseHardwareCallbackTypes(); // callback types other then CALLBACK_TYPE_ALL_MATCHES are not supported on Lollipop

		return builder.build();
	}

	@NonNull
	/* package */ ArrayList<android.bluetooth.le.ScanFilter> toNativeScanFilters(@NonNull final List<ScanFilter> filters) {
		final ArrayList<android.bluetooth.le.ScanFilter> nativeScanFilters = new ArrayList<>();
		for (final ScanFilter filter : filters)
			nativeScanFilters.add(toNativeScanFilter(filter));
		return nativeScanFilters;
	}

	@NonNull
	/* package */ android.bluetooth.le.ScanFilter toNativeScanFilter(@NonNull final ScanFilter filter) {
		final android.bluetooth.le.ScanFilter.Builder builder = new android.bluetooth.le.ScanFilter.Builder();
		builder.setDeviceAddress(filter.getDeviceAddress())
				.setDeviceName(filter.getDeviceName())
				.setServiceUuid(filter.getServiceUuid(), filter.getServiceUuidMask())
				.setManufacturerData(filter.getManufacturerId(), filter.getManufacturerData(), filter.getManufacturerDataMask());

		if (filter.getServiceDataUuid() != null)
			builder.setServiceData(filter.getServiceDataUuid(), filter.getServiceData(), filter.getServiceDataMask());

		return builder.build();
	}

	@NonNull
	/* package */ ScanResult fromNativeScanResult(@NonNull final android.bluetooth.le.ScanResult nativeScanResult) {
		final byte[] data = nativeScanResult.getScanRecord() != null ?
				nativeScanResult.getScanRecord().getBytes() : null;
		return new ScanResult(nativeScanResult.getDevice(), ScanRecord.parseFromBytes(data),
				nativeScanResult.getRssi(), nativeScanResult.getTimestampNanos());
	}

	@NonNull
	/* package */ ArrayList<ScanResult> fromNativeScanResults(@NonNull final List<android.bluetooth.le.ScanResult> nativeScanResults) {
		final ArrayList<ScanResult> results = new ArrayList<>();
		for (final android.bluetooth.le.ScanResult nativeScanResult : nativeScanResults) {
			final ScanResult result = fromNativeScanResult(nativeScanResult);
			results.add(result);
		}
		return results;
	}

	/* package */ static class ScanCallbackWrapperLollipop extends ScanCallbackWrapper {

		private ScanCallbackWrapperLollipop(final boolean offloadedBatchingSupported,
											final boolean offloadedFilteringSupported,
											@NonNull final List<ScanFilter> filters,
											@NonNull final ScanSettings settings,
											@NonNull final ScanCallback callback,
											@NonNull final Handler handler) {
			super(offloadedBatchingSupported, offloadedFilteringSupported,
					filters, settings, callback, handler);
		}

		@NonNull
		private final android.bluetooth.le.ScanCallback nativeCallback = new android.bluetooth.le.ScanCallback() {
			private long lastBatchTimestamp;

			@Override
			public void onScanResult(final int callbackType, final android.bluetooth.le.ScanResult nativeScanResult) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						final BluetoothLeScannerImplLollipop scannerImpl =
								(BluetoothLeScannerImplLollipop) BluetoothLeScannerCompat.getScanner();
						final ScanResult result = scannerImpl.fromNativeScanResult(nativeScanResult);
						handleScanResult(callbackType, result);
					}
				});
			}

			@Override
			public void onBatchScanResults(final List<android.bluetooth.le.ScanResult> nativeScanResults) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// On several phones the onBatchScanResults is called twice for every batch.
						// Skip the second call if came to early.
						final long now = SystemClock.elapsedRealtime();
						if (lastBatchTimestamp > now - scanSettings.getReportDelayMillis() + 5) {
							return;
						}
						lastBatchTimestamp = now;

						final BluetoothLeScannerImplLollipop scannerImpl =
								(BluetoothLeScannerImplLollipop) BluetoothLeScannerCompat.getScanner();
						final List<ScanResult> results = scannerImpl.fromNativeScanResults(nativeScanResults);
						handleScanResults(results);
					}
				});
			}

			@Override
			public void onScanFailed(final int errorCode) {
				handler.post(new Runnable() {
					@Override
					@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
					public void run() {
						// We were able to determine offloaded batching and filtering before we started scan,
						// but there is no method checking if callback types FIRST_MATCH and MATCH_LOST
						// are supported. We get an error here it they are not.
						if (scanSettings.getUseHardwareCallbackTypesIfSupported()
								&& scanSettings.getCallbackType() != ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
							// On Nexus 6 with Android 6.0 (MPA44G, M Pre-release 3) the errorCode = 5 (SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES)
							// On Pixel 2 with Android 9.0 the errorCode = 4 (SCAN_FAILED_FEATURE_UNSUPPORTED)

							// This feature seems to be not supported on your phone.
							// Let's try to do pretty much the same in the code.
							scanSettings.disableUseHardwareCallbackTypes();

							final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
							try {
								scanner.stopScan(scanCallback);
							} catch (final Exception e) {
								// Ignore
							}
							try {
								scanner.startScanInternal(filters, scanSettings, scanCallback, handler);
							} catch (final Exception e) {
								// Ignore
							}
							return;
						}

						// else, notify user application
						handleScanError(errorCode);
					}
				});
			}
		};
	}
}