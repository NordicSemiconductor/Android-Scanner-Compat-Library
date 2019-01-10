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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"deprecation", "WeakerAccess"})
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
/* package */ class BluetoothLeScannerImplLollipop extends BluetoothLeScannerCompat {

	private final Map<ScanCallback, ScanCallbackWrapperLollipop> mWrappers = new HashMap<>();

	/* package */ BluetoothLeScannerImplLollipop() {}

	@Override
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	/* package */ void startScanInternal(@NonNull final List<ScanFilter> filters,
										 @NonNull final ScanSettings settings,
										 @NonNull final ScanCallback callback,
										 @NonNull final Handler handler) {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothLeUtils.checkAdapterStateOn(adapter);

		final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
		if (scanner == null)
			throw new IllegalStateException("BT le scanner not available");

		boolean offloadedFilteringSupported = adapter.isOffloadedFilteringSupported();

		ScanCallbackWrapperLollipop wrapper;

		synchronized (mWrappers) {
			if (mWrappers.containsKey(callback)) {
				throw new IllegalArgumentException("scanner already started with given callback");
			}
			wrapper = new ScanCallbackWrapperLollipop(filters, settings, callback, handler, offloadedFilteringSupported);
			mWrappers.put(callback, wrapper);
		}

		final android.bluetooth.le.ScanSettings nativeScanSettings = toNativeScanSettings(adapter, settings);
		List<android.bluetooth.le.ScanFilter> nativeScanFilters = null;
		if (!filters.isEmpty() && adapter.isOffloadedFilteringSupported() && settings.getUseHardwareFilteringIfSupported())
			nativeScanFilters = toNativeScanFilters(filters);

		scanner.startScan(nativeScanFilters, nativeScanSettings, wrapper.mNativeCallback);
	}

	@Override
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	public void stopScan(@NonNull final ScanCallback callback) {
		ScanCallbackWrapperLollipop wrapper;
		synchronized (mWrappers) {
			wrapper = mWrappers.get(callback);
			if (wrapper == null) {
				return;
			}
			mWrappers.remove(callback);
		}

		wrapper.close();

		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null)
			return;

		final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
		if (scanner == null)
			return;

		scanner.stopScan(wrapper.mNativeCallback);
	}

	@Override
	@RequiresPermission(Manifest.permission.BLUETOOTH)
	public void flushPendingScanResults(@NonNull final ScanCallback callback) {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothLeUtils.checkAdapterStateOn(adapter);
		//noinspection ConstantConditions
		if (callback == null) {
			throw new IllegalArgumentException("callback cannot be null!");
		}

		ScanCallbackWrapperLollipop wrapper;
		synchronized (mWrappers) {
			wrapper = mWrappers.get(callback);
		}

		if (wrapper == null) {
			throw new IllegalArgumentException("callback not registered!");
		}

		final ScanSettings settings = wrapper.mScanSettings;
		if (adapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported()) {
			final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
			if (scanner == null)
				return;
			scanner.flushPendingScanResults(wrapper.mNativeCallback);
		} else {
			wrapper.flushPendingScanResults();
		}
	}

	@NonNull
	/* package */ android.bluetooth.le.ScanSettings toNativeScanSettings(@NonNull final BluetoothAdapter adapter, @NonNull final ScanSettings settings) {
		final android.bluetooth.le.ScanSettings.Builder builder = new android.bluetooth.le.ScanSettings.Builder().setScanMode(settings.getScanMode());

		if (adapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported())
			builder.setReportDelay(settings.getReportDelayMillis());

		settings.disableUseHardwareCallbackTypes(); // callback types other then CALLBACK_TYPE_ALL_MATCHES are not supported on Lollipop

		return builder.build();
	}

	@NonNull
	/* package */ List<android.bluetooth.le.ScanFilter> toNativeScanFilters(@NonNull final List<ScanFilter> filters) {
		final List<android.bluetooth.le.ScanFilter> nativeScanFilters = new ArrayList<>();
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

	private final class ScanCallbackWrapperLollipop extends ScanCallbackWrapper {

		private final boolean mOffloadedFilteringSupported;
		private long mLastBatchTimestamp;

		private ScanCallbackWrapperLollipop(@NonNull final List<ScanFilter> filters, @NonNull final ScanSettings settings, @NonNull final ScanCallback callback, @NonNull final Handler handler, final boolean offloadedFilteringSupported) {
			super(filters, settings, callback, handler);
			this.mOffloadedFilteringSupported = offloadedFilteringSupported;
		}

		@NonNull
		private final android.bluetooth.le.ScanCallback mNativeCallback = new android.bluetooth.le.ScanCallback() {
			@Override
			public void onScanResult(final int callbackType, final android.bluetooth.le.ScanResult nativeScanResult) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						final ScanResult result = fromNativeScanResult(nativeScanResult);
						handleScanResult(result);
					}
				});
			}

			@Override
			public void onBatchScanResults(final List<android.bluetooth.le.ScanResult> nativeScanResults) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// On several phones the onBatchScanResults is called twice for every batch.
						// Skip the second call if came to early.
						final long now = SystemClock.elapsedRealtime();
						if (mLastBatchTimestamp > now - mScanSettings.getReportDelayMillis() + 5) {
							return;
						}
						mLastBatchTimestamp = now;

						final List<ScanResult> results = new ArrayList<>();
						for (final android.bluetooth.le.ScanResult nativeScanResult : nativeScanResults) {
							final ScanResult result = fromNativeScanResult(nativeScanResult);
							results.add(result);
						}

						handleScanResults(results, mOffloadedFilteringSupported);
					}
				});
			}

			@Override
			public void onScanFailed(final int errorCode) {
				mHandler.post(new Runnable() {
					@Override
					@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
					public void run() {
						// We were able to determine offloaded batching and filtering before we started scan,
						// but there is no method checking if callback types FIRST_MATCH and MATCH_LOST
						// are supported. We get an error here it they are not.
						if (mScanSettings.getUseHardwareCallbackTypesIfSupported()
								&& mScanSettings.getCallbackType() != ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
							// On Nexus 6 with Android 6.0 (MPA44G, M Pre-release 3) the errorCode = 5

							// This feature seems to be not supported on your phone.
							// Let's try to do pretty much the same in the code.
							mScanSettings.disableUseHardwareCallbackTypes();
							stopScan(mScanCallback);
							startScanInternal(mFilters, mScanSettings, mScanCallback, mHandler);
							return;
						}

						// else, notify user application
						onScanManagerErrorCallback(errorCode);
					}
				});
			}
		};
	}

	@NonNull
		/* package */ ScanResult fromNativeScanResult(@NonNull final android.bluetooth.le.ScanResult nativeScanResult) {
		final byte[] data = nativeScanResult.getScanRecord() != null ? nativeScanResult.getScanRecord().getBytes() : null;
		return new ScanResult(nativeScanResult.getDevice(), ScanRecord.parseFromBytes(data), nativeScanResult.getRssi(), nativeScanResult.getTimestampNanos());
	}
}