# Android BLE Scanner Compat library

[ ![Download](https://api.bintray.com/packages/nordic/android/no.nordicsemi.android.support.v18%3Ascanner/images/download.svg) ](https://bintray.com/nordic/android/no.nordicsemi.android.support.v18%3Ascanner/_latestVersion)

The Scanner Compat library solves the problem with scanning for Bluetooth Low Energy devices on Android. 
The scanner API, initially created in Android 4.3, has changed in Android 5.0 and has been extended in 6.0 and 8.0. 
This library allows to use modern API even on older phones, emulating not supported features. If a feature 
(for example offloaded filtering or batching) is not available natively, it will be emulated by 
the compat library. Also, native filtering, batching and reporting first match or match lost may
be disabled if you find them not working on some devices. Advertising Extension (`ScanSetting#setLegacy(boolean)` 
or `setPhy(int)`) is available only on Android Oreo or newer and such calls will be ignored on 
older platforms where only legacy advertising packets on PHY LE 1M will be reported, 
due to the Bluetooth chipset capabilities.

### Background scanning

`SCAN_MODE_LOW_POWER` or `SCAN_MODE_OPPORTUNISTIC` should be used when scanning in background.
Note, that newer Android versions will enforce using low power mode in background, even if another one has been set.
This library allows to emulate [scanning with PendingIntent](https://developer.android.com/reference/android/bluetooth/le/BluetoothLeScanner.html#startScan(java.util.List%3Candroid.bluetooth.le.ScanFilter%3E,%20android.bluetooth.le.ScanSettings,%20android.app.PendingIntent))
on pre-Oreo devices by starting a background service that will scan with requested scan mode. 
This is much less battery friendly than when the original method is used, but works and saves
a lot of development time if such feature should be implemented anyway. Please read below 
for more details.

## Usage

The compat library may be found on jcenter repository. Add it to your project by adding the 
following dependency:

```Groovy
implementation 'no.nordicsemi.android.support.v18:scanner:1.4.0'
```

Projects not migrated to Android Jetpack should use version 1.3.1, which is feature-equal to 1.4.0.

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

### Scanning modes

There are 4 scanning modes available in native [ScanSettings](https://developer.android.com/reference/android/bluetooth/le/ScanSettings).
3 of them are available since Android Lollipop while the opportunistic scan mode has been added in Marshmallow.
This library tries to emulate them on platforms where they are not supported natively.
1. [SCAN_MODE_LOW_POWER](https://developer.android.com/reference/android/bluetooth/le/ScanSettings#SCAN_MODE_LOW_POWER) - 
Perform Bluetooth LE scan in low power mode. This is the default scan mode as it consumes the least power. 
The scanner will scan for 0.5 second and rest for 4.5 seconds. A Bluetooth LE device should advertise 
very often (at least once per 100 ms) in order to be found with this mode, otherwise the scanning interval may miss some or even all 
advertising events. This mode may be enforced if the scanning application is not in foreground.
2. [SCAN_MODE_BALANCED](https://developer.android.com/reference/android/bluetooth/le/ScanSettings#SCAN_MODE_BALANCED) - 
Perform Bluetooth LE scan in balanced power mode. Scan results are returned at a rate that provides a 
good trade-off between scan frequency and power consumption. The scanner will scan for 2 seconds followed
by 3 seconds of idle.
3. [SCAN_MODE_LOW_LATENCY](https://developer.android.com/reference/android/bluetooth/le/ScanSettings#SCAN_MODE_LOW_LATENCY) -
Scan using highest duty cycle. It's recommended to only use this mode when the application is running in the foreground.
4. [SCAN_MODE_OPPORTUNISTIC](https://developer.android.com/reference/android/bluetooth/le/ScanSettings#SCAN_MODE_OPPORTUNISTIC) - 
A special Bluetooth LE scan mode. Applications using this scan mode will passively listen for other scan results 
without starting BLE scans themselves.

3 first modes are emulated on Android 4.3 and 4.4.x by starting a handler task that scans for a period of time
and rests in between. To set scanning and rest intervals use `Builder#setPowerSave(long,long)`.

Opportunistic scanning is not possible to emulate and will fallback to `SCAN_MODE_LOW_POWER` on Lollipop and
power save settings on pre-Lollipop devices. That means that this library actually will initiate scanning 
on its own. This may have impact on battery consumption and should be used with care.

### Scan filters and batching

Offloaded filtering is available on Lollipop or newer devices where 
[BluetoothAdapter#isOffloadedFilteringSupported()](https://developer.android.com/reference/android/bluetooth/BluetoothAdapter.html#isOffloadedFilteringSupported())
returns *true* (when Bluetooth is enabled). If it is not supported, this library will scan without a filter and
apply the filter to the results. If you find offloaded filtering unreliable you may force using compat filtering by calling
`Builder#useHardwareFilteringIfSupported(false)`. Keep in mind that, newer Android versions may prohibit 
background scanning without native filters to save battery, so this method should be used with care.

Android Scanner Compat Library may also emulate batching. To enable scan batching call `Builder#setScanDelay(interval)`
with an interval greater than 0. For intervals less 5 seconds the actual interval may vary.
If you want to get results in lower intervals, call `Builder#useHardwareBatchingIfSupported(false)`, which will
start a normal scan and report results in given interval. Emulated batching uses significantly more battery
than offloaded as it wakes CPU with every device found.

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

**Note:** Android versions 6 and 7 will not report any advertising packets when in Doze mode.
Read more about it here: https://developer.android.com/training/monitoring-device-state/doze-standby

## Background scanning guidelines

To save power it is recommended to use as low power settings as possible and and use filters.
However, the more battery friendly settings are used, the longest time to finding a device.
In general, scanning with `PendingIntent` and `SCAN_MODE_LOW_POWER` or `SCAN_MODE_OPPORTUNISTIC`
should be used, together with report delay set and filters used.
`useHardwareFilteringIfSupported` and `useHardwareBatchingIfSupported` should be set to *true* (default).

Background scanning on Android 4.3 and 4.4.x will use a lot of power, as all those properties 
will have to be emulated. It is recommended to scan in background only on Lollipop or newer, or
even Oreo or newer devices and giving the user an option to disable this feature.

## License

The Scanner Compat library is available under BSD 3-Clause license. See the LICENSE file for more info.