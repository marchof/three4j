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

import static com.mountainminds.three4j.KeyEncoder.fromHex;
import static com.mountainminds.three4j.KeyEncoder.toHex;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class KeyEncoderTest {

	@Test
	public void toHex_should_encode_byte_array() {
		var content = new byte[] { (byte) 0x00, (byte) 0xff, (byte) 0x83 };
		assertEquals("00ff83", toHex(content));
	}

	@Test
	public void fromHex_should_decode_hex_string() {
		var expected = new byte[] { (byte) 0x00, (byte) 0xb5, (byte) 0xff };
		assertArrayEquals(expected, fromHex("00b5ff"));
	}

}
