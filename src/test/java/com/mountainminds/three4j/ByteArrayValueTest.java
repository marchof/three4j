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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ByteArrayValueTest {

	static class Subject extends ByteArrayValue {
		Subject(byte[] value) {
			super(value, 4);
		}
	}

	@Test
	void should_throw_IllegalArgumentException_for_wrong_array_length() {
		var e = assertThrows(IllegalArgumentException.class, () -> new Subject(new byte[5]));
		assertEquals("Illegal value size: 5 bytes", e.getMessage());
	}

	@Test
	void getValue_should_return_value() {
		var s = new Subject(new byte[] { 1, 2, 3, 4 });
		assertArrayEquals(new byte[] { 1, 2, 3, 4 }, s.getValue());
	}

	@Test
	void hashCode_should_be_different_for_different_values() {
		var s1 = new Subject(new byte[] { 1, 2, 3, 4 });
		var s2 = new Subject(new byte[] { 1, 4, 3, 4 });
		assertNotEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	void hashCode_should_be_same_for_same_values() {
		var s1 = new Subject(new byte[] { 1, 2, 3, 4 });
		var s2 = new Subject(new byte[] { 1, 2, 3, 4 });
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	void equals_should_be_true_for_equal_values_and_types() {
		var s1 = new Subject(new byte[] { 1, 2, 3, 4 });
		var s2 = new Subject(new byte[] { 1, 2, 3, 4 });
		assertTrue(s1.equals(s2));
	}

	@Test
	void equals_should_be_false_for_null() {
		var s1 = new Subject(new byte[] { 1, 2, 3, 4 });
		assertFalse(s1.equals(null));
	}

	@Test
	void equals_should_be_false_for_different_values() {
		var s1 = new Subject(new byte[] { 1, 2, 3, 4 });
		var s2 = new Subject(new byte[] { 1, 2, 5, 4 });
		assertFalse(s1.equals(s2));
	}

	@Test
	void equals_should_be_false_for_different_types() {
		var s1 = new Subject(new byte[] { 1, 2, 3, 4 });
		var s2 = new Subject(new byte[] { 1, 2, 3, 4 }) {
		};
		assertFalse(s1.equals(s2));
	}

	@Test
	void toString_should_print_hex_value() {
		var s = new Subject(new byte[] { 0x0a, 0x12, 0x34, 0x56 });
		assertEquals("Subject[0a123456]", s.toString());
	}

}
