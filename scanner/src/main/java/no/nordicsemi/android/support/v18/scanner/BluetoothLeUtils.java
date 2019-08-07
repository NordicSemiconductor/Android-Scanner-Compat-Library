/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.nordicsemi.android.support.v18.scanner;

import android.util.SparseArray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

/**
 * Helper class for Bluetooth LE utils.
 */
/* package */
@SuppressWarnings("unused")
class BluetoothLeUtils {

	/**
	 * Returns a string composed from a {@link SparseArray}.
	 */
	static String toString(@Nullable final SparseArray<byte[]> array) {
		if (array == null) {
			return "null";
		}
		if (array.size() == 0) {
			return "{}";
		}
		final StringBuilder buffer = new StringBuilder();
		buffer.append('{');
		for (int i = 0; i < array.size(); ++i) {
			buffer.append(array.keyAt(i)).append("=").append(Arrays.toString(array.valueAt(i)));
		}
		buffer.append('}');
		return buffer.toString();
	}

	/**
	 * Returns a string composed from a {@link Map}.
	 */
	static <T> String toString(@Nullable final Map<T, byte[]> map) {
		if (map == null) {
			return "null";
		}
		if (map.isEmpty()) {
			return "{}";
		}
		final StringBuilder buffer = new StringBuilder();
		buffer.append('{');
		final Iterator<Map.Entry<T, byte[]>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<T, byte[]> entry = it.next();
			final Object key = entry.getKey();
			buffer.append(key).append("=").append(Arrays.toString(map.get(key)));
			if (it.hasNext()) {
				buffer.append(", ");
			}
		}
		buffer.append('}');
		return buffer.toString();
	}

	/**
	 * Check whether two {@link SparseArray} equal.
	 */
	static boolean equals(@Nullable final SparseArray<byte[]> array,
						  @Nullable final SparseArray<byte[]> otherArray) {
		if (array == otherArray) {
			return true;
		}
		if (array == null || otherArray == null) {
			return false;
		}
		if (array.size() != otherArray.size()) {
			return false;
		}

		// Keys are guaranteed in ascending order when indices are in ascending order.
		for (int i = 0; i < array.size(); ++i) {
			if (array.keyAt(i) != otherArray.keyAt(i) ||
					!Arrays.equals(array.valueAt(i), otherArray.valueAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check whether two {@link Map} equal.
	 */
	static <T> boolean equals(@Nullable final Map<T, byte[]> map, Map<T, byte[]> otherMap) {
		if (map == otherMap) {
			return true;
		}
		if (map == null || otherMap == null) {
			return false;
		}
		if (map.size() != otherMap.size()) {
			return false;
		}
		Set<T> keys = map.keySet();
		if (!keys.equals(otherMap.keySet())) {
			return false;
		}
		for (T key : keys) {
			if (!Objects.deepEquals(map.get(key), otherMap.get(key))) {
				return false;
			}
		}
		return true;
	}

}