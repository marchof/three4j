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

/**
 * Unique 8 byte ID for every message.
 */
public final class MessageId extends ByteArrayValue {

	/**
	 * Number of bytes of a message id.
	 */
	public static final int SIZE = 8;

	private MessageId(byte[] value) {
		super(value, SIZE);
	}

	/**
	 * @param value 8 bytes
	 * @return new message id
	 */
	public static MessageId of(byte[] value) {
		return new MessageId(value);
	}

	/**
	 * @param hexvalue 16 character hex string
	 * @return new message id
	 */
	public static MessageId of(String hexvalue) {
		return of(fromHex(hexvalue));
	}

	static MessageId read(InputStream in) throws IOException {
		var id = in.readNBytes(SIZE);
		return id.length == SIZE ? of(id) : null;
	}

}
