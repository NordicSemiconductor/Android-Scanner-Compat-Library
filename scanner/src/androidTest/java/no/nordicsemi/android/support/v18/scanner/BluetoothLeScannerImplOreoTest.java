package no.nordicsemi.android.support.v18.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.truth.Truth.assertThat;

public class BluetoothLeScannerImplOreoTest {

	@Test
	public void toImpl() {
		// Build mock data
		final byte[] bytes = new byte[]{
				2, 1, 6, 								// Flags
				5, 8, 'T', 'e', 's', 't',				// Shortened Local Name (Test)
				6, (byte) 0xFF, 0x59, 0x00, 1, 2, 3,	// Manufacturer Data (Nordic Semi -> 0x010203)
				3, 0x16, 0x09, 0x18,					// Service Data - 16-bit UUID (0x1809)
				2, 0x0A, 1								// Tx Power Level (1 dBm)
		};

		final BluetoothDevice device =
				BluetoothAdapter.getDefaultAdapter().getRemoteDevice("01:02:03:04:05:06");

		final android.bluetooth.le.ScanRecord _record = parseScanRecord(bytes);

		android.bluetooth.le.ScanResult _result = new android.bluetooth.le.ScanResult(device,
				0b000001, 1, 2, 0,
				android.bluetooth.le.ScanResult.TX_POWER_NOT_PRESENT, -70,
				android.bluetooth.le.ScanResult.PERIODIC_INTERVAL_NOT_PRESENT, _record,
				123456789L);

		// Convert to support.v18.ScanResult
		final BluetoothLeScannerImplOreo impl = new BluetoothLeScannerImplOreo();
		final ScanResult result = impl.fromNativeScanResult(_result);

		// Validate
		assertThat(result).isNotNull();
		assertThat(_record).isNotNull();
		assertThat(_result.isLegacy()).isEqualTo(result.isLegacy());
		assertThat(_result.isConnectable()).isEqualTo(result.isConnectable());
		assertThat(result.getDataStatus()).isEqualTo(ScanResult.DATA_COMPLETE);
		assertThat(result.getScanRecord()).isNotNull();
		final ScanRecord record = result.getScanRecord();
		assertThat(record.getAdvertiseFlags()).isEqualTo(6);
		assertThat(bytes).isEqualTo(record.getBytes());
		assertThat(record.getManufacturerSpecificData(0x0059)).isNotNull();
		assertThat(_record.getManufacturerSpecificData(0x0059))
				.isEqualTo(record.getManufacturerSpecificData(0x0059));
		assertThat(result.getPeriodicAdvertisingInterval())
				.isEqualTo(ScanResult.PERIODIC_INTERVAL_NOT_PRESENT);
		assertThat(result.getTxPower()).isEqualTo(ScanResult.TX_POWER_NOT_PRESENT);
		assertThat(result.getTimestampNanos()).isEqualTo(123456789L);
		assertThat(_result.getDevice()).isEqualTo(result.getDevice());
		assertThat(device).isEqualTo(result.getDevice());
	}

	/**
	 * Utility method to call hidden ScanRecord.parseFromBytes method.
	 */
	static android.bluetooth.le.ScanRecord parseScanRecord(byte[] bytes) {
		final Class<?> scanRecordClass = android.bluetooth.le.ScanRecord.class;
		try {
			final Method method = scanRecordClass.getDeclaredMethod("parseFromBytes", byte[].class);
			return (android.bluetooth.le.ScanRecord) method.invoke(null, bytes);
		} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			return null;
		}
	}
}