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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* package */
@SuppressWarnings("deprecation")
class BluetoothLeScannerImplJB extends BluetoothLeScannerCompat {

	@NonNull private final Map<ScanCallback, ScanCallbackWrapper> mWrappers = new HashMap<>();
	@Nullable private HandlerThread mHandlerThread;
	@Nullable private Handler mHandler;

	private long mPowerSaveRestInterval;
	private long mPowerSaveScanInterval;

	private final Runnable mPowerSaveSleepRunnable = new Runnable() {
		@SuppressWarnings("deprecation")
		@Override
		@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
		public void run() {
			final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (adapter != null && mPowerSaveRestInterval > 0 && mPowerSaveScanInterval > 0) {
				adapter.stopLeScan(mCallback);
				mHandler.postDelayed(mPowerSaveScanRunnable, mPowerSaveRestInterval);
			}
		}
	};

	private final Runnable mPowerSaveScanRunnable = new Runnable() {
		@SuppressWarnings("deprecation")
		@Override
		@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
		public void run() {
			final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (adapter != null && mPowerSaveRestInterval > 0 && mPowerSaveScanInterval > 0) {
				adapter.startLeScan(mCallback);
				mHandler.postDelayed(mPowerSaveSleepRunnable, mPowerSaveScanInterval);
			}
		}
	};

	/* package */ BluetoothLeScannerImplJB() {}

	@Override
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	@SuppressWarnings("deprecation")
	/* package */ void startScanInternal(@NonNull final List<ScanFilter> filters,
										 @NonNull final ScanSettings settings,
										 @NonNull final ScanCallback callback,
										 @NonNull final Handler handler) {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothLeUtils.checkAdapterStateOn(adapter);

		boolean shouldStart;

		synchronized (mWrappers) {
			if (mWrappers.containsKey(callback)) {
				throw new IllegalArgumentException("scanner already started with given callback");
			}
			final ScanCallbackWrapper wrapper = new ScanCallbackWrapper(filters, settings, callback, handler);
			shouldStart = mWrappers.isEmpty();
			mWrappers.put(callback, wrapper);
		}

		setPowerSaveSettings();

		if (shouldStart) {
			if (mHandlerThread == null) {
				mHandlerThread = new HandlerThread(BluetoothLeScannerImplJB.class.getName());
				mHandlerThread.start();
				mHandler = new Handler(mHandlerThread.getLooper());
			}

			adapter.startLeScan(mCallback);
		}
	}

	private void setPowerSaveSettings() {
		long minRest = Long.MAX_VALUE, minScan = Long.MAX_VALUE;
		synchronized (mWrappers) {
			for (final ScanCallbackWrapper wrapper : mWrappers.values()) {
				final ScanSettings settings = wrapper.mScanSettings;
				if (settings.hasPowerSaveMode()) {
					if (minRest > settings.getPowerSaveRest()) {
						minRest = settings.getPowerSaveRest();
					}
					if (minScan > settings.getPowerSaveScan()) {
						minScan = settings.getPowerSaveScan();
					}
				}
			}
		}
		if (minRest < Long.MAX_VALUE && minScan < Long.MAX_VALUE) {
			mPowerSaveRestInterval = minRest;
			mPowerSaveScanInterval = minScan;
			mHandler.removeCallbacks(mPowerSaveScanRunnable);
			mHandler.removeCallbacks(mPowerSaveSleepRunnable);
			mHandler.postDelayed(mPowerSaveSleepRunnable, mPowerSaveScanInterval);
		} else {
			mPowerSaveRestInterval = mPowerSaveScanInterval = 0;
			mHandler.removeCallbacks(mPowerSaveScanRunnable);
			mHandler.removeCallbacks(mPowerSaveSleepRunnable);
		}
	}

	@Override
	@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
	@SuppressWarnings("deprecation")
	public void stopScan(@NonNull final ScanCallback callback) {
		boolean shouldStop;
		ScanCallbackWrapper wrapper;
		synchronized (mWrappers) {
			wrapper = mWrappers.get(callback);
			if (wrapper == null)
				return;

			mWrappers.remove(callback);
			shouldStop = mWrappers.isEmpty();
		}

		wrapper.close();

		setPowerSaveSettings();

		if (shouldStop) {
			final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (adapter != null) {
				adapter.stopLeScan(mCallback);
			}

			if (mHandler != null) {
				mHandler.removeCallbacksAndMessages(null);
			}

			if (mHandlerThread != null) {
				mHandlerThread.quitSafely();
				mHandlerThread = null;
			}
		}
	}

	@Override
	@RequiresPermission(Manifest.permission.BLUETOOTH)
	public void flushPendingScanResults(@NonNull final ScanCallback callback) {
		BluetoothLeUtils.checkAdapterStateOn(BluetoothAdapter.getDefaultAdapter());
		//noinspection ConstantConditions
		if (callback == null) {
			throw new IllegalArgumentException("callback cannot be null!");
		}
		ScanCallbackWrapper wrapper;
		synchronized (mWrappers) {
			wrapper = mWrappers.get(callback);
		}

		if (wrapper != null) {
			wrapper.flushPendingScanResults();
		}
	}

	private final BluetoothAdapter.LeScanCallback mCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			final ScanResult scanResult = new ScanResult(device, ScanRecord.parseFromBytes(scanRecord), rssi, SystemClock.elapsedRealtimeNanos());

			synchronized (mWrappers) {
				final Collection<ScanCallbackWrapper> wrappers = mWrappers.values();
				for (final ScanCallbackWrapper wrapper : wrappers) {
					wrapper.mHandler.post(new Runnable() {
						@Override
						public void run() {
							wrapper.handleScanResult(scanResult);
						}
					});
				}
			}
		}
	};
}