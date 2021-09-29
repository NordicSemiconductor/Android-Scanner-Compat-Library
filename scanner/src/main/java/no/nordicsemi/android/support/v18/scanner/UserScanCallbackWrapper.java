package no.nordicsemi.android.support.v18.scanner;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class wraps the {@link ScanCallback} object given by the user and holds a weak reference
 * to it. This prevents from leaking object if the callbacks are held by Activities,
 * Fragments or View Models.
 *
 * See https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library/issues/109
 */
/* package */ class UserScanCallbackWrapper extends ScanCallback {
	private final WeakReference<ScanCallback> weakScanCallback;

	UserScanCallbackWrapper(@NonNull final ScanCallback userCallback) {
		weakScanCallback = new WeakReference<>(userCallback);
	}

	boolean isDead() {
		return weakScanCallback.get() == null;
	}

	@Nullable
	ScanCallback get() {
		return weakScanCallback.get();
	}

	@Override
	public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
		final ScanCallback userCallback = weakScanCallback.get();
		if (userCallback != null)
			userCallback.onScanResult(callbackType, result);
	}

	@Override
	public void onBatchScanResults(@NonNull final List<ScanResult> results) {
		final ScanCallback userCallback = weakScanCallback.get();
		if (userCallback != null)
			userCallback.onBatchScanResults(results);
	}

	@Override
	public void onScanFailed(final int errorCode) {
		final ScanCallback userCallback = weakScanCallback.get();
		if (userCallback != null)
			userCallback.onScanFailed(errorCode);
	}
}