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

import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents a scan record from Bluetooth LE scan.
 */
@SuppressWarnings("WeakerAccess")
public final class ScanRecord {

	private static final String TAG = "ScanRecord";

	// The following data type values are assigned by Bluetooth SIG.
	// For more details refer to Bluetooth 4.1 specification, Volume 3, Part C, Section 18.
	private static final int DATA_TYPE_FLAGS = 0x01;
	private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02;
	private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03;
	private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04;
	private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05;
	private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06;
	private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07;
	private static final int DATA_TYPE_LOCAL_NAME_SHORT = 0x08;
	private static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09;
	private static final int DATA_TYPE_TX_POWER_LEVEL = 0x0A;
	private static final int DATA_TYPE_SERVICE_DATA_16_BIT = 0x16;
	private static final int DATA_TYPE_SERVICE_DATA_32_BIT = 0x20;
	private static final int DATA_TYPE_SERVICE_DATA_128_BIT = 0x21;
	private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;

	// Flags of the advertising data.
	private final int advertiseFlags;

	@Nullable private final List<ParcelUuid> serviceUuids;

	@Nullable private final SparseArray<byte[]> manufacturerSpecificData;

	@Nullable private final Map<ParcelUuid, byte[]> serviceData;

	// Transmission power level(in dB).
	private final int txPowerLevel;

	// Local name of the Bluetooth LE device.
	private final String deviceName;

	// Raw bytes of scan record.
	private final byte[] bytes;

	/**
	 * Returns the advertising flags indicating the discoverable mode and capability of the device.
	 * Returns -1 if the flag field is not set.
	 */
	public int getAdvertiseFlags() {
		return advertiseFlags;
	}

	/**
	 * Returns a list of service UUIDs within the advertisement that are used to identify the
	 * bluetooth GATT services.
	 */
	@Nullable
	public List<ParcelUuid> getServiceUuids() {
		return serviceUuids;
	}

	/**
	 * Returns a sparse array of manufacturer identifier and its corresponding manufacturer specific
	 * data.
	 */
	@Nullable
	public SparseArray<byte[]> getManufacturerSpecificData() {
		return manufacturerSpecificData;
	}

	/**
	 * Returns the manufacturer specific data associated with the manufacturer id. Returns
	 * {@code null} if the {@code manufacturerId} is not found.
	 */
	@Nullable
	public byte[] getManufacturerSpecificData(final int manufacturerId) {
		if (manufacturerSpecificData == null) {
			return null;
		}
		return manufacturerSpecificData.get(manufacturerId);
	}

	/**
	 * Returns a map of service UUID and its corresponding service data.
	 */
	@Nullable
	public Map<ParcelUuid, byte[]> getServiceData() {
		return serviceData;
	}

	/**
	 * Returns the service data byte array associated with the {@code serviceUuid}. Returns
	 * {@code null} if the {@code serviceDataUuid} is not found.
	 */
	@Nullable
	public byte[] getServiceData(@NonNull final ParcelUuid serviceDataUuid) {
		//noinspection ConstantConditions
		if (serviceDataUuid == null || serviceData == null) {
			return null;
		}
		return serviceData.get(serviceDataUuid);
	}

	/**
	 * Returns the transmission power level of the packet in dBm. Returns {@link Integer#MIN_VALUE}
	 * if the field is not set. This value can be used to calculate the path loss of a received
	 * packet using the following equation:
	 * <p>
	 * <code>pathloss = txPowerLevel - rssi</code>
	 */
	public int getTxPowerLevel() {
		return txPowerLevel;
	}

	/**
	 * Returns the local name of the BLE device. The is a UTF-8 encoded string.
	 */
	@Nullable
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * Returns raw bytes of scan record.
	 */
	@Nullable
	public byte[] getBytes() {
		return bytes;
	}

	private ScanRecord(@Nullable final List<ParcelUuid> serviceUuids,
					   @Nullable final SparseArray<byte[]> manufacturerData,
					   @Nullable final Map<ParcelUuid, byte[]> serviceData,
					   final int advertiseFlags, final int txPowerLevel,
					   final String localName, final byte[] bytes) {
		this.serviceUuids = serviceUuids;
		this.manufacturerSpecificData = manufacturerData;
		this.serviceData = serviceData;
		this.deviceName = localName;
		this.advertiseFlags = advertiseFlags;
		this.txPowerLevel = txPowerLevel;
		this.bytes = bytes;
	}

