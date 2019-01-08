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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.List;

@TargetApi(Build.VERSION_CODES.O)
/* package */ class BluetoothLeScannerImplOreo extends BluetoothLeScannerImplMarshmallow {

	/**
	 * Start Bluetooth LE scan using a {@link PendingIntent}. The scan results will be delivered
	 * via the PendingIntent. Use this method of scanning if your process is not always running
	 * and it should be started when scan results are available.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 * An app must hold
	 * {@link Manifest.permission#ACCESS_COARSE_LOCATION ACCESS_FINE_LOCATION} permission
	 * in order to get results.
	 * <p>
	 * When the PendingIntent is delivered, the Intent passed to the receiver or activity will
	 * contain one or more of the extras {@link #EXTRA_CALLBACK_TYPE}, {@link #EXTRA_ERROR_CODE} and
	 * {@link #EXTRA_LIST_SCAN_RESULT} to indicate the result of the scan.
	 * <p>
	 * Scanning using {@link PendingIntent} on Android Oreo or newer may ignore some settings.
	 * For example, {@link ScanSettings.Builder#setUseHardwareBatchingIfSupported(boolean)},
	 * {@link ScanSettings.Builder#setUseHardwareCallbackTypesIfSupported(boolean)} and
	 * {@link ScanSettings.Builder#setUseHardwareFilteringIfSupported(boolean)} will all be set to
	 * true.
	 *
	 * @param filters        {@link ScanFilter}s for finding exact BLE devices.
	 * @param settings       Optional settings for the scan.
	 * @param context        Unused. Used only before Android Oreo.
	 * @param callbackIntent The PendingIntent to deliver the result to.
	 * @throws IllegalArgumentException If {@code settings} or {@code callback} is null.
	 */
	@Override
	@RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
	public void startScan(@Nullable final List<ScanFilter> filters,
						  @Nullable final ScanSettings settings,
						  @NonNull  final Context context,
						  @NonNull  final PendingIntent callbackIntent) {
		//noinspection ConstantConditions
		if (callbackIntent == null) {
			throw new IllegalArgumentException("callbackIntent is null");
		}
		final BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		BluetoothLeUtils.checkAdapterStateOn(ba);

		final BluetoothLeScanner scanner = ba.getBluetoothLeScanner();
		if (scanner == null)
			throw new IllegalStateException("BT le scanner not available");

		final ScanSettings nonNullSettings = settings != null ? settings : new ScanSettings.Builder().build();
		final android.bluetooth.le.ScanSettings nativeSettings = toImpl(ba, nonNullSettings);
		List<android.bluetooth.le.ScanFilter> nativeFilters = null;
		if (filters != null && ba.isOffloadedFilteringSupported() && nonNullSettings.getUseHardwareFilteringIfSupported())
            nativeFilters = toImpl(filters);

		scanner.startScan(nativeFilters, nativeSettings, callbackIntent);
	}

	/**
	 * Stops an ongoing Bluetooth LE scan.
	 * <p>
	 * Requires {@link Manifest.permission#BLUETOOTH_ADMIN} permission.
	 *
	 * @param context        Unused. Used only before Android Oreo.
	 * @param callbackIntent The PendingIntent that was used to start the scan.
	 */
	@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
	public void stopScan(@NonNull final Context context,
						 @NonNull final PendingIntent callbackIntent) {
		//noinspection ConstantConditions
		if (callbackIntent == null) {
			throw new IllegalArgumentException("callbackIntent is null");
		}

		final BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		if (ba == null)
			return;
		final BluetoothLeScanner scanner = ba.getBluetoothLeScanner();
		if (scanner == null)
			return;

		scanner.stopScan(callbackIntent);
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
}
