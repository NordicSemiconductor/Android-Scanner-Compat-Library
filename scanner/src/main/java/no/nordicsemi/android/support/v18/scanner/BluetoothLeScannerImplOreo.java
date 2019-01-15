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
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.O)
/* package */ class BluetoothLeScannerImplOreo extends BluetoothLeScannerImplMarshmallow {

	@NonNull private final Map<PendingIntent, ScanCallbackWrapperOreo> wrappers = new HashMap<>();

	@Nullable
	/* package */ ScanCallbackWrapperOreo getWrapper(@NonNull final PendingIntent pendingIntent) {
	    synchronized (wrappers) {
            return wrappers.get(pendingIntent);
        }
	}

	@Override
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	/* package */ void startScanInternal(@Nullable final List<ScanFilter> filters,
										 @Nullable final ScanSettings settings,
										 @NonNull  final Context context,
										 @NonNull  final PendingIntent callbackIntent) {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothLeUtils.checkAdapterStateOn(adapter);

		final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
		if (scanner == null)
			throw new IllegalStateException("BT le scanner not available");

		final boolean offloadedBatchingSupported = adapter.isOffloadedScanBatchingSupported();
		final boolean offloadedFilteringSupported = adapter.isOffloadedFilteringSupported();

		final ScanSettings nonNullSettings = settings != null ? settings : new ScanSettings.Builder().build();
		final List<ScanFilter> nonNullFilters = filters != null ? filters : Collections.<ScanFilter>emptyList();

		ScanCallbackWrapperOreo wrapper;
		synchronized (wrappers) {
			if (wrappers.containsKey(callbackIntent))
				throw new IllegalArgumentException("scanner already started with given callback intent");
			wrapper = new ScanCallbackWrapperOreo(offloadedBatchingSupported, offloadedFilteringSupported,
					nonNullFilters, nonNullSettings, context, callbackIntent);
			wrappers.put(callbackIntent, wrapper);
		}

		final android.bluetooth.le.ScanSettings nativeSettings = toNativeScanSettings(adapter, nonNullSettings);
		List<android.bluetooth.le.ScanFilter> nativeFilters = null;
		if (filters != null && adapter.isOffloadedFilteringSupported() && nonNullSettings.getUseHardwareFilteringIfSupported())
			nativeFilters = toNativeScanFilters(filters);
		scanner.startScan(nativeFilters, nativeSettings, wrapper.nativePendingIntent);
	}

	@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
	/* package */ void stopScanInternal(@NonNull final Context context,
										@NonNull final PendingIntent callbackIntent) {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothLeUtils.checkAdapterStateOn(adapter);

		final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
		if (scanner == null)
			throw new IllegalStateException("BT le scanner not available");

		ScanCallbackWrapperOreo wrapper;
		synchronized (wrappers) {
			wrapper = wrappers.remove(callbackIntent);
		}
		if (wrapper == null)
			return;

		scanner.stopScan(wrapper.nativePendingIntent);
	}

	@NonNull
	@Override
	/* package */ android.bluetooth.le.ScanSettings toNativeScanSettings(@NonNull final BluetoothAdapter adapter,
																		 @NonNull final ScanSettings settings) {
		final android.bluetooth.le.ScanSettings.Builder builder =
				new android.bluetooth.le.ScanSettings.Builder();

		if (adapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported())
			builder.setReportDelay(settings.getReportDelayMillis());

		if (settings.getUseHardwareCallbackTypesIfSupported())
			builder.setCallbackType(settings.getCallbackType())
					.setMatchMode(settings.getMatchMode())
					.setNumOfMatches(settings.getNumOfMatches());

		builder.setScanMode(settings.getScanMode())
				.setLegacy(settings.getLegacy())
				.setPhy(settings.getPhy());

		return builder.build();
	}

	@NonNull
	@Override
	/* package */ ScanResult fromNativeScanResult(@NonNull final android.bluetooth.le.ScanResult result) {
		// Calculate the important bits of Event Type
		final int eventType = (result.getDataStatus() << 5)
				| (result.isLegacy() ? ScanResult.ET_LEGACY_MASK : 0)
				| (result.isConnectable() ? ScanResult.ET_CONNECTABLE_MASK : 0);
		// Get data as bytes
		final byte[] data = result.getScanRecord() != null ? result.getScanRecord().getBytes() : null;
		// And return the v18.ScanResult
		return new ScanResult(result.getDevice(), eventType, result.getPrimaryPhy(),
				result.getSecondaryPhy(), result.getAdvertisingSid(),
				result.getTxPower(), result.getRssi(),
				result.getPeriodicAdvertisingInterval(),
				ScanRecord.parseFromBytes(data), result.getTimestampNanos());
	}

	/* package */ static class ScanCallbackWrapperOreo extends ScanCallbackWrapper {
		@NonNull final PendingIntent nativePendingIntent;
		@NonNull final PendingIntentExecutor executor;

		ScanCallbackWrapperOreo(final boolean offloadedBatchingSupported,
								final boolean offloadedFilteringSupported,
								@NonNull final List<ScanFilter> filters,
								@NonNull final ScanSettings settings,
								@NonNull final Context context,
								@NonNull final PendingIntent callbackIntent) {
			super(offloadedBatchingSupported, offloadedFilteringSupported, filters, settings,
					new PendingIntentExecutor(callbackIntent), new Handler(Looper.getMainLooper()));
			executor = (PendingIntentExecutor) scanCallback;

			final Intent intent = new Intent(PendingIntentReceiver.ACTION);
			intent.putExtra(PendingIntentReceiver.EXTRA_PENDING_INTENT, callbackIntent);
			nativePendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		}
	}
}
