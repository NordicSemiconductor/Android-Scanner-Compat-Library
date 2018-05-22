# Android BLE Scanner Compat library

[ ![Download](https://api.bintray.com/packages/nordic/android/no.nordicsemi.android.support.v18%3Ascanner/images/download.svg) ](https://bintray.com/nordic/android/no.nordicsemi.android.support.v18%3Ascanner/_latestVersion)

The Scanner Compat library solves the problem with scanning for Bluetooth Smart devices on Android. 
The scanner API has changed in the Android 5.0 and has been extended in 6.0 and 8.0. 
Using this library you may have almost all new features even on older phones. If a feature (for example offloaded filtering or batching) is not supported natively,
it will be emulated by the compat library. You may also disable the native support for filtering, batching and reporting first match or match lost if required.
Advertising Extension (`ScanSetting#setLegacy(boolean)` or `setPhy(int)`) is available only on Android Oreo or newer and such calls will be ignored on older platforms, 
that means only legacy advertising packets on PHY LE 1M will be reported, due to the Bluetooth chipset capabilities.

## Usage

The compat library may be found on jcenter repository. Add it to your project by adding the following dependency:

```Groovy
compile 'no.nordicsemi.android.support.v18:scanner:1.1.0'
```

## API

The Scanner Compat API is very similar to the original one, known from Android Oreo.

Instead of getting it from the **BluetoothAdapter**, acquire the scanner instance using:

```java
BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
```

You also need to change the packets for **ScanSettings**, **ScanFilter** and **ScanCallback** classes to:

```java
no.nordicsemi.android.support.v18.scanner
```

## Sample

To start scanning use (example):

```java
	BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
	ScanSettings settings = new ScanSettings.Builder()
				.setLegacy(false)
				.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000)
				.setUseHardwareBatchingIfSupported(false).build();
	List<ScanFilter> filters = new ArrayList<>();
	filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());
	scanner.startScan(filters, settings, scanCallback);
```

to stop scanning use:

```java
	BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
	scanner.stopScan(scanCallback);
```

## License

The Scanner Compat library is available under BSD 3-Clause license. See the LICENSE file for more info.