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

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Bluetooth LE scan callbacks. Scan results are reported using these callbacks.
 *
 * @see BluetoothLeScannerCompat#startScan
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class ScanCallback {
	/**
	 * Fails to start scan as BLE scan with the same settings is already started by the app.
	 */
	public static final int SCAN_FAILED_ALREADY_STARTED = 1;

	/**
	 * Fails to start scan as app cannot be registered.
	 */
	public static final int SCAN_FAILED_APPLICATION_REGISTRATION_FAILED = 2;

	/**
	 * Fails to start scan due an internal error
	 */
	public static final int SCAN_FAILED_INTERNAL_ERROR = 3;

	/**
	 * Fails to start power optimized scan as this feature is not supported.
	 */
	public static final int SCAN_FAILED_FEATURE_UNSUPPORTED = 4;

	/**
	 * Fails to start scan as it is out of hardware resources.
	 */
	public static final int SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES = 5;

	/**
	 * Fails to start scan as application tries to scan too frequently.
	 */
	public static final int SCAN_FAILED_SCANNING_TOO_FREQUENTLY = 6;

	static final int NO_ERROR = 0;

	/**
	 * Callback when a BLE advertisement has been found.
	 *
	 * @param callbackType Determines how this callback was triggered. Could be one of
	 *            {@link ScanSettings#CALLBACK_TYPE_ALL_MATCHES},
	 *            {@link ScanSettings#CALLBACK_TYPE_FIRST_MATCH} or
	 *            {@link ScanSettings#CALLBACK_TYPE_MATCH_LOST}
	 * @param result A Bluetooth LE scan result.
	 */
	public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
	}

	/**
	 * Callback when batch results are delivered.
	 *
	 * @param results List of scan results that are previously scanned.
	 */
	public void onBatchScanResults(@NonNull final List<ScanResult> results) {
	}

	/**
	 * Callback when scan could not be started.
	 *
	 * @param errorCode Error code (one of SCAN_FAILED_*) for scan failure.
	 */
	public void onScanFailed(final int errorCode) {
	}
}
