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
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
/* package */ class BluetoothLeScannerImplLollipop extends BluetoothLeScannerCompat {
	private final BluetoothAdapter mBluetoothAdapter;
	private final Map<ScanCallback, ScanCallbackWrapper> mWrappers; // used to get settings
	private final Map<ScanCallback, android.bluetooth.le.ScanCallback> mCallbacks; // used to stop scanning and flash pending results
	private final Map<android.bluetooth.le.ScanCallback, ScanCallbackWrapper> mWrappers2; // used to get wrapper in scan result callback
	private boolean offloadedFilteringSupported;

	public BluetoothLeScannerImplLollipop() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mWrappers = new HashMap<>();
		mWrappers2 = new HashMap<>();
		mCallbacks = new HashMap<>();
	}

	@Override
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	/* package */ void startScanInternal(final List<ScanFilter> filters, final ScanSettings settings, final ScanCallback callback) {
		BluetoothLeUtils.checkAdapterStateOn(mBluetoothAdapter);
		offloadedFilteringSupported = mBluetoothAdapter.isOffloadedFilteringSupported();

		if (mWrappers.containsKey(callback)) {
			throw new IllegalArgumentException("scanner already started with given callback");
		}

		final BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
		if (scanner == null)
			throw new IllegalStateException("BT le scanner not available");

		final ScanCallbackWrapper wrapper = new ScanCallbackWrapper(filters, settings, callback);
		final ScanCallbackImpl _callback = new ScanCallbackImpl();
		final android.bluetooth.le.ScanSettings _settings = toImpl(mBluetoothAdapter, settings);
		List<android.bluetooth.le.ScanFilter> _filters = null;
		if (filters != null && mBluetoothAdapter.isOffloadedFilteringSupported() && settings.getUseHardwareFilteringIfSupported())
			_filters = toImpl(filters);

		mWrappers.put(callback, wrapper);
		mCallbacks.put(callback, _callback);
		mWrappers2.put(_callback, wrapper);

		scanner.startScan(_filters, _settings, _callback);
	}

	@Override
	public void stopScan(final ScanCallback callback) {
		final ScanCallbackWrapper wrapper = mWrappers.get(callback);
		if (wrapper == null)
			return;

		wrapper.close();
		mWrappers.remove(callback);
		android.bluetooth.le.ScanCallback _callback = mCallbacks.get(callback);
		mCallbacks.remove(callback);
		mWrappers2.remove(_callback);

		final BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
		if (scanner == null)
			return;

		scanner.stopScan(_callback);
	}

	@Override
	@RequiresPermission(Manifest.permission.BLUETOOTH)
	public void flushPendingScanResults(final ScanCallback callback) {
		BluetoothLeUtils.checkAdapterStateOn(mBluetoothAdapter);
		if (callback == null) {
			throw new IllegalArgumentException("callback cannot be null!");
		}

		final ScanCallbackWrapper wrapper = mWrappers.get(callback);
		if (wrapper == null) {
			throw new IllegalArgumentException("callback not registered!");
		}

		final ScanSettings settings = wrapper.getScanSettings();
		if (mBluetoothAdapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported())
			mBluetoothAdapter.getBluetoothLeScanner().flushPendingScanResults(mCallbacks.get(callback));
		else
			mWrappers.get(callback).flushPendingScanResults();
	}

	private class ScanCallbackImpl extends android.bluetooth.le.ScanCallback {
		private long mLastBatchTimestamp;

		@Override
		public void onScanResult(final int callbackType, final android.bluetooth.le.ScanResult _result) {
			final ScanCallbackWrapper wrapper = mWrappers2.get(this);
			if (wrapper != null) {
				final byte[] data = _result.getScanRecord() != null ? _result.getScanRecord().getBytes() : null;
				final ScanResult result = new ScanResult(_result.getDevice(), ScanRecord.parseFromBytes(data), _result.getRssi(), _result.getTimestampNanos());

				wrapper.handleScanResult(result);
			}
		}

		@Override
		public void onBatchScanResults(final List<android.bluetooth.le.ScanResult> _results) {
			final ScanCallbackWrapper wrapper = mWrappers2.get(this);
			if (wrapper != null) {
				// On several phones the onBatchScanResults is called twice for every batch. Skip the second call if came to early.
				final long now = SystemClock.elapsedRealtime();
				if (mLastBatchTimestamp > now - wrapper.getScanSettings().getReportDelayMillis() + 5) {
					return;
				}
				mLastBatchTimestamp = now;

				final List<ScanResult> results = new ArrayList<>();
				for (final android.bluetooth.le.ScanResult _result : _results) {
					final byte[] data = _result.getScanRecord() != null ? _result.getScanRecord().getBytes() : null;
					final ScanResult result = new ScanResult(_result.getDevice(), ScanRecord.parseFromBytes(data), _result.getRssi(), _result.getTimestampNanos());
					results.add(result);
				}

				wrapper.handleScanResults(results, offloadedFilteringSupported);
			}
		}

		@Override
		@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
		public void onScanFailed(final int errorCode) {
			final ScanCallbackWrapper wrapper = mWrappers2.get(this);
			if (wrapper == null)
				return;

			final ScanSettings settings = wrapper.getScanSettings();

			// We were able to determine offloaded batching and filtering before we started scan, but there is no method
			// checking if callback types FIRST_MATCH and MATCH_LOST are supported. We get an error here it they are not.
			if (settings.getUseHardwareCallbackTypesIfSupported() && settings.getCallbackType() != ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
				// On Nexus 6 with Android 6.0 (MPA44G, M Pre-release 3) the errorCode = 5

				// This feature seems to be not supported on your phone. We will try to do pretty much the same in the code.
				settings.disableUseHardwareCallbackTypes();

				final ScanCallback callback = wrapper.getScanCallback();
				stopScan(callback);
				startScanInternal(wrapper.getScanFilters(), settings, callback);
				return;
			}

			// else, notify user application
			wrapper.onScanManagerErrorCallback(errorCode);
		}
	}

	/* package */ android.bluetooth.le.ScanSettings toImpl(@NonNull final BluetoothAdapter adapter, @NonNull final ScanSettings settings) {
		final android.bluetooth.le.ScanSettings.Builder builder = new android.bluetooth.le.ScanSettings.Builder().setScanMode(settings.getScanMode());

		if (adapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported())
			builder.setReportDelay(settings.getReportDelayMillis());

		settings.disableUseHardwareCallbackTypes(); // callback types other then CALLBACK_TYPE_ALL_MATCHES are not supported on Lollipop

		return builder.build();
	}

	/* package */ List<android.bluetooth.le.ScanFilter> toImpl(final @NonNull List<ScanFilter> filters) {
		final List<android.bluetooth.le.ScanFilter> _filters = new ArrayList<>();
		for (final ScanFilter filter : filters)
			_filters.add(toImpl(filter));
		return _filters;
	}

	/* package */ android.bluetooth.le.ScanFilter toImpl(final ScanFilter filter) {
		final android.bluetooth.le.ScanFilter.Builder builder = new android.bluetooth.le.ScanFilter.Builder();
		builder.setDeviceAddress(filter.getDeviceAddress())
				.setDeviceName(filter.getDeviceName())
				.setServiceUuid(filter.getServiceUuid(), filter.getServiceUuidMask())
				.setManufacturerData(filter.getManufacturerId(), filter.getManufacturerData(), filter.getManufacturerDataMask());

		if (filter.getServiceDataUuid() != null)
			builder.setServiceData(filter.getServiceDataUuid(), filter.getServiceData(), filter.getServiceDataMask());

		return builder.build();
	}
}
