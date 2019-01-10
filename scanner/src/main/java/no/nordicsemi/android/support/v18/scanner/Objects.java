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

import java.util.Arrays;

/* package */ class Objects {

	/**
	 * Returns true if both arguments are null,
	 * the result of {@link Arrays#equals} if both arguments are primitive arrays,
	 * the result of {@link Arrays#deepEquals} if both arguments are arrays of reference types,
	 * and the result of {@link #equals} otherwise.
	 */
	static boolean deepEquals(final Object a, final Object b) {
		if (a == null || b == null) {
			return a == b;
		} else if (a instanceof Object[] && b instanceof Object[]) {
			return Arrays.deepEquals((Object[]) a, (Object[]) b);
		} else if (a instanceof boolean[] && b instanceof boolean[]) {
			return Arrays.equals((boolean[]) a, (boolean[]) b);
		} else if (a instanceof byte[] && b instanceof byte[]) {
			return Arrays.equals((byte[]) a, (byte[]) b);
		} else if (a instanceof char[] && b instanceof char[]) {
			return Arrays.equals((char[]) a, (char[]) b);
		} else if (a instanceof double[] && b instanceof double[]) {
			return Arrays.equals((double[]) a, (double[]) b);
		} else if (a instanceof float[] && b instanceof float[]) {
			return Arrays.equals((float[]) a, (float[]) b);
		} else if (a instanceof int[] && b instanceof int[]) {
			return Arrays.equals((int[]) a, (int[]) b);
		} else if (a instanceof long[] && b instanceof long[]) {
			return Arrays.equals((long[]) a, (long[]) b);
		} else if (a instanceof short[] && b instanceof short[]) {
			return Arrays.equals((short[]) a, (short[]) b);
		}
		return a.equals(b);
	}

	/**
	 * Null-safe equivalent of {@code a.equals(b)}.
	 */
	static boolean equals(final Object a, final Object b) {
		return (a == null) ? (b == null) : a.equals(b);
	}

	/**
	 * Convenience wrapper for {@link Arrays#hashCode}, adding varargs.
	 * This can be used to compute a hash code for an object's fields as follows:
	 * {@code Objects.hash(a, b, c)}.
	 */
	static int hash(final Object... values) {
		return Arrays.hashCode(values);
	}

	/**
	 * Returns "null" for null or {@code o.toString()}.
	 */
	static String toString(final Object o) {
		return (o == null) ? "null" : o.toString();
	}
}
