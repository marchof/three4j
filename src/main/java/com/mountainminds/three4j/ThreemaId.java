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

/**
 * Eight character Threema ID.
 */
public final class ThreemaId {

	private final String value;

	private ThreemaId(String value) throws IllegalArgumentException {
		if (value.length() != 8) {
			throw new IllegalArgumentException("Illegal Threema ID length: " + value);
		}
		this.value = value;
	}

	public static ThreemaId of(String value) {
		return new ThreemaId(value);
	}

	/**
	 * @return String value of the ThreemaID
	 */
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !this.getClass().equals(other.getClass())) {
			return false;
		}
		return value.equals(((ThreemaId) other).value);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + value + "]";
	}

}
