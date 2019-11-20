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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class) public class ScanFilterTest {

  private static final String DEVICE_MAC = "01:02:03:04:05:AB";
  private ScanResult scanResult;
  private ScanFilter.Builder filterBuilder;

  @Before public void setup() {
    byte[] scanRecord = new byte[] {
        0x02, 0x01, 0x1a, // advertising flags
        0x05, 0x02, 0x0b, 0x11, 0x0a, 0x11, // 16 bit service uuids
        0x04, 0x09, 0x50, 0x65, 0x64, // setName
        0x02, 0x0A, (byte) 0xec, // tx power level
        0x05, 0x16, 0x0b, 0x11, 0x50, 0x64, // service data
        0x05, (byte) 0xff, (byte) 0xe0, 0x00, 0x02, 0x15, // manufacturer specific data
        0x03, 0x50, 0x01, 0x02, // an unknown data type won't cause trouble
    };

    final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    final BluetoothDevice device = adapter.getRemoteDevice(DEVICE_MAC);

    scanResult =
        new ScanResult(device, ScanRecord.parseFromBytes(scanRecord), -10, 1397545200000000L);
    filterBuilder = new ScanFilter.Builder();
  }

  @Test public void testsetNameFilter() {
    ScanFilter filter = filterBuilder.setDeviceName("Ped").build();
    assertTrue("setName filter fails", filter.matches(scanResult));
    filter = filterBuilder.setDeviceName("Pem").build();
    assertFalse("setName filter fails", filter.matches(scanResult));
  }

  @Test public void testDeviceFilter() {
    ScanFilter filter = filterBuilder.setDeviceAddress(DEVICE_MAC).build();
    assertTrue("device filter fails", filter.matches(scanResult));
    filter = filterBuilder.setDeviceAddress("11:22:33:44:55:66").build();
    assertFalse("device filter fails", filter.matches(scanResult));
  }

  @Test public void testsetServiceUuidFilter() {
    ScanFilter filter =
        filterBuilder.setServiceUuid(ParcelUuid.fromString("0000110A-0000-1000-8000-00805F9B34FB"))
            .build();
    assertTrue("uuid filter fails", filter.matches(scanResult));
    filter =
        filterBuilder.setServiceUuid(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"))
            .build();
    assertFalse("uuid filter fails", filter.matches(scanResult));
    filter =
        filterBuilder.setServiceUuid(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"),
            ParcelUuid.fromString("FFFFFFF0-FFFF-FFFF-FFFF-FFFFFFFFFFFF")).build();
    assertTrue("uuid filter fails", filter.matches(scanResult));
  }

  @Test public void testsetServiceDataFilter() {
    byte[] setServiceData = new byte[] {
        0x50, 0x64
    };
    ParcelUuid serviceDataUuid = ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB");
    ScanFilter filter = filterBuilder.setServiceData(serviceDataUuid, setServiceData).build();
    assertTrue("service data filter fails", filter.matches(scanResult));
    byte[] emptyData = new byte[0];
    filter = filterBuilder.setServiceData(serviceDataUuid, emptyData).build();
    assertTrue("service data filter fails", filter.matches(scanResult));
    byte[] prefixData = new byte[] {
        0x50
    };
    filter = filterBuilder.setServiceData(serviceDataUuid, prefixData).build();
    assertTrue("service data filter fails", filter.matches(scanResult));
    byte[] nonMatchData = new byte[] {
        0x51, 0x64
    };
    byte[] mask = new byte[] {
        (byte) 0x00, (byte) 0xFF
    };
    filter = filterBuilder.setServiceData(serviceDataUuid, nonMatchData, mask).build();
    assertTrue("partial service data filter fails", filter.matches(scanResult));
    filter = filterBuilder.setServiceData(serviceDataUuid, nonMatchData).build();
    assertFalse("service data filter fails", filter.matches(scanResult));

    filter = filterBuilder.setServiceData(
            ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"),
            ParcelUuid.fromString("FFFFFFF0-FFFF-FFFF-FFFF-FFFFFFFFFFFF"),
            setServiceData).build();
    assertTrue("service data filter fails", filter.matches(scanResult));
    filter = filterBuilder.setServiceData(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"), ParcelUuid.fromString("FFFFFFF0-FFFF-FFFF-FFFF-FFFFFFFFFFFF"), nonMatchData, mask).build();
    assertTrue("partial service data filter fails", filter.matches(scanResult));
    filter = filterBuilder.setServiceData(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"), ParcelUuid.fromString("FFFFFFF0-FFFF-FFFF-FFFF-FFFFFFFFFFFF"), nonMatchData).build();
    assertFalse("service data filter fails", filter.matches(scanResult));

    filter = filterBuilder.setServiceData(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"), ParcelUuid.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"), nonMatchData).build();
    assertFalse("service data filter fails", filter.matches(scanResult));
    filter = filterBuilder.setServiceData(ParcelUuid.fromString("75837467-2222-3333-4444-193749571524"), ParcelUuid.fromString("00000000-0000-0000-0000-000000000000"), nonMatchData).build();
    assertTrue("service data filter fails", filter.matches(scanResult));
  }

  @Test public void testManufacturerSpecificData() {
    byte[] setManufacturerData = new byte[] {
        0x02, 0x15
    };
    int manufacturerId = 0xE0;
    ScanFilter filter =
        filterBuilder.setManufacturerData(manufacturerId, setManufacturerData).build();
    assertTrue("manufacturer data filter fails", filter.matches(scanResult));
    byte[] emptyData = new byte[0];
    filter = filterBuilder.setManufacturerData(manufacturerId, emptyData).build();
    assertTrue("manufacturer data filter fails", filter.matches(scanResult));
    byte[] prefixData = new byte[] {
        0x02
    };
    filter = filterBuilder.setManufacturerData(manufacturerId, prefixData).build();
    assertTrue("manufacturer data filter fails", filter.matches(scanResult));
    // Test data mask
    byte[] nonMatchData = new byte[] {
        0x02, 0x14
    };
    filter = filterBuilder.setManufacturerData(manufacturerId, nonMatchData).build();
    assertFalse("manufacturer data filter fails", filter.matches(scanResult));
    byte[] mask = new byte[] {
        (byte) 0xFF, (byte) 0x00
    };
    filter = filterBuilder.setManufacturerData(manufacturerId, nonMatchData, mask).build();
    assertTrue("partial setManufacturerData filter fails", filter.matches(scanResult));
  }

  @Test public void testReadWriteParcel() {
    ScanFilter filter = filterBuilder.build();
    testReadWriteParcelForFilter(filter);
    filter = filterBuilder.setDeviceName("Ped").build();
    testReadWriteParcelForFilter(filter);
    filter = filterBuilder.setDeviceAddress("11:22:33:44:55:66").build();
    testReadWriteParcelForFilter(filter);
    filter =
        filterBuilder.setServiceUuid(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"))
            .build();
    testReadWriteParcelForFilter(filter);
    filter =
        filterBuilder.setServiceUuid(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"),
            ParcelUuid.fromString("FFFFFFF0-FFFF-FFFF-FFFF-FFFFFFFFFFFF")).build();
    testReadWriteParcelForFilter(filter);

    byte[] serviceData = new byte[] {
        0x50, 0x64
    };
    ParcelUuid serviceDataUuid = ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB");
    filter = filterBuilder.setServiceData(serviceDataUuid, serviceData).build();
    testReadWriteParcelForFilter(filter);
    filter = filterBuilder.setServiceData(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"),
        ParcelUuid.fromString("FFFFFFF0-FFFF-FFFF-FFFF-FFFFFFFFFFFF"), serviceData).build();
    testReadWriteParcelForFilter(filter);
    filter = filterBuilder.setServiceData(serviceDataUuid, new byte[0]).build();
    testReadWriteParcelForFilter(filter);
    filter = filterBuilder.setServiceData(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"),
        ParcelUuid.fromString("FFFFFFF0-FFFF-FFFF-FFFF-FFFFFFFFFFFF"), new byte[0]).build();
    testReadWriteParcelForFilter(filter);
    byte[] serviceDataMask = new byte[] {
        (byte) 0xFF, (byte) 0xFF
    };
    filter = filterBuilder.setServiceData(serviceDataUuid, serviceData, serviceDataMask).build();
    testReadWriteParcelForFilter(filter);
    filter = filterBuilder.setServiceData(ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB"),
        ParcelUuid.fromString("FFFFFFF0-FFFF-FFFF-FFFF-FFFFFFFFFFFF"), serviceData, serviceDataMask).build();
    testReadWriteParcelForFilter(filter);

    byte[] manufacturerData = new byte[] {
        0x02, 0x15
    };
    int manufacturerId = 0xE0;
    filter = filterBuilder.setManufacturerData(manufacturerId, manufacturerData).build();
    testReadWriteParcelForFilter(filter);
    filter = filterBuilder.setServiceData(serviceDataUuid, new byte[0]).build();
    testReadWriteParcelForFilter(filter);
    byte[] manufacturerDataMask = new byte[] {
        (byte) 0xFF, (byte) 0xFF
    };
    filter =
        filterBuilder.setManufacturerData(manufacturerId, manufacturerData, manufacturerDataMask)
            .build();
    testReadWriteParcelForFilter(filter);
  }

  private void testReadWriteParcelForFilter(ScanFilter filter) {
    Parcel parcel = Parcel.obtain();
    filter.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    ScanFilter filterFromParcel = ScanFilter.CREATOR.createFromParcel(parcel);
    assertEquals(filter, filterFromParcel);
  }
}
