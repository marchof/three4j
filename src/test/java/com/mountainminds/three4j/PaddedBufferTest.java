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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class PaddedBufferTest {

	private final Random minRandom = new Random() {
		private static final long serialVersionUID = 1L;

		@Override
		public int nextInt(int bound) {
			return 0;
		}
	};

	private final Random maxRandom = new Random() {
		private static final long serialVersionUID = 1L;

		@Override
		public int nextInt(int bound) {
			return bound - 1;
		}
	};

	@Test
	void finish_should_expand_message_to_at_least_32_bytes() throws IOException {
		try (var buffer = new PaddedBuffer(minRandom)) {
			buffer.write(0x12);
			buffer.write(0x15);
			var msg = buffer.withPadding();
			var expected = bytes(0x12, 0x15).nbytes(30, 30).toByteArray();
			assertArrayEquals(expected, msg);
		}
	}

	@Test
	void finish_should_add_at_least_1_byte() throws IOException {
		try (var buffer = new PaddedBuffer(minRandom)) {
			buffer.write(bytes().nbytes(64, 42).toByteArray());
			var msg = buffer.withPadding();
			var expected = bytes().nbytes(64, 42).nbytes(1, 1).toByteArray();
			assertArrayEquals(expected, msg);
		}
	}

	@Test
	void finish_should_add_at_most_255_bytes() throws IOException {
		try (var buffer = new PaddedBuffer(maxRandom)) {
			buffer.write(0x42);
			buffer.write(0x43);
			var msg = buffer.withPadding();
			var expected = bytes(0x42, 0x43).nbytes(255, 255).toByteArray();
			assertArrayEquals(expected, msg);
		}
	}

	@Test
	void removePadding_should_remove_padding_of_length_1() throws IOException {
		var data = PaddedBuffer.removePadding(bytes(1, 2, 3).nbytes(1, 1).toByteArray());
		assertArrayEquals(bytes(1, 2, 3).toByteArray(), data.readAllBytes());
	}

	@Test
	void removePadding_should_remove_padding_of_length_255() throws IOException {
		var data = PaddedBuffer.removePadding(bytes(1, 2, 3).nbytes(255, 255).toByteArray());
		assertArrayEquals(bytes(1, 2, 3).toByteArray(), data.readAllBytes());
	}

	private static Bytes bytes(int... bytes) {
		var out = new Bytes();
		for (int b : bytes) {
			out.write(0xff & b);
		}
		return out;
	}

	private static class Bytes extends ByteArrayOutputStream {
		Bytes nbytes(int count, int value) {
			for (int i = 0; i < count; i++) {
				write(0xff & value);
			}
			return this;
		}
	}

}
