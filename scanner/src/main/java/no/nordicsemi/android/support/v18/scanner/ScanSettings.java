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
	 */
	public static final int SCAN_MODE_OPPORTUNISTIC = -1;

	/**
	 * Perform Bluetooth LE scan in low power mode. This is the default scan mode as it consumes the
	 * least power.
	 */
	public static final int SCAN_MODE_LOW_POWER = 0;

	/**
	 * Perform Bluetooth LE scan in balanced power mode. Scan results are returned at a rate that
	 * provides a good trade-off between scan frequency and power consumption.
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
	 * Request full scan results which contain the device, rssi, advertising data, scan response
	 * as well as the scan timestamp.
	 */
	private static final int SCAN_RESULT_TYPE_FULL = 0;

	/**
	 * Request abbreviated scan results which contain the device, rssi and scan timestamp.
	 * <p>
	 * <b>Note:</b> It is possible for an application to get more scan results than it asked for, if
	 * there are multiple apps using this type.
	 */
	private static final int SCAN_RESULT_TYPE_ABBREVIATED = 1;

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
	private final long mPowerSaveScanInterval;
	private final long mPowerSaveRestInterval;

	// Bluetooth LE scan mode.
	private int mScanMode;

	// Bluetooth LE scan callback type
	private int mCallbackType;

	// Bluetooth LE scan result type
	private int mScanResultType;

	// Time of delay for reporting the scan result
	private long mReportDelayMillis;

	private int mMatchMode;

	private int mNumOfMatchesPerFilter;

	private boolean mUseHardwareFilteringIfSupported;

	private boolean mUseHardwareBatchingIfSupported;

	private boolean mUseHardwareCallbackTypesIfSupported;

	private long mMatchLostDeviceTimeout;

	private long mMatchLostTaskInterval;

	// Include only legacy advertising results
	private boolean mLegacy;

	private int mPhy;

	public int getScanMode() {
		return mScanMode;
	}

	public int getCallbackType() {
		return mCallbackType;
	}

	public int getScanResultType() {
		return mScanResultType;
	}

	public int getMatchMode() {
		return mMatchMode;
	}

	public int getNumOfMatches() {
		return mNumOfMatchesPerFilter;
	}

	public boolean getUseHardwareFilteringIfSupported() {
		return mUseHardwareFilteringIfSupported;
	}

	public boolean getUseHardwareBatchingIfSupported() {
		return mUseHardwareBatchingIfSupported;
	}

	public boolean getUseHardwareCallbackTypesIfSupported() {
		return mUseHardwareCallbackTypesIfSupported;
	}

	/**
	 * Some devices with Android Marshmallow (Nexus 6) theoretically support other callback types,
	 * but call {@link android.bluetooth.le.ScanCallback#onScanFailed(int)} with error = 5.
	 * In that case the Scanner Compat will disable the hardware support and start using compat
	 * mechanism.
	 */
	/* package */ void disableUseHardwareCallbackTypes() {
		mUseHardwareCallbackTypesIfSupported = false;
	}

	public long getMatchLostDeviceTimeout() {
		return mMatchLostDeviceTimeout;
	}

	public long getMatchLostTaskInterval() {
		return mMatchLostTaskInterval;
	}

	/**
	 * Returns whether only legacy advertisements will be returned.
	 * Legacy advertisements include advertisements as specified
	 * by the Bluetooth core specification 4.2 and below.
	 */
	public boolean getLegacy() {
		return mLegacy;
	}

	/**
	 * Returns the physical layer used during a scan.
	 */
	public int getPhy() {
		return mPhy;
	}

	/**
	 * Returns report delay timestamp based on the device clock.
	 */
	public long getReportDelayMillis() {
		return mReportDelayMillis;
	}

	private ScanSettings(int scanMode, int callbackType, int scanResultType,
						 long reportDelayMillis, int matchMode,
						 int numOfMatchesPerFilter, boolean legacy, int phy,
						 boolean hardwareFiltering, boolean hardwareBatching,
						 boolean hardwareCallbackTypes, long matchTimeout, long taskInterval,
						 long powerSaveScanInterval, long powerSaveRestInterval) {
		mScanMode = scanMode;
		mCallbackType = callbackType;
		mScanResultType = scanResultType;
		mReportDelayMillis = reportDelayMillis;
		mNumOfMatchesPerFilter = numOfMatchesPerFilter;
		mMatchMode = matchMode;
		mLegacy = legacy;
		mPhy = phy;
		mUseHardwareFilteringIfSupported = hardwareFiltering;
		mUseHardwareBatchingIfSupported = hardwareBatching;
		mUseHardwareCallbackTypesIfSupported = hardwareCallbackTypes;
		mMatchLostDeviceTimeout = matchTimeout * 1000000L; // convert to nanos
		mMatchLostTaskInterval = taskInterval;
		mPowerSaveScanInterval = powerSaveScanInterval;
		mPowerSaveRestInterval = powerSaveRestInterval;
	}

	private ScanSettings(Parcel in) {
		mScanMode = in.readInt();
		mCallbackType = in.readInt();
		mScanResultType = in.readInt();
		mReportDelayMillis = in.readLong();
		mMatchMode = in.readInt();
		mNumOfMatchesPerFilter = in.readInt();
		mLegacy = in.readInt() != 0;
		mPhy = in.readInt();
		mUseHardwareFilteringIfSupported = in.readInt() == 1;
		mUseHardwareBatchingIfSupported = in.readInt() == 1;
		mPowerSaveScanInterval = in.readLong();
		mPowerSaveRestInterval = in.readLong();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mScanMode);
		dest.writeInt(mCallbackType);
		dest.writeInt(mScanResultType);
		dest.writeLong(mReportDelayMillis);
		dest.writeInt(mMatchMode);
		dest.writeInt(mNumOfMatchesPerFilter);
		dest.writeInt(mLegacy ? 1 : 0);
		dest.writeInt(mPhy);
		dest.writeInt(mUseHardwareFilteringIfSupported ? 1 : 0);
		dest.writeInt(mUseHardwareBatchingIfSupported ? 1 : 0);
		dest.writeLong(mPowerSaveScanInterval);
		dest.writeLong(mPowerSaveRestInterval);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ScanSettings>
			CREATOR = new Creator<ScanSettings>() {
		@Override
		public ScanSettings[] newArray(int size) {
			return new ScanSettings[size];
		}

		@Override
		public ScanSettings createFromParcel(Parcel in) {
			return new ScanSettings(in);
		}
	};

	/**
	 * Determine if we should do power-saving sleep on pre-Lollipop
     */
	public boolean hasPowerSaveMode() {
		return mPowerSaveRestInterval > 0 && mPowerSaveScanInterval > 0;
	}

	public long getPowerSaveRest() {
		return mPowerSaveRestInterval;
	}

	public long getPowerSaveScan() {
		return mPowerSaveScanInterval;
	}

	/**
	 * Builder for {@link ScanSettings}.
	 */
	@SuppressWarnings({"UnusedReturnValue", "unused"})
	public static final class Builder {
		private int mScanMode = SCAN_MODE_LOW_POWER;
		private int mCallbackType = CALLBACK_TYPE_ALL_MATCHES;
		private int mScanResultType = SCAN_RESULT_TYPE_FULL;
		private long mReportDelayMillis = 0;
		private int mMatchMode = MATCH_MODE_AGGRESSIVE;
		private int mNumOfMatchesPerFilter  = MATCH_NUM_MAX_ADVERTISEMENT;
		private boolean mLegacy = true;
		private int mPhy = PHY_LE_ALL_SUPPORTED;
		private boolean mUseHardwareFilteringIfSupported = true;
		private boolean mUseHardwareBatchingIfSupported = true;
		private boolean mUseHardwareCallbackTypesIfSupported = true;
		private long mMatchLostDeviceTimeout = MATCH_LOST_DEVICE_TIMEOUT_DEFAULT;
		private long mMatchLostTaskInterval = MATCH_LOST_TASK_INTERVAL_DEFAULT;
		private long mPowerSaveRestInterval = 0;
		private long mPowerSaveScanInterval = 0;

		/**
		 * Set scan mode for Bluetooth LE scan.
		 *
		 * @param scanMode The scan mode can be one of {@link ScanSettings#SCAN_MODE_LOW_POWER},
		 *            {@link ScanSettings#SCAN_MODE_BALANCED} or
		 *            {@link ScanSettings#SCAN_MODE_LOW_LATENCY}.
		 * @throws IllegalArgumentException If the {@code scanMode} is invalid.
		 */
		public Builder setScanMode(int scanMode) {
			if (scanMode < SCAN_MODE_OPPORTUNISTIC || scanMode > SCAN_MODE_LOW_LATENCY) {
				throw new IllegalArgumentException("invalid scan mode " + scanMode);
			}
			mScanMode = scanMode;
			return this;
		}

		/**
		 * Set callback type for Bluetooth LE scan.
		 *
		 * @param callbackType The callback type flags for the scan.
		 * @throws IllegalArgumentException If the {@code callbackType} is invalid.
		 */
		public Builder setCallbackType(int callbackType) {
			if (!isValidCallbackType(callbackType)) {
				throw new IllegalArgumentException("invalid callback type - " + callbackType);
			}
			mCallbackType = callbackType;
			return this;
		}

		// Returns true if the callbackType is valid.
		private boolean isValidCallbackType(int callbackType) {
			if (callbackType == CALLBACK_TYPE_ALL_MATCHES ||
					callbackType == CALLBACK_TYPE_FIRST_MATCH ||
					callbackType == CALLBACK_TYPE_MATCH_LOST) {
				return true;
			}
			return callbackType == (CALLBACK_TYPE_FIRST_MATCH | CALLBACK_TYPE_MATCH_LOST);
		}

		/**
		 * Set scan result type for Bluetooth LE scan.
		 *
		 * @param scanResultType Type for scan result, could be either
		 *            {@link ScanSettings#SCAN_RESULT_TYPE_FULL} or
		 *            {@link ScanSettings#SCAN_RESULT_TYPE_ABBREVIATED}.
		 * @throws IllegalArgumentException If the {@code scanResultType} is invalid.
		 */
		public Builder setScanResultType(int scanResultType) {
			if (scanResultType < SCAN_RESULT_TYPE_FULL
					|| scanResultType > SCAN_RESULT_TYPE_ABBREVIATED) {
				throw new IllegalArgumentException(
						"invalid scanResultType - " + scanResultType);
			}
			mScanResultType = scanResultType;
			return this;
		}

		/**
		 * Set report delay timestamp for Bluetooth LE scan.
		 *
		 * @param reportDelayMillis Delay of report in milliseconds. Set to 0 to be notified of
		 *            results immediately. Values &gt; 0 causes the scan results to be queued up and
		 *            delivered after the requested delay or when the internal buffers fill up.
		 * @throws IllegalArgumentException If {@code reportDelayMillis} &lt; 0.
		 */
		public Builder setReportDelay(long reportDelayMillis) {
			if (reportDelayMillis < 0) {
				throw new IllegalArgumentException("reportDelay must be > 0");
			}
			mReportDelayMillis = reportDelayMillis;
			return this;
		}

		/**
		 * Set the number of matches for Bluetooth LE scan filters hardware match
		 *
		 * @param numOfMatches The num of matches can be one of
		 *              {@link ScanSettings#MATCH_NUM_ONE_ADVERTISEMENT} or
		 *              {@link ScanSettings#MATCH_NUM_FEW_ADVERTISEMENT} or
		 *              {@link ScanSettings#MATCH_NUM_MAX_ADVERTISEMENT}
		 * @throws IllegalArgumentException If the {@code matchMode} is invalid.
		 */
		public Builder setNumOfMatches(int numOfMatches) {
			if (numOfMatches < MATCH_NUM_ONE_ADVERTISEMENT
					|| numOfMatches > MATCH_NUM_MAX_ADVERTISEMENT) {
				throw new IllegalArgumentException("invalid numOfMatches " + numOfMatches);
			}
			mNumOfMatchesPerFilter = numOfMatches;
			return this;
		}

		/**
		 * Set match mode for Bluetooth LE scan filters hardware match
		 *
		 * @param matchMode The match mode can be one of
		 *              {@link ScanSettings#MATCH_MODE_AGGRESSIVE} or
		 *              {@link ScanSettings#MATCH_MODE_STICKY}
		 * @throws IllegalArgumentException If the {@code matchMode} is invalid.
		 */
		public Builder setMatchMode(int matchMode) {
			if (matchMode < MATCH_MODE_AGGRESSIVE
					|| matchMode > MATCH_MODE_STICKY) {
				throw new IllegalArgumentException("invalid matchMode " + matchMode);
			}
			mMatchMode = matchMode;
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
		public Builder setLegacy(boolean legacy) {
			mLegacy = legacy;
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
		 *   {@link BluetoothDevice#PHY_LE_1M},
		 *   {@link BluetoothDevice#PHY_LE_CODED} or
		 *   {@link ScanSettings#PHY_LE_ALL_SUPPORTED}
		 */
		public Builder setPhy(int phy) {
			mPhy = phy;
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
		 *                 If false a compat software filtering will be used
		 *                 (uses much more resources).
		 */
		public Builder setUseHardwareFilteringIfSupported(boolean use) {
			mUseHardwareFilteringIfSupported = use;
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
		 *                 False to always use compat mechanism.
		 */
		public Builder setUseHardwareBatchingIfSupported(boolean use) {
			mUseHardwareBatchingIfSupported = use;
			return this;
		}

		/**
		 * This method may be used when callback type is set to a value different than
		 * {@link #CALLBACK_TYPE_ALL_MATCHES}. When disabled, the Scanner Compat itself will
		 * take care of reporting first match and match lost. The compat behaviour may differ
		 * from the one natively supported on Android Marshmallow.
		 *
		 * Also, in compat mode values set by {@link #setMatchMode(int)} and
		 * {@link #setNumOfMatches(int)} are ignored.
		 * Instead use {@link #setMatchOptions(long, long)} to set timer options.
		 *
		 * @param use true to enable (default) the offloaded match reporting if hardware supports it,
		 *                 false to enable compat implementation.
		 */
		public Builder setUseHardwareCallbackTypesIfSupported(boolean use) {
			mUseHardwareCallbackTypesIfSupported = use;
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
		 * @param taskIntervalMillis the task interval (default {@link #MATCH_LOST_TASK_INTERVAL_DEFAULT}).
		 */
		public Builder setMatchOptions(final long deviceTimeoutMillis, final long taskIntervalMillis) {
			if (deviceTimeoutMillis <= 0 || taskIntervalMillis <= 0) {
				throw new IllegalArgumentException("maxDeviceAgeMillis and taskIntervalMillis must be > 0");
			}
			mMatchLostDeviceTimeout = deviceTimeoutMillis;
			mMatchLostTaskInterval = taskIntervalMillis;
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
		 *                              scanInterval.
		 */
		public Builder setPowerSave(final long scanInterval, final long restInterval) {
			if (scanInterval <= 0 || restInterval <= 0) {
				throw new IllegalArgumentException("scanInterval and restInterval must be > 0");
			}
			mPowerSaveScanInterval = scanInterval;
			mPowerSaveRestInterval = restInterval;
			return this;
		}

		/**
		 * Build {@link ScanSettings}.
		 */
		public ScanSettings build() {
			return new ScanSettings(mScanMode, mCallbackType, mScanResultType,
					mReportDelayMillis, mMatchMode,
					mNumOfMatchesPerFilter, mLegacy, mPhy, mUseHardwareFilteringIfSupported,
					mUseHardwareBatchingIfSupported, mUseHardwareCallbackTypesIfSupported,
					mMatchLostDeviceTimeout, mMatchLostTaskInterval,
					mPowerSaveScanInterval, mPowerSaveRestInterval);
		}
	}
}
