/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.nordicsemi.android.support.v18.scanner;

import android.os.ParcelUuid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Static helper methods and constants to decode the ParcelUuid of remote devices.
 */
/* package */ final class BluetoothUuid {

	private static final ParcelUuid BASE_UUID =
			ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");

	/** Length of bytes for 16 bit UUID */
	static final int UUID_BYTES_16_BIT = 2;
	/** Length of bytes for 32 bit UUID */
	static final int UUID_BYTES_32_BIT = 4;
	/** Length of bytes for 128 bit UUID */
	static final int UUID_BYTES_128_BIT = 16;

	/**
	 * Parse UUID from bytes. The {@code uuidBytes} can represent a 16-bit, 32-bit or 128-bit UUID,
	 * but the returned UUID is always in 128-bit format.
	 * Note UUID is little endian in Bluetooth.
	 *
	 * @param uuidBytes Byte representation of uuid.
	 * @return {@link ParcelUuid} parsed from bytes.
	 * @throws IllegalArgumentException If the {@code uuidBytes} cannot be parsed.
	 */
	static ParcelUuid parseUuidFrom(final byte[] uuidBytes) {
		if (uuidBytes == null) {
			throw new IllegalArgumentException("uuidBytes cannot be null");
		}
		final int length = uuidBytes.length;
		if (length != UUID_BYTES_16_BIT && length != UUID_BYTES_32_BIT &&
				length != UUID_BYTES_128_BIT) {
			throw new IllegalArgumentException("uuidBytes length invalid - " + length);
		}

		// Construct a 128 bit UUID.
		if (length == UUID_BYTES_128_BIT) {
			final ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
			final long msb = buf.getLong(8);
			final long lsb = buf.getLong(0);
			return new ParcelUuid(new UUID(msb, lsb));
		}

		// For 16 bit and 32 bit UUID we need to convert them to 128 bit value.
		// 128_bit_value = uuid * 2^96 + BASE_UUID
		long shortUuid;
		if (length == UUID_BYTES_16_BIT) {
			shortUuid = uuidBytes[0] & 0xFF;
			shortUuid += (uuidBytes[1] & 0xFF) << 8;
		} else {
			shortUuid = uuidBytes[0] & 0xFF ;
			shortUuid += (uuidBytes[1] & 0xFF) << 8;
			shortUuid += (uuidBytes[2] & 0xFF) << 16;
			shortUuid += (uuidBytes[3] & 0xFF) << 24;
		}
		final long msb = BASE_UUID.getUuid().getMostSignificantBits() + (shortUuid << 32);
		final long lsb = BASE_UUID.getUuid().getLeastSignificantBits();
		return new ParcelUuid(new UUID(msb, lsb));
	}
}
