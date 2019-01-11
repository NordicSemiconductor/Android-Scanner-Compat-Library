# Android BLE Scanner Compat library

[ ![Download](https://api.bintray.com/packages/nordic/android/no.nordicsemi.android.support.v18%3Ascanner/images/download.svg) ](https://bintray.com/nordic/android/no.nordicsemi.android.support.v18%3Ascanner/_latestVersion)

The Scanner Compat library solves the problem with scanning for Bluetooth Low Energy devices on Android. 
The scanner API has changed in Android 5.0 and has been extended in 6.0 and 8.0. 
Using this library you may have almost all new features even on older phones. If a feature (for example offloaded filtering or batching) is not supported natively,
it will be emulated by the compat library. You may also disable the native support for filtering, batching and reporting first match or match lost if required.
Advertising Extension (`ScanSetting#setLegacy(boolean)` or `setPhy(int)`) is available only on Android Oreo or newer and such calls will be ignored on older platforms, 
that means only legacy advertising packets on PHY LE 1M will be reported, due to the Bluetooth chipset capabilities.

## Usage

The compat library may be found on jcenter repository. Add it to your project by adding the following dependency:

```Groovy
compile 'no.nordicsemi.android.support.v18:scanner:1.3.0'
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

### Scanning with Pending Intent

Android 8.0 Oreo introduced [Background Execution Limits](https://developer.android.com/about/versions/oreo/background).
At the same time, to make background scanning possible, a new 
[method](https://developer.android.com/reference/android/bluetooth/le/BluetoothLeScanner.html#startScan(java.util.List%3Candroid.bluetooth.le.ScanFilter%3E,%20android.bluetooth.le.ScanSettings,%20android.app.PendingIntent))
was added to [BluetoothLeScanner](https://developer.android.com/reference/android/bluetooth/le/BluetoothLeScanner.html)
which allows registering a [PendingIntent](https://developer.android.com/reference/android/app/PendingIntent).
This allows to send a Broadcast whenever a device is found.

Starting from version 1.3.0, this library may emulate such feature on older Android versions.
In order to do that, a background service will be started after calling `scanner.startScan(filters, settings, context, pendingIntent)`,
which will start scanning in background with given settings and will send an Intent if a device 
matching filter is found. To lower battery consumption it is recommended to set `ScanSettings.SCAN_MODE_LOW_POWER`
scanning mode and use filter. To stop scanning call `scanner.stopScan(context, pendingIntent)` with 
the same intent in parameter. The service will be stopped when a last scan was stopped.

## License

The Scanner Compat library is available under BSD 3-Clause license. See the LICENSE file for more info.