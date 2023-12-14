package no.nordicsemi.android.support.v18.scanner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class ScanCallbackWrapperSet<W extends BluetoothLeScannerCompat.ScanCallbackWrapper> {
	@NonNull
	private final Set<W> wrappers = new HashSet<>();

	@NonNull
	public Set<W> values() {
		return wrappers;
	}

	boolean isEmpty() {
		return wrappers.isEmpty();
	}

	void add(@NonNull final W wrapper) {
		wrappers.add(wrapper);
	}

	boolean contains(@NonNull final ScanCallback callback) {
		for (final W wrapper : wrappers) {
			if (wrapper.scanCallback == callback) {
				return true;
			}
			if (wrapper.scanCallback instanceof UserScanCallbackWrapper) {
				final UserScanCallbackWrapper callbackWrapper = (UserScanCallbackWrapper) wrapper.scanCallback;
				if (callbackWrapper.get() == callback) {
					return true;
				}
			}
		}
		return false;
	}

	@Nullable
	W get(@NonNull final ScanCallback callback) {
		for (final W wrapper : wrappers) {
			if (wrapper.scanCallback == callback) {
				return wrapper;
			}
			if (wrapper.scanCallback instanceof UserScanCallbackWrapper) {
				final UserScanCallbackWrapper callbackWrapper = (UserScanCallbackWrapper) wrapper.scanCallback;
				if (callbackWrapper.get() == callback) {
					return wrapper;
				}
			}
		}
		return null;
	}

	@Nullable
	W remove(@NonNull final ScanCallback callback) {
		for (final W wrapper : wrappers) {
			if (wrapper.scanCallback == callback) {
				return wrapper;
			}
			if (wrapper.scanCallback instanceof UserScanCallbackWrapper) {
				final UserScanCallbackWrapper callbackWrapper = (UserScanCallbackWrapper) wrapper.scanCallback;
				if (callbackWrapper.get() == callback) {
					wrappers.remove(wrapper);
					return wrapper;
				}
			}
		}
		cleanUp();
		return null;
	}

	private void cleanUp() {
		final List<W> deadWrappers = new LinkedList<>();
		for (final W wrapper : wrappers) {
			if (wrapper.scanCallback instanceof UserScanCallbackWrapper) {
				final UserScanCallbackWrapper callbackWrapper = (UserScanCallbackWrapper) wrapper.scanCallback;
				if (callbackWrapper.isDead())
					deadWrappers.add(wrapper);
			}
		}
		for (final W wrapper : deadWrappers) {
			wrappers.remove(wrapper);
		}
	}
}
