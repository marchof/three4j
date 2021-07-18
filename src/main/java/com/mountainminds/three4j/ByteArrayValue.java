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

	private byte[] value;

	ByteArrayValue(byte[] value, int valuesize) throws IllegalArgumentException {
		if (value.length != valuesize) {
			throw new IllegalArgumentException("Illegal value size: " + value.length + " bytes");
		}
		this.value = value;
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
		return KeyEncoder.toHex(value);
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
		return getClass().getSimpleName() + "[" + KeyEncoder.toHex(value) + "]";
	}

}
