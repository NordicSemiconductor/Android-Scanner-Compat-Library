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

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class ScanResult implements Parcelable {
	// Remote bluetooth device.
	private BluetoothDevice mDevice;

	// Scan record, including advertising data and scan response data.
	@Nullable
	private ScanRecord mScanRecord;

	// Received signal strength.
	private int mRssi;

	// Device timestamp when the result was last seen.
	private long mTimestampNanos;

	/**
	 * Constructor of scan result.
	 *
	 * @param device Remote bluetooth device that is found.
	 * @param scanRecord Scan record including both advertising data and scan response data.
	 * @param rssi Received signal strength.
	 * @param timestampNanos Device timestamp when the scan result was observed.
	 */
	public ScanResult(BluetoothDevice device, @Nullable ScanRecord scanRecord, int rssi,
					  long timestampNanos) {
		mDevice = device;
		mScanRecord = scanRecord;
		mRssi = rssi;
		mTimestampNanos = timestampNanos;
	}

	private ScanResult(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (mDevice != null) {
			dest.writeInt(1);
			mDevice.writeToParcel(dest, flags);
		} else {
			dest.writeInt(0);
		}
		if (mScanRecord != null) {
			dest.writeInt(1);
			dest.writeByteArray(mScanRecord.getBytes());
		} else {
			dest.writeInt(0);
		}
		dest.writeInt(mRssi);
		dest.writeLong(mTimestampNanos);
	}

	private void readFromParcel(Parcel in) {
		if (in.readInt() == 1) {
			mDevice = BluetoothDevice.CREATOR.createFromParcel(in);
		}
		if (in.readInt() == 1) {
			mScanRecord = ScanRecord.parseFromBytes(in.createByteArray());
		}
		mRssi = in.readInt();
		mTimestampNanos = in.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Returns the remote bluetooth device identified by the bluetooth device address.
	 */
	public BluetoothDevice getDevice() {
		return mDevice;
	}

	/**
	 * Returns the scan record, which is a combination of advertisement and scan response.
	 */
	@Nullable
	public ScanRecord getScanRecord() {
		return mScanRecord;
	}

	/**
	 * Returns the received signal strength in dBm. The valid range is [-127, 127].
	 */
	public int getRssi() {
		return mRssi;
	}

	/**
	 * Returns timestamp since boot when the scan record was observed.
	 */
	public long getTimestampNanos() {
		return mTimestampNanos;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mDevice, mRssi, mScanRecord, mTimestampNanos);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ScanResult other = (ScanResult) obj;
		return Objects.equals(mDevice, other.mDevice) && (mRssi == other.mRssi) &&
				Objects.equals(mScanRecord, other.mScanRecord)
				&& (mTimestampNanos == other.mTimestampNanos);
	}

	@Override
	public String toString() {
		return "ScanResult{" + "mDevice=" + mDevice + ", mScanRecord="
				+ Objects.toString(mScanRecord) + ", mRssi=" + mRssi + ", mTimestampNanos="
				+ mTimestampNanos + '}';
	}

	public static final Creator<ScanResult> CREATOR = new Creator<ScanResult>() {
		@Override
		public ScanResult createFromParcel(Parcel source) {
			return new ScanResult(source);
		}

		@Override
		public ScanResult[] newArray(int size) {
			return new ScanResult[size];
		}
	};
}
