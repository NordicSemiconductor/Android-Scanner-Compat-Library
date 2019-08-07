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

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Bluetooth LE scan settings are passed to {@link BluetoothLeScannerCompat#startScan} to define the
 * parameters for the scan.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class ScanSettings implements Parcelable {

	/**
	 * The default value of the maximum time for the device not to be discoverable before it will be
	 * assumed lost.
	 */
	public static final long MATCH_LOST_DEVICE_TIMEOUT_DEFAULT = 10000L; // [ms]

	/**
	 * The default interval of the task that calls match lost events.
	 */
	public static final long MATCH_LOST_TASK_INTERVAL_DEFAULT = 10000L; // [ms]

	/**
	 * A special Bluetooth LE scan mode. Applications using this scan mode will passively listen for
	 * other scan results without starting BLE scans themselves.
	 * <p>
	 * On Android Lollipop {@link #SCAN_MODE_LOW_POWER} will be used instead, as opportunistic
	 * mode was not yet supported.
	 * <p>
	 * On pre-Lollipop devices it is possible to override the default intervals
	 * using {@link Builder#setPowerSave(long, long)}.
	 */
	public static final int SCAN_MODE_OPPORTUNISTIC = -1;

	/**
	 * Perform Bluetooth LE scan in low power mode. This is the default scan mode as it consumes the
	 * least power. This mode is enforced if the scanning application is not in foreground.
	 * <p>
	 * On pre-Lollipop devices this mode will be emulated by scanning for 0.5 second followed
	 * by 4.5 second of idle, which corresponds to the low power intervals on Lollipop or newer.
	 */
	public static final int SCAN_MODE_LOW_POWER = 0;

	/**
	 * Perform Bluetooth LE scan in balanced power mode. Scan results are returned at a rate that
	 * provides a good trade-off between scan frequency and power consumption.
	 * <p>
	 * On pre-Lollipop devices this mode will be emulated by scanning for 2 second followed
	 * by 3 seconds of idle, which corresponds to the low power intervals on Lollipop or newer.
	 */
	public static final int SCAN_MODE_BALANCED = 1;

	/**
	 * Scan using highest duty cycle. It's recommended to only use this mode when the application is
	 * running in the foreground.
	 */
	public static final int SCAN_MODE_LOW_LATENCY = 2;

	/**
	 * Trigger a callback for every Bluetooth advertisement found that matches the filter criteria.
	 * If no filter is active, all advertisement packets are reported.
	 */
	public static final int CALLBACK_TYPE_ALL_MATCHES = 1;

	/**
	 * A result callback is only triggered for the first advertisement packet received that matches
	 * the filter criteria.
	 */
	public static final int CALLBACK_TYPE_FIRST_MATCH = 2;

	/**
	 * Receive a callback when advertisements are no longer received from a device that has been
	 * previously reported by a first match callback.
	 */
	public static final int CALLBACK_TYPE_MATCH_LOST = 4;


	/*
	 * Determines how many advertisements to match per filter, as this is scarce hw resource
	 */
	/**
	 * Match one advertisement per filter
	 */
	public static final int MATCH_NUM_ONE_ADVERTISEMENT = 1;

	/**
	 * Match few advertisement per filter, depends on current capability and availability of
	 * the resources in hw
	 */
	public static final int MATCH_NUM_FEW_ADVERTISEMENT = 2;

	/**
	 * Match as many advertisement per filter as hw could allow, depends on current
	 * capability and availability of the resources in hw
	 */
	public static final int MATCH_NUM_MAX_ADVERTISEMENT = 3;

	/**
	 * In Aggressive mode, hw will determine a match sooner even with feeble signal strength
	 * and few number of sightings/match in a duration.
	 */
	public static final int MATCH_MODE_AGGRESSIVE = 1;

	/**
	 * For sticky mode, higher threshold of signal strength and sightings is required
	 * before reporting by hw
	 */
	public static final int MATCH_MODE_STICKY = 2;

	/**
	 * Use all supported PHYs for scanning.
	 * This will check the controller capabilities, and start
	 * the scan on 1Mbit and LE Coded PHYs if supported, or on
	 * the 1Mbit PHY only.
	 */
	public static final int PHY_LE_ALL_SUPPORTED = 255;

	/**
	 * Pre-Lollipop scanning requires a wakelock and the CPU cannot go to sleep.
	 * To conserve power we can optionally scan for a certain duration (scan interval)
	 * and then rest for a time before starting scanning again.
	 */
	private final long powerSaveScanInterval;
	private final long powerSaveRestInterval;

	// Bluetooth LE scan mode.
	private int scanMode;

	// Bluetooth LE scan callback type
	private int callbackType;

	// Time of delay for reporting the scan result
	private long reportDelayMillis;

	private int matchMode;

	private int numOfMatchesPerFilter;

	private boolean useHardwareFilteringIfSupported;

	private boolean useHardwareBatchingIfSupported;

	private boolean useHardwareCallbackTypesIfSupported;

	private long matchLostDeviceTimeout;

	private long matchLostTaskInterval;

	// Include only legacy advertising results
	private boolean legacy;

	private int phy;

	public int getScanMode() {
		return scanMode;
	}

	public int getCallbackType() {
		return callbackType;
	}

	public int getMatchMode() {
		return matchMode;
	}

	public int getNumOfMatches() {
		return numOfMatchesPerFilter;
	}

	public boolean getUseHardwareFilteringIfSupported() {
		return useHardwareFilteringIfSupported;
	}

	public boolean getUseHardwareBatchingIfSupported() {
		return useHardwareBatchingIfSupported;
	}

	public boolean getUseHardwareCallbackTypesIfSupported() {
		return useHardwareCallbackTypesIfSupported;
	}

	/**
	 * Some devices with Android Marshmallow (Nexus 6) theoretically support other callback types,
	 * but call {@link android.bluetooth.le.ScanCallback#onScanFailed(int)} with error = 5.
	 * In that case the Scanner Compat will disable the hardware support and start using compat
	 * mechanism.
	 */
	/* package */ void disableUseHardwareCallbackTypes() {
		useHardwareCallbackTypesIfSupported = false;
	}

	public long getMatchLostDeviceTimeout() {
		return matchLostDeviceTimeout;
	}

	public long getMatchLostTaskInterval() {
		return matchLostTaskInterval;
	}

	/**
	 * Returns whether only legacy advertisements will be returned.
	 * Legacy advertisements include advertisements as specified
	 * by the Bluetooth core specification 4.2 and below.
	 */
	public boolean getLegacy() {
		return legacy;
	}

	/**
	 * Returns the physical layer used during a scan.
	 */
	public int getPhy() {
		return phy;
	}

	/**
	 * Returns report delay timestamp based on the device clock.
	 */
	public long getReportDelayMillis() {
		return reportDelayMillis;
	}

	private ScanSettings(final int scanMode, final int callbackType,
						 final long reportDelayMillis, final int matchMode,
						 final int numOfMatchesPerFilter, final boolean legacy, final int phy,
						 final boolean hardwareFiltering, final boolean hardwareBatching,
						 final boolean hardwareCallbackTypes, final long matchTimeout,
						 final long taskInterval,
						 final long powerSaveScanInterval, final long powerSaveRestInterval) {
		this.scanMode = scanMode;
		this.callbackType = callbackType;
		this.reportDelayMillis = reportDelayMillis;
		this.numOfMatchesPerFilter = numOfMatchesPerFilter;
		this.matchMode = matchMode;
		this.legacy = legacy;
		this.phy = phy;
		this.useHardwareFilteringIfSupported = hardwareFiltering;
		this.useHardwareBatchingIfSupported = hardwareBatching;
		this.useHardwareCallbackTypesIfSupported = hardwareCallbackTypes;
		this.matchLostDeviceTimeout = matchTimeout * 1000000L; // convert to nanos
		this.matchLostTaskInterval = taskInterval;
		this.powerSaveScanInterval = powerSaveScanInterval;
		this.powerSaveRestInterval = powerSaveRestInterval;
	}

	private ScanSettings(final Parcel in) {
		scanMode = in.readInt();
		callbackType = in.readInt();
		reportDelayMillis = in.readLong();
		matchMode = in.readInt();
		numOfMatchesPerFilter = in.readInt();
		legacy = in.readInt() != 0;
		phy = in.readInt();
		useHardwareFilteringIfSupported = in.readInt() == 1;
		useHardwareBatchingIfSupported = in.readInt() == 1;
		powerSaveScanInterval = in.readLong();
		powerSaveRestInterval = in.readLong();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(scanMode);
		dest.writeInt(callbackType);
		dest.writeLong(reportDelayMillis);
		dest.writeInt(matchMode);
		dest.writeInt(numOfMatchesPerFilter);
		dest.writeInt(legacy ? 1 : 0);
		dest.writeInt(phy);
		dest.writeInt(useHardwareFilteringIfSupported ? 1 : 0);
		dest.writeInt(useHardwareBatchingIfSupported ? 1 : 0);
		dest.writeLong(powerSaveScanInterval);
		dest.writeLong(powerSaveRestInterval);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ScanSettings> CREATOR = new Creator<ScanSettings>() {
		@Override
		public ScanSettings[] newArray(final int size) {
			return new ScanSettings[size];
		}

		@Override
		public ScanSettings createFromParcel(final Parcel in) {
			return new ScanSettings(in);
		}
	};

	/**
	 * Determine if we should do power-saving sleep on pre-Lollipop
	 */
	public boolean hasPowerSaveMode() {
		return powerSaveRestInterval > 0 && powerSaveScanInterval > 0;
	}

	public long getPowerSaveRest() {
		return powerSaveRestInterval;
	}

	public long getPowerSaveScan() {
		return powerSaveScanInterval;
	}

	/**
	 * Builder for {@link ScanSettings}.
	 */
	@SuppressWarnings({"UnusedReturnValue", "unused"})
	public static final class Builder {
		private int scanMode = SCAN_MODE_LOW_POWER;
		private int callbackType = CALLBACK_TYPE_ALL_MATCHES;
		private long reportDelayMillis = 0;
		private int matchMode = MATCH_MODE_AGGRESSIVE;
		private int numOfMatchesPerFilter = MATCH_NUM_MAX_ADVERTISEMENT;
		private boolean legacy = true;
		private int phy = PHY_LE_ALL_SUPPORTED;
		private boolean useHardwareFilteringIfSupported = true;
		private boolean useHardwareBatchingIfSupported = true;
		private boolean useHardwareCallbackTypesIfSupported = true;
		private long matchLostDeviceTimeout = MATCH_LOST_DEVICE_TIMEOUT_DEFAULT;
		private long matchLostTaskInterval = MATCH_LOST_TASK_INTERVAL_DEFAULT;
		private long powerSaveRestInterval = 0;
		private long powerSaveScanInterval = 0;

		/**
		 * Set scan mode for Bluetooth LE scan.
		 * <p>
		 * {@link #SCAN_MODE_OPPORTUNISTIC} is supported on Android Marshmallow onwards.
		 * On Lollipop this mode will fall back {@link #SCAN_MODE_LOW_POWER}, which actually means
		 * that the library will start its own scan instead of relying on scans from other apps.
		 * This may have significant impact on battery usage.
		 * <p>
		 * On pre-Lollipop devices, the settings set by {@link #setPowerSave(long, long)}
		 * will be used. By default, the intervals are the same as for {@link #SCAN_MODE_LOW_POWER}.
		 *
		 * @param scanMode The scan mode can be one of {@link ScanSettings#SCAN_MODE_LOW_POWER},
		 *                 {@link #SCAN_MODE_BALANCED},
		 *                 {@link #SCAN_MODE_LOW_LATENCY} or
		 *                 {@link #SCAN_MODE_OPPORTUNISTIC}.
		 * @throws IllegalArgumentException If the {@code scanMode} is invalid.
		 */
		@NonNull
		public Builder setScanMode(final int scanMode) {
			if (scanMode < SCAN_MODE_OPPORTUNISTIC || scanMode > SCAN_MODE_LOW_LATENCY) {
				throw new IllegalArgumentException("invalid scan mode " + scanMode);
			}
			this.scanMode = scanMode;
			return this;
		}

		/**
		 * Set callback type for Bluetooth LE scan.
		 *
		 * @param callbackType The callback type flags for the scan.
		 * @throws IllegalArgumentException If the {@code callbackType} is invalid.
		 */
		@NonNull
		public Builder setCallbackType(final int callbackType) {
			if (!isValidCallbackType(callbackType)) {
				throw new IllegalArgumentException("invalid callback type - " + callbackType);
			}
			this.callbackType = callbackType;
			return this;
		}

		// Returns true if the callbackType is valid.
		private boolean isValidCallbackType(final int callbackType) {
			if (callbackType == CALLBACK_TYPE_ALL_MATCHES ||
					callbackType == CALLBACK_TYPE_FIRST_MATCH ||
					callbackType == CALLBACK_TYPE_MATCH_LOST) {
				return true;
			}
			return callbackType == (CALLBACK_TYPE_FIRST_MATCH | CALLBACK_TYPE_MATCH_LOST);
		}

		/**
		 * Set report delay timestamp for Bluetooth LE scan.
		 *
		 * @param reportDelayMillis Delay of report in milliseconds. Set to 0 to be notified of
		 *                          results immediately. Values &gt; 0 causes the scan results
		 *                          to be queued up and delivered after the requested delay or
		 *                          when the internal buffers fill up.<p>
		 *                          For delays below 5000 ms (5 sec) the
		 *                          {@link ScanCallback#onBatchScanResults(List)}
		 *                          will be called in unreliable intervals, but starting from
		 *                          around 5000 the intervals get even.
		 * @throws IllegalArgumentException If {@code reportDelayMillis} &lt; 0.
		 */
		@NonNull
		public Builder setReportDelay(final long reportDelayMillis) {
			if (reportDelayMillis < 0) {
				throw new IllegalArgumentException("reportDelay must be > 0");
			}
			this.reportDelayMillis = reportDelayMillis;
			return this;
		}

		/**
		 * Set the number of matches for Bluetooth LE scan filters hardware match.
		 *
		 * @param numOfMatches The num of matches can be one of
		 *                     {@link ScanSettings#MATCH_NUM_ONE_ADVERTISEMENT} or
		 *                     {@link ScanSettings#MATCH_NUM_FEW_ADVERTISEMENT} or
		 *                     {@link ScanSettings#MATCH_NUM_MAX_ADVERTISEMENT}
		 * @throws IllegalArgumentException If the {@code matchMode} is invalid.
		 */
		@NonNull
		public Builder setNumOfMatches(final int numOfMatches) {
			if (numOfMatches < MATCH_NUM_ONE_ADVERTISEMENT
					|| numOfMatches > MATCH_NUM_MAX_ADVERTISEMENT) {
				throw new IllegalArgumentException("invalid numOfMatches " + numOfMatches);
			}
			numOfMatchesPerFilter = numOfMatches;
			return this;
		}

		/**
		 * Set match mode for Bluetooth LE scan filters hardware match
		 *
		 * @param matchMode The match mode can be one of
		 *                  {@link ScanSettings#MATCH_MODE_AGGRESSIVE} or
		 *                  {@link ScanSettings#MATCH_MODE_STICKY}
		 * @throws IllegalArgumentException If the {@code matchMode} is invalid.
		 */
		@NonNull
		public Builder setMatchMode(final int matchMode) {
			if (matchMode < MATCH_MODE_AGGRESSIVE
					|| matchMode > MATCH_MODE_STICKY) {
				throw new IllegalArgumentException("invalid matchMode " + matchMode);
			}
			this.matchMode = matchMode;
			return this;
		}

		/**
		 * Set whether only legacy advertisements should be returned in scan results.
		 * Legacy advertisements include advertisements as specified by the
		 * Bluetooth core specification 4.2 and below. This is true by default
		 * for compatibility with older apps.
		 *
		 * @param legacy true if only legacy advertisements will be returned
		 */
		@NonNull
		public Builder setLegacy(final boolean legacy) {
			this.legacy = legacy;
			return this;
		}

		/**
		 * Set the Physical Layer to use during this scan.
		 * This is used only if {@link ScanSettings.Builder#setLegacy}
		 * is set to false and only on Android 0reo or newer.
		 * {@link android.bluetooth.BluetoothAdapter#isLeCodedPhySupported}
		 * may be used to check whether LE Coded phy is supported by calling
		 * {@link android.bluetooth.BluetoothAdapter#isLeCodedPhySupported}.
		 * Selecting an unsupported phy will result in failure to start scan.
		 *
		 * @param phy Can be one of
		 *            {@link BluetoothDevice#PHY_LE_1M},
		 *            {@link BluetoothDevice#PHY_LE_CODED} or
		 *            {@link ScanSettings#PHY_LE_ALL_SUPPORTED}
		 */
		@NonNull
		public Builder setPhy(final int phy) {
			this.phy = phy;
			return this;
		}

		/**
		 * Several phones may have some issues when it comes to offloaded filtering.
		 * Even if it should be supported, it may not work as expected.
		 * It has been observed for example, that setting 2 filters with different devices
		 * addresses on Nexus 6 with Lollipop gives no callbacks if one or both devices advertise.
		 * See https://code.google.com/p/android/issues/detail?id=181561.
		 *
		 * @param use true to enable (default) hardware offload filtering.
		 *            If false a compat software filtering will be used
		 *            (uses much more resources).
		 */
		@NonNull
		public Builder setUseHardwareFilteringIfSupported(final boolean use) {
			useHardwareFilteringIfSupported = use;
			return this;
		}

		/**
		 * Some devices, for example Samsung S6 and S6 Edge with Lollipop, return always
		 * the same RSSI value for all devices if offloaded batching is used.
		 * Batching may also be emulated using a compat mechanism - a periodically called timer.
		 * Timer approach requires more resources but reports devices in constant delays
		 * and works on devices that does not support offloaded batching.
		 * In comparison, when setReportDelay(..) is called with parameter 1000 the standard,
		 * hardware triggered callback will be called every 1500ms +-200ms.
		 *
		 * @param use true to enable (default) hardware offloaded batching if they are supported.
		 *            False to always use compat mechanism.
		 */
		@NonNull
		public Builder setUseHardwareBatchingIfSupported(final boolean use) {
			useHardwareBatchingIfSupported = use;
			return this;
		}

		/**
		 * This method may be used when callback type is set to a value different than
		 * {@link #CALLBACK_TYPE_ALL_MATCHES}. When disabled, the Scanner Compat itself will
		 * take care of reporting first match and match lost. The compat behaviour may differ
		 * from the one natively supported on Android Marshmallow or newer.
		 * <p>
		 * Also, in compat mode values set by {@link #setMatchMode(int)} and
		 * {@link #setNumOfMatches(int)} are ignored.
		 * Instead use {@link #setMatchOptions(long, long)} to set timer options.
		 *
		 * @param use true to enable (default) the offloaded match reporting if hardware supports it,
		 *            false to enable compat implementation.
		 */
		@NonNull
		public Builder setUseHardwareCallbackTypesIfSupported(final boolean use) {
			useHardwareCallbackTypesIfSupported = use;
			return this;
		}

		/**
		 * The match options are used when the callback type has been set to
		 * {@link ScanSettings#CALLBACK_TYPE_FIRST_MATCH} or
		 * {@link ScanSettings#CALLBACK_TYPE_MATCH_LOST} and hardware does not support those types.
		 * In that case {@link BluetoothLeScannerCompat} starts a task that runs periodically
		 * and calls {@link ScanCallback#onScanResult(int, ScanResult)} with type
		 * {@link #CALLBACK_TYPE_MATCH_LOST} if a device has not been seen for at least given time.
		 *
		 * @param deviceTimeoutMillis the time required for the device to be recognized as lost
		 *                            (default {@link #MATCH_LOST_DEVICE_TIMEOUT_DEFAULT}).
		 * @param taskIntervalMillis  the task interval (default {@link #MATCH_LOST_TASK_INTERVAL_DEFAULT}).
		 */
		@NonNull
		public Builder setMatchOptions(final long deviceTimeoutMillis, final long taskIntervalMillis) {
			if (deviceTimeoutMillis <= 0 || taskIntervalMillis <= 0) {
				throw new IllegalArgumentException("maxDeviceAgeMillis and taskIntervalMillis must be > 0");
			}
			matchLostDeviceTimeout = deviceTimeoutMillis;
			matchLostTaskInterval = taskIntervalMillis;
			return this;
		}

		/**
		 * Pre-Lollipop scanning requires a wakelock and the CPU cannot go to sleep.
		 * To conserve power we can optionally scan for a certain duration (scan interval)
		 * and then rest for a time before starting scanning again. Won't affect Lollipop
		 * or later devices.
		 *
		 * @param scanInterval interval in ms to scan at a time.
		 * @param restInterval interval to sleep for without scanning before scanning again for
		 *                     scanInterval.
		 */
		@NonNull
		public Builder setPowerSave(final long scanInterval, final long restInterval) {
			if (scanInterval <= 0 || restInterval <= 0) {
				throw new IllegalArgumentException("scanInterval and restInterval must be > 0");
			}
			powerSaveScanInterval = scanInterval;
			powerSaveRestInterval = restInterval;
			return this;
		}

		/**
		 * Build {@link ScanSettings}.
		 */
		@NonNull
		public ScanSettings build() {
			if (powerSaveRestInterval == 0 && powerSaveScanInterval == 0)
				updatePowerSaveSettings();

			return new ScanSettings(scanMode, callbackType, reportDelayMillis, matchMode,
					numOfMatchesPerFilter, legacy, phy, useHardwareFilteringIfSupported,
					useHardwareBatchingIfSupported, useHardwareCallbackTypesIfSupported,
					matchLostDeviceTimeout, matchLostTaskInterval,
					powerSaveScanInterval, powerSaveRestInterval);
		}

		/**
		 * Sets power save settings based on the scan mode selected.
		 */
		private void updatePowerSaveSettings() {
			switch (scanMode) {
				case SCAN_MODE_LOW_LATENCY:
					// Disable power save mode
					powerSaveScanInterval = 0;
					powerSaveRestInterval = 0;
					break;
				case SCAN_MODE_BALANCED:
					// Scan for 2 seconds every 5 seconds
					powerSaveScanInterval = 2000;
					powerSaveRestInterval = 3000;
					break;
				case SCAN_MODE_OPPORTUNISTIC:
					// It is not possible to emulate OPPORTUNISTIC scanning, but in theory
					// that should be even less battery consuming than LOW_POWER.
					// For pre-Lollipop devices intervals can be overwritten by
					// setPowerSave(long, long) if needed.

					// On Android Lollipop the native SCAN_MODE_LOW_POWER will be used instead
					// of power save values.
				case SCAN_MODE_LOW_POWER:
				default:
					// Scan for 0.5 second every 5 seconds
					powerSaveScanInterval = 500;
					powerSaveRestInterval = 4500;
					break;
			}
		}
	}
}
