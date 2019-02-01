# Android BLE Scanner Compat library

[ ![Download](https://api.bintray.com/packages/nordic/android/no.nordicsemi.android.support.v18%3Ascanner/images/download.svg) ](https://bintray.com/nordic/android/no.nordicsemi.android.support.v18%3Ascanner/_latestVersion)

The Scanner Compat library solves the problem with scanning for Bluetooth Low Energy devices on Android. 
The scanner API has changed in Android 5.0 and has been extended in 6.0 and 8.0. 
Using this library you may have almost all new features even on older phones. If a feature 
(for example offloaded filtering or batching) is not supported natively, it will be emulated by 
the compat library. You may also disable the native support for filtering, batching and reporting 
first match or match lost if required. Advertising Extension (`ScanSetting#setLegacy(boolean)` 
or `setPhy(int)`) is available only on Android Oreo or newer and such calls will be ignored on 
older platforms, that means only legacy advertising packets on PHY LE 1M will be reported, 
due to the Bluetooth chipset capabilities.

## Usage

The compat library may be found on jcenter repository. Add it to your project by adding the 
following dependency:

```Groovy
compile 'no.nordicsemi.android.support.v18:scanner:1.3.0'
```

## API

The Scanner Compat API is very similar to the original one, known from Android Oreo.

Instead of getting it from the **BluetoothAdapter**, acquire the scanner instance using:

```java
BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
```

You also need to change the packets for **ScanSettings**, **ScanFilter** and **ScanCallback** 
classes to:

```java
no.nordicsemi.android.support.v18.scanner
```

## Sample

To start scanning use (example):

```java
	BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
	ScanSettings settings = new ScanSettings.Builder()
				.setLegacy(false)
				.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
				.setReportDelay(1000)
				.setUseHardwareBatchingIfSupported(true)
				.build();
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

Android 8.0 Oreo introduced [Background Execution Limits](https://developer.android.com/about/versions/oreo/background)
which made background running services short-lived. At the same time, to make background scanning possible, a new 
[method](https://developer.android.com/reference/android/bluetooth/le/BluetoothLeScanner.html#startScan(java.util.List%3Candroid.bluetooth.le.ScanFilter%3E,%20android.bluetooth.le.ScanSettings,%20android.app.PendingIntent))
was added to [BluetoothLeScanner](https://developer.android.com/reference/android/bluetooth/le/BluetoothLeScanner.html)
which allows registering a [PendingIntent](https://developer.android.com/reference/android/app/PendingIntent)
that will be sent whenever a device matching filter criteria is found. This will also work after 
your application has been killed (the receiver must be added in *AndroidManifest* and the 
`PendingIntent` must be created with an explicit Intent).

Starting from version 1.3.0, this library may emulate such feature on older Android versions.
In order to do that, a background service will be started after calling 
`scanner.startScan(filters, settings, context, pendingIntent)`, which will be scanning in 
background with given settings and will send the given `PendingIntent` when a device 
matching filter is found. To lower battery consumption it is recommended to set 
`ScanSettings.SCAN_MODE_LOW_POWER` scanning mode and use filter, but even with those conditions fulfilled
**the battery consumption will be significantly higher than on Oreo+**. To stop scanning call 
`scanner.stopScan(context, pendingIntent)` with 
[the same](https://developer.android.com/reference/android/app/PendingIntent) intent in parameter. 
The service will be stopped when the last scan was stopped.

On Android Oreo or newer this library will use the native scanning mechanism. However, as it may also 
emulate batching or apply filtering (when `useHardwareBatchingIfSupported` or `useHardwareFilteringIfSupported` 
were called with parameter *false*) the library will register its own broadcast 
receiver that will translate results from native to compat classes. 

The receiver and service will be added automatically to the manifest even if they are not used by 
the application. No changes are required to make it work.

To use this feature:

```java
    Intent intent = new Intent(context, MyReceiver.class); // explicite intent 
	intent.setAction("com.example.ACTION_FOUND");
	intent.putExtra("some.extra", value); // optional
	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	
	BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
	ScanSettings settings = new ScanSettings.Builder()
				.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
				.setReportDelay(10000)
				.build();
	List<ScanFilter> filters = new ArrayList<>();
	filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());
	scanner.startScan(filters, settings, context, pendingIntent);
```

Add your `MyRecever` to *AndroidManifest*, as the application context might have been released 
and all broadcast receivers registered to it together with it.

To stop scanning call:

```java
	// To stop scanning use the same or an equal PendingIntent (check PendingIntent documentation)
    Intent intent = new Intent(context, MyReceiver.class);
	intent.setAction("com.example.ACTION_FOUND");
	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	
	BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
	scanner.stopScan(context, pendingIntent);
```


## License

The Scanner Compat library is available under BSD 3-Clause license. See the LICENSE file for more info.