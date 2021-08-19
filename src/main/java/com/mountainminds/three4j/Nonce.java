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

import java.io.IOException;
import java.io.InputStream;

import software.pando.crypto.nacl.Bytes;

/**
 * Nonce (24 bytes) used for every encrypted content.
 */
public final class Nonce extends ByteArrayValue {

	/**
	 * Number of bytes of a nonce.
	 */
	public static final int SIZE = 24;

	private Nonce(byte[] value) {
		super(value, SIZE);
	}

	/**
	 * @param value 24 bytes
	 * @return new message id
	 */
	public static Nonce of(byte[] value) {
		return new Nonce(value);
	}

	/**
	 * @param hexvalue 48 character hex string
	 * @return new message id
	 */
	public static Nonce of(String hexvalue) {
		return of(fromHex(hexvalue));
	}

	/**
	 * @return new random nonce
	 */
	public static Nonce random() {
		return of(Bytes.secureRandom(SIZE));
	}

	static Nonce read(InputStream in) throws IOException {
		var id = in.readNBytes(SIZE);
		return id.length == SIZE ? of(id) : null;
	}

}
