/*******************************************************************************
 * Copyright (c) 2023 Mountainminds GmbH & Co. KG
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Internal utility for PKCS#7 padding.
 */
class PaddedBuffer extends DataOutputStream {

	private static ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(SecureRandom::new);

	private static final int MSG_LEN_MIN = 32;
	private static final int PAD_LEN_MIN = 1;
	private static final int PAD_LEN_MAX = 255;

	private final Random random;

	PaddedBuffer(Random random) {
		super(new ByteArrayOutputStream());
		this.random = random;
	}

	PaddedBuffer() {
		this(RANDOM.get());
	}

	byte[] withPadding() throws IOException {
		var buffer = (ByteArrayOutputStream) out;
		int panLenMin = Math.max(PAD_LEN_MIN, MSG_LEN_MIN - buffer.size());
		int padding = random.nextInt(PAD_LEN_MAX - panLenMin + 1) + panLenMin;
		for (int i = 0; i < padding; i++) {
			write(padding);
		}
		return buffer.toByteArray();
	}

	static DataInputStream removePadding(byte[] buffer) {
		int len = 0xff & buffer[buffer.length - 1];
		return new DataInputStream(new ByteArrayInputStream(buffer, 0, buffer.length - len));
	}

}
