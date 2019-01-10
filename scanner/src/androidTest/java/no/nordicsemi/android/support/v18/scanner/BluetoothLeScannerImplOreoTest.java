package no.nordicsemi.android.support.v18.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.SparseArray;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class BluetoothLeScannerImplOreoTest {

	@Test
	public void toImpl() {
		// Build mock data
		List<ParcelUuid> serviceUuids = new ArrayList<>();
		serviceUuids.add(ParcelUuid.fromString("00001809-0000-1000-8000-00805F9B34FB"));

		SparseArray<byte[]> manufacturerData = new SparseArray<>();
		manufacturerData.append(0x0059, new byte[] { 1, 2, 3});

		Map<ParcelUuid, byte[]> serviceData = new HashMap<>();
		serviceData.put(ParcelUuid.fromString("00001809-0000-1000-8000-00805F9B34FB"), new byte[] { 0x64 });

		final byte[] bytes = new byte[] { 2, 1, 6, 5, 8, 'T', 'e', 's', 't', 6, (byte) 0xFF, 0x59, 0x00, 1, 2, 3, 4, 0x16, 0x09, 0x18, 0x64, 2, 0x0A, 1};

		try {
			BluetoothDevice device =
					BluetoothAdapter.getDefaultAdapter().getRemoteDevice("01:02:03:04:05:06");

			final Constructor constructor =
					android.bluetooth.le.ScanRecord.class.getDeclaredConstructor(List.class,
					SparseArray.class, Map.class, int.class, int.class, String.class, byte[].class);
			constructor.setAccessible(true);
			final android.bluetooth.le.ScanRecord _record = (android.bluetooth.le.ScanRecord)
					constructor.newInstance(serviceUuids, manufacturerData, serviceData, 0x06, 1, "Test", bytes);

			android.bluetooth.le.ScanResult _result = new android.bluetooth.le.ScanResult(device,
					0b000001, 1, 2, 0,
					android.bluetooth.le.ScanResult.TX_POWER_NOT_PRESENT, -70,
					android.bluetooth.le.ScanResult.PERIODIC_INTERVAL_NOT_PRESENT, _record,
					123456789L);

			// Convert to support.v18.ScanResult
			final BluetoothLeScannerImplOreo impl = new BluetoothLeScannerImplOreo();
			final ScanResult result = impl.fromNativeScanResult(_result);

			// Validate
			assertEquals(_result.isLegacy(), result.isLegacy());
			assertEquals(_result.isConnectable(), result.isConnectable());
			assertEquals(ScanResult.DATA_COMPLETE, result.getDataStatus());
			assertNotNull(result.getScanRecord());
			final ScanRecord record = result.getScanRecord();
			assertEquals(6, record.getAdvertiseFlags());
			assertArrayEquals(bytes, record.getBytes());
			assertNotNull(record.getManufacturerSpecificData(0x0059));
			assertArrayEquals(_record.getManufacturerSpecificData(0x0059),
					record.getManufacturerSpecificData(0x0059));
			assertEquals(ScanResult.PERIODIC_INTERVAL_NOT_PRESENT, result.getPeriodicAdvertisingInterval());
			assertEquals(ScanResult.TX_POWER_NOT_PRESENT, result.getTxPower());
			assertEquals(123456789L, result.getTimestampNanos());
			assertSame(_result.getDevice(), result.getDevice());
			assertSame(device, result.getDevice());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}