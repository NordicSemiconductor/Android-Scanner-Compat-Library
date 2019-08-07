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

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ScanResult for Bluetooth LE scan.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class ScanResult implements Parcelable {

	/**
	 * For chained advertisements, indicates that the data contained in this
	 * scan result is complete.
	 */
	public static final int DATA_COMPLETE = 0x00;

	/**
	 * For chained advertisements, indicates that the controller was
	 * unable to receive all chained packets and the scan result contains
	 * incomplete truncated data.
	 */
	public static final int DATA_TRUNCATED = 0x02;

	/**
	 * Indicates that the secondary physical layer was not used.
	 */
	public static final int PHY_UNUSED = 0x00;

	/**
	 * Advertising Set ID is not present in the packet.
	 */
	public static final int SID_NOT_PRESENT = 0xFF;

	/**
	 * TX power is not present in the packet.
	 */
	public static final int TX_POWER_NOT_PRESENT = 0x7F;

	/**
	 * Periodic advertising interval is not present in the packet.
	 */
	public static final int PERIODIC_INTERVAL_NOT_PRESENT = 0x00;

	/**
	 * Mask for checking whether event type represents legacy advertisement.
	 */
	static final int ET_LEGACY_MASK = 0x10;

	/**
	 * Mask for checking whether event type represents connectable advertisement.
	 */
	static final int ET_CONNECTABLE_MASK = 0x01;

	// Remote Bluetooth device.
	@SuppressWarnings("NullableProblems")
	@NonNull
	private BluetoothDevice device;

	// Scan record, including advertising data and scan response data.
	@Nullable
	private ScanRecord scanRecord;

	// Received signal strength.
	private int rssi;

	// Device timestamp when the result was last seen.
	private long timestampNanos;

	private int eventType;
	private int primaryPhy;
	private int secondaryPhy;
	private int advertisingSid;
	private int txPower;
	private int periodicAdvertisingInterval;

	/**
	 * Constructs a new ScanResult.
	 *
	 * @param device Remote Bluetooth device found.
	 * @param scanRecord Scan record including both advertising data and scan response data.
	 * @param rssi Received signal strength.
	 * @param timestampNanos Timestamp at which the scan result was observed.
	 * @deprecated use {@link #ScanResult(BluetoothDevice, int, int, int, int, int, int, int, ScanRecord, long)}
	 */
	public ScanResult(@NonNull final BluetoothDevice device, @Nullable final ScanRecord scanRecord,
					  int rssi, long timestampNanos) {
		this.device = device;
		this.scanRecord = scanRecord;
		this.rssi = rssi;
		this.timestampNanos = timestampNanos;
		this.eventType = (DATA_COMPLETE << 5) | ET_LEGACY_MASK | ET_CONNECTABLE_MASK;
		this.primaryPhy = 1; // BluetoothDevice.PHY_LE_1M;
		this.secondaryPhy = PHY_UNUSED;
		this.advertisingSid = SID_NOT_PRESENT;
		this.txPower = 127;
		this.periodicAdvertisingInterval = 0;
	}

	/**
	 * Constructs a new ScanResult.
	 *
	 * @param device Remote Bluetooth device found.
	 * @param eventType Event type.
	 * @param primaryPhy Primary advertising phy.
	 * @param secondaryPhy Secondary advertising phy.
	 * @param advertisingSid Advertising set ID.
	 * @param txPower Transmit power.
	 * @param rssi Received signal strength.
	 * @param periodicAdvertisingInterval Periodic advertising interval.
	 * @param scanRecord Scan record including both advertising data and scan response data.
	 * @param timestampNanos Timestamp at which the scan result was observed.
	 */
	public ScanResult(@NonNull final BluetoothDevice device, final int eventType,
					  final int primaryPhy, final int secondaryPhy,
					  final int advertisingSid, final int txPower, final int rssi,
					  final int periodicAdvertisingInterval,
					  @Nullable final ScanRecord scanRecord, final long timestampNanos) {
		this.device = device;
		this.eventType = eventType;
		this.primaryPhy = primaryPhy;
		this.secondaryPhy = secondaryPhy;
		this.advertisingSid = advertisingSid;
		this.txPower = txPower;
		this.rssi = rssi;
		this.periodicAdvertisingInterval = periodicAdvertisingInterval;
		this.scanRecord = scanRecord;
		this.timestampNanos = timestampNanos;
	}

	private ScanResult(final Parcel in) {
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		device.writeToParcel(dest, flags);
		if (scanRecord != null) {
			dest.writeInt(1);
			dest.writeByteArray(scanRecord.getBytes());
		} else {
			dest.writeInt(0);
		}
		dest.writeInt(rssi);
		dest.writeLong(timestampNanos);
		dest.writeInt(eventType);
		dest.writeInt(primaryPhy);
		dest.writeInt(secondaryPhy);
		dest.writeInt(advertisingSid);
		dest.writeInt(txPower);
		dest.writeInt(periodicAdvertisingInterval);
	}

	private void readFromParcel(final Parcel in) {
		device = BluetoothDevice.CREATOR.createFromParcel(in);
		if (in.readInt() == 1) {
			scanRecord = ScanRecord.parseFromBytes(in.createByteArray());
		}
		rssi = in.readInt();
		timestampNanos = in.readLong();
		eventType = in.readInt();
		primaryPhy = in.readInt();
		secondaryPhy = in.readInt();
		advertisingSid = in.readInt();
		txPower = in.readInt();
		periodicAdvertisingInterval = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Returns the remote Bluetooth device identified by the Bluetooth device address.
	 */
	@NonNull
	public BluetoothDevice getDevice() {
		return device;
	}

	/**
	 * Returns the scan record, which is a combination of advertisement and scan response.
	 */
	@Nullable
	public ScanRecord getScanRecord() {
		return scanRecord;
	}

	/**
	 * Returns the received signal strength in dBm. The valid range is [-127, 126].
	 */
	public int getRssi() {
		return rssi;
	}

	/**
	 * Returns timestamp since boot when the scan record was observed.
	 */
	public long getTimestampNanos() {
		return timestampNanos;
	}

	/**
	 * Returns true if this object represents legacy scan result.
	 * Legacy scan results do not contain advanced advertising information
	 * as specified in the Bluetooth Core Specification v5.
	 */
	public boolean isLegacy() {
		return (eventType & ET_LEGACY_MASK) != 0;
	}

	/**
	 * Returns true if this object represents connectable scan result.
	 */
	public boolean isConnectable() {
		return (eventType & ET_CONNECTABLE_MASK) != 0;
	}

	/**
	 * Returns the data status.
	 * Can be one of {@link ScanResult#DATA_COMPLETE} or
	 * {@link ScanResult#DATA_TRUNCATED}.
	 */
	public int getDataStatus() {
		// return bit 5 and 6
		return (eventType >> 5) & 0x03;
	}

	/**
	 * Returns the primary Physical Layer
	 * on which this advertisement was received.
	 * Can be one of {@link BluetoothDevice#PHY_LE_1M} or
	 * {@link BluetoothDevice#PHY_LE_CODED}.
	 */
	public int getPrimaryPhy() { return primaryPhy; }

	/**
	 * Returns the secondary Physical Layer
	 * on which this advertisement was received.
	 * Can be one of {@link BluetoothDevice#PHY_LE_1M},
	 * {@link BluetoothDevice#PHY_LE_2M}, {@link BluetoothDevice#PHY_LE_CODED}
	 * or {@link ScanResult#PHY_UNUSED} - if the advertisement
	 * was not received on a secondary physical channel.
	 */
	public int getSecondaryPhy() { return secondaryPhy; }

	/**
	 * Returns the advertising set id.
	 * May return {@link ScanResult#SID_NOT_PRESENT} if
	 * no set id was is present.
	 */
	public int getAdvertisingSid() { return advertisingSid; }

	/**
	 * Returns the transmit power in dBm.
	 * Valid range is [-127, 126]. A value of {@link ScanResult#TX_POWER_NOT_PRESENT}
	 * indicates that the TX power is not present.
	 */
	public int getTxPower() { return txPower; }

	/**
	 * Returns the periodic advertising interval in units of 1.25ms.
	 * Valid range is 6 (7.5ms) to 65536 (81918.75ms). A value of
	 * {@link ScanResult#PERIODIC_INTERVAL_NOT_PRESENT} means periodic
	 * advertising interval is not present.
	 */
	public int getPeriodicAdvertisingInterval() {
		return periodicAdvertisingInterval;
	}

	@Override
	public int hashCode() {
		return Objects.hash(device, rssi, scanRecord, timestampNanos,
				eventType, primaryPhy, secondaryPhy,
				advertisingSid, txPower,
				periodicAdvertisingInterval);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final ScanResult other = (ScanResult) obj;
		return Objects.equals(device, other.device) && (rssi == other.rssi) &&
				Objects.equals(scanRecord, other.scanRecord) &&
				(timestampNanos == other.timestampNanos) &&
				eventType == other.eventType &&
				primaryPhy == other.primaryPhy &&
				secondaryPhy == other.secondaryPhy &&
				advertisingSid == other.advertisingSid &&
				txPower == other.txPower &&
				periodicAdvertisingInterval == other.periodicAdvertisingInterval;
	}

	@Override
	public String toString() {
		return "ScanResult{" + "device=" + device + ", scanRecord=" +
				Objects.toString(scanRecord) + ", rssi=" + rssi +
				", timestampNanos=" + timestampNanos + ", eventType=" + eventType +
				", primaryPhy=" + primaryPhy + ", secondaryPhy=" + secondaryPhy +
				", advertisingSid=" + advertisingSid + ", txPower=" + txPower +
				", periodicAdvertisingInterval=" + periodicAdvertisingInterval + '}';
	}

	public static final Parcelable.Creator<ScanResult> CREATOR = new Creator<ScanResult>() {
		@Override
		public ScanResult createFromParcel(final Parcel source) {
			return new ScanResult(source);
		}

		@Override
		public ScanResult[] newArray(final int size) {
			return new ScanResult[size];
		}
	};

}