	/**
	 * Parse scan record bytes to {@link ScanRecord}.
	 * <p>
	 * The format is defined in Bluetooth 4.1 specification, Volume 3, Part C, Section 11 and 18.
	 * <p>
	 * All numerical multi-byte entities and values shall use little-endian <strong>byte</strong>
	 * order.
	 *
	 * @param scanRecord The scan record of Bluetooth LE advertisement and/or scan response.
	 */
	@Nullable
	/* package */ static ScanRecord parseFromBytes(@Nullable final byte[] scanRecord) {
		if (scanRecord == null) {
			return null;
		}

		int currentPos = 0;
		int advertiseFlag = -1;
		int txPowerLevel = Integer.MIN_VALUE;
		String localName = null;
		List<ParcelUuid> serviceUuids = null;
		SparseArray<byte[]> manufacturerData = null;
		Map<ParcelUuid, byte[]> serviceData = null;

		try {
			while (currentPos < scanRecord.length) {
				// length is unsigned int.
				final int length = scanRecord[currentPos++] & 0xFF;
				if (length == 0) {
					break;
				}
				// Note the length includes the length of the field type itself.
				final int dataLength = length - 1;
				// fieldType is unsigned int.
				final int fieldType = scanRecord[currentPos++] & 0xFF;
				switch (fieldType) {
					case DATA_TYPE_FLAGS:
						advertiseFlag = scanRecord[currentPos] & 0xFF;
						break;
					case DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL:
					case DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE:
						if (serviceUuids == null)
							serviceUuids = new ArrayList<>();
						parseServiceUuid(scanRecord, currentPos,
								dataLength, BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids);
						break;
					case DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL:
					case DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE:
						if (serviceUuids == null)
							serviceUuids = new ArrayList<>();
						parseServiceUuid(scanRecord, currentPos, dataLength,
								BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids);
						break;
					case DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL:
					case DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE:
						if (serviceUuids == null)
							serviceUuids = new ArrayList<>();
						parseServiceUuid(scanRecord, currentPos, dataLength,
								BluetoothUuid.UUID_BYTES_128_BIT, serviceUuids);
						break;
					case DATA_TYPE_LOCAL_NAME_SHORT:
					case DATA_TYPE_LOCAL_NAME_COMPLETE:
						localName = new String(
								extractBytes(scanRecord, currentPos, dataLength));
						break;
					case DATA_TYPE_TX_POWER_LEVEL:
						txPowerLevel = scanRecord[currentPos];
						break;
					case DATA_TYPE_SERVICE_DATA_16_BIT:
					case DATA_TYPE_SERVICE_DATA_32_BIT:
					case DATA_TYPE_SERVICE_DATA_128_BIT:
						int serviceUuidLength = BluetoothUuid.UUID_BYTES_16_BIT;
						if (fieldType == DATA_TYPE_SERVICE_DATA_32_BIT) {
							serviceUuidLength = BluetoothUuid.UUID_BYTES_32_BIT;
						} else if (fieldType == DATA_TYPE_SERVICE_DATA_128_BIT) {
							serviceUuidLength = BluetoothUuid.UUID_BYTES_128_BIT;
						}

						final byte[] serviceDataUuidBytes = extractBytes(scanRecord, currentPos,
								serviceUuidLength);
						final ParcelUuid serviceDataUuid = BluetoothUuid.parseUuidFrom(
								serviceDataUuidBytes);
						final byte[] serviceDataArray = extractBytes(scanRecord,
								currentPos + serviceUuidLength, dataLength - serviceUuidLength);
						if (serviceData == null)
							serviceData = new HashMap<>();
						serviceData.put(serviceDataUuid, serviceDataArray);
						break;
					case DATA_TYPE_MANUFACTURER_SPECIFIC_DATA:
						// The first two bytes of the manufacturer specific data are
						// manufacturer ids in little endian.
						final int manufacturerId = ((scanRecord[currentPos + 1] & 0xFF) << 8) +
								(scanRecord[currentPos] & 0xFF);
						final byte[] manufacturerDataBytes = extractBytes(scanRecord, currentPos + 2,
								dataLength - 2);
						if (manufacturerData == null)
							manufacturerData = new SparseArray<>();
						manufacturerData.put(manufacturerId, manufacturerDataBytes);
						break;
					default:
						// Just ignore, we don't handle such data type.
						break;
				}
				currentPos += dataLength;
			}

			return new ScanRecord(serviceUuids, manufacturerData, serviceData,
					advertiseFlag, txPowerLevel, localName, scanRecord);
		} catch (final Exception e) {
			Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
			// As the record is invalid, ignore all the parsed results for this packet
			// and return an empty record with raw scanRecord bytes in results
			return new ScanRecord(null, null, null,
					-1, Integer.MIN_VALUE, null, scanRecord);
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ScanRecord other = (ScanRecord) obj;
		return Arrays.equals(bytes, other.bytes);
	}

	@Override
	public String toString() {
		return "ScanRecord [advertiseFlags=" + advertiseFlags + ", serviceUuids=" + serviceUuids
				+ ", manufacturerSpecificData=" + BluetoothLeUtils.toString(manufacturerSpecificData)
				+ ", serviceData=" + BluetoothLeUtils.toString(serviceData)
				+ ", txPowerLevel=" + txPowerLevel + ", deviceName=" + deviceName + "]";
	}

	// Parse service UUIDs.
	@SuppressWarnings("UnusedReturnValue")
	private static int parseServiceUuid(@NonNull final byte[] scanRecord,
										int currentPos, int dataLength,
										final int uuidLength,
										@NonNull final List<ParcelUuid> serviceUuids) {
		while (dataLength > 0) {
			final byte[] uuidBytes = extractBytes(scanRecord, currentPos,
					uuidLength);
			serviceUuids.add(BluetoothUuid.parseUuidFrom(uuidBytes));
			dataLength -= uuidLength;
			currentPos += uuidLength;
		}
		return currentPos;
	}

	// Helper method to extract bytes from byte array.
	private static byte[] extractBytes(@NonNull final byte[] scanRecord,
									   final int start, final int length) {
		byte[] bytes = new byte[length];
		System.arraycopy(scanRecord, start, bytes, 0, length);
		return bytes;
	}
}
