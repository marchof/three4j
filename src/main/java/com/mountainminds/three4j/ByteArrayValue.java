/*******************************************************************************
 * Copyright (c) 2021 Mountainminds GmbH & Co. KG
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * SPDX-License-Identifier: MIT
 *******************************************************************************/
package com.mountainminds.three4j;

import java.util.Arrays;

/**
 * Internal base class for all byte[] based immutable types. The purpose of the
 * subclasses is to provide type safety at the API.
 */
public abstract class ByteArrayValue {

	private final byte[] value;

	ByteArrayValue(byte[] value) {
		this.value = value;
	}

	ByteArrayValue(byte[] value, int valuesize) throws IllegalArgumentException {
		this(value);
		if (value.length != valuesize) {
			throw new IllegalArgumentException("Illegal value size: " + value.length + " bytes");
		}
	}

	/**
	 * @return encapsulated byte array value
	 */
	public byte[] getValue() {
		return value;
	}

	/**
	 * @return encapsulated byte array as hex string
	 */
	public String getHexValue() {
		return toHex(value);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !this.getClass().equals(other.getClass())) {
			return false;
		}
		return Arrays.equals(value, ((ByteArrayValue) other).value);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + toHex(value) + "]";
	}

	/**
	 * Converts a hex string into a byte array.
	 * 
	 * @param hex hex string
	 * @return byte array (half the size than hex digits)
	 */
	static byte[] fromHex(String hex) {
		var bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			var d1 = Character.digit(hex.charAt(2 * i), 16);
			var d2 = Character.digit(hex.charAt(2 * i + 1), 16);
			bytes[i] = (byte) ((d1 << 4) | d2);
		}
		return bytes;
	}

	/**
	 * Converts a byte array into its hex representation
	 * 
	 * @param bytes byte array of arbitrary size
	 * @return hex string (twice as many digits as bytes)
	 */
	static String toHex(byte[] bytes) {
		var sb = new StringBuilder();
		for (var b : bytes) {
			sb.append(Character.forDigit((b & 0xff) >> 4, 16));
			sb.append(Character.forDigit(b & 0xf, 16));
		}
		return sb.toString();
	}

}
