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

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.support.annotation.NonNull;

@TargetApi(Build.VERSION_CODES.O)
/* package */ class BluetoothLeScannerImplOreo extends BluetoothLeScannerImplMarshmallow {

	@NonNull
    @Override
	/* package */ android.bluetooth.le.ScanSettings toNativeScanSettings(@NonNull final BluetoothAdapter adapter,
																		 @NonNull final ScanSettings settings) {
		final android.bluetooth.le.ScanSettings.Builder builder =
				new android.bluetooth.le.ScanSettings.Builder().setScanMode(settings.getScanMode());

		if (adapter.isOffloadedScanBatchingSupported() && settings.getUseHardwareBatchingIfSupported())
			builder.setReportDelay(settings.getReportDelayMillis());

		if (settings.getUseHardwareCallbackTypesIfSupported())
			builder.setCallbackType(settings.getCallbackType())
					.setMatchMode(settings.getMatchMode())
					.setNumOfMatches(settings.getNumOfMatches());

		builder.setLegacy(settings.getLegacy());
		builder.setPhy(settings.getPhy());

		return builder.build();
	}

	@NonNull
    @Override
	/* package */ ScanResult fromNativeScanResult(@NonNull final android.bluetooth.le.ScanResult _result) {
		// Calculate the important bits of Event Type
		final int eventType = (_result.getDataStatus() << 5)
				| (_result.isLegacy() ? ScanResult.ET_LEGACY_MASK : 0)
				| (_result.isConnectable() ? ScanResult.ET_CONNECTABLE_MASK : 0);
		// Get data as bytes
		final byte[] data = _result.getScanRecord() != null ? _result.getScanRecord().getBytes() : null;
		// And return the v18.ScanResult
		return new ScanResult(_result.getDevice(), eventType, _result.getPrimaryPhy(),
				_result.getSecondaryPhy(), _result.getAdvertisingSid(),
				_result.getTxPower(), _result.getRssi(),
				_result.getPeriodicAdvertisingInterval(),
				ScanRecord.parseFromBytes(data), _result.getTimestampNanos());
	}
}
