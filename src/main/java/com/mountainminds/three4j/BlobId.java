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
 * A 16 byte identifier for uploaded blobs.
 */
public final class BlobId extends ByteArrayValue {

	/**
	 * Number of bytes of a blob id.
	 */
	public static final int SIZE = 16;

	private BlobId(byte[] value) {
		super(value, SIZE);
	}

	/**
	 * @param value 16 bytes
	 * @return new blob id
	 */
	public static BlobId of(byte[] value) {
		return new BlobId(value);
	}

	/**
	 * @param hexvalue 32 character hex string
	 * @return new blob id
	 */
	public static BlobId of(String hexvalue) {
		return of(fromHex(hexvalue));
	}

}
