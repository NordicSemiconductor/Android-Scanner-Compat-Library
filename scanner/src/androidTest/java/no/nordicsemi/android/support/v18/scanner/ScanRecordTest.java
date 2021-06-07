/*
 * Copyright (C) 2014 The Android Open Source Project
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

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public class ScanRecordTest {

	@Test
	public void testParser() {
		final byte[] scanRecord = new byte[]{
				0x02, 0x01, 0x1a,                                 // Flags
				0x05, 0x02, 0x0b, 0x11, 0x0a, 0x11,               // Incomplete List of 16-bit Service Class UUIDs
				0x04, 0x09, 0x50, 0x65, 0x64,                     // Complete Local Name
				0x02, 0x0A, (byte) 0xec,                          // Tx Power Level
				0x05, 0x16, 0x0b, 0x11, 0x50, 0x64,               // Service Data - 16-bit UUID
				0x05, (byte) 0xff, (byte) 0xe0, 0x00, 0x02, 0x15, // Manufacturer Specific Data
				0x03, 0x50, 0x01, 0x02,                           // An unknown data type won't cause trouble
		};
		final ScanRecord data = ScanRecord.parseFromBytes(scanRecord);
		assertThat(data).isNotNull();
		assertThat(data.getAdvertiseFlags()).isEqualTo(0x1a);
		final ParcelUuid uuid1 = ParcelUuid.fromString("0000110A-0000-1000-8000-00805F9B34FB");
		final ParcelUuid uuid2 = ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB");
		assertThat(data.getServiceUuids()).contains(uuid1);
		assertThat(data.getServiceUuids()).contains(uuid2);
		assertThat(data.getDeviceName()).isEqualTo("Ped");
		assertThat(data.getTxPowerLevel()).isEqualTo(-20);
		assertThat(data.getManufacturerSpecificData()).isNotNull();
		assertThat(data.getManufacturerSpecificData().get(0x00E0)).isNotNull();
		assertThat(data.getManufacturerSpecificData().get(0x00E0))
				.isEqualTo(new byte[] { 0x02, 0x15});
		assertThat(data.getServiceData()).isNotNull();
		assertThat(data.getServiceData()).containsKey(uuid2);
		assertThat(data.getServiceData().get(uuid2))
				.isEqualTo(new byte[] { 0x50, 0x64});
	}
}
