/*******************************************************************************
 * Copyright (c) 2022 Mountainminds GmbH & Co. KG
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.mountainminds.three4j.PlainMessage.Location;

public class PlainMessageLocationTest {

	@Test
	public void init_should_decode_location() throws IOException {
		var encoded = "46.94675,7.44423".getBytes(StandardCharsets.UTF_8);
		var l = new Location(new DataInputStream(new ByteArrayInputStream(encoded)));
		assertEquals(46.94675, l.getLatitude());
		assertEquals(7.44423, l.getLongitude());
		assertTrue(Double.isNaN(l.getAccuracy()));
		assertNull(l.getName());
		assertNull(l.getAddress());
	}

	@Test
	public void decode_should_decode_location_and_accuracy() throws IOException {
		var encoded = "46.947,7.444,40.000".getBytes(StandardCharsets.UTF_8);
		var l = new Location(new DataInputStream(new ByteArrayInputStream(encoded)));
		assertEquals(46.947, l.getLatitude());
		assertEquals(7.444, l.getLongitude());
		assertEquals(40.0, l.getAccuracy());
		assertNull(l.getName());
		assertNull(l.getAddress());
	}

	@Test
	public void decode_should_decode_location_and_address() throws IOException {
		var encoded = "46.947,7.444,40.000\nBundesplatz 3, 3003 Bern, Switzerland".getBytes(StandardCharsets.UTF_8);
		var l = new Location(new DataInputStream(new ByteArrayInputStream(encoded)));
		assertEquals(46.947, l.getLatitude());
		assertEquals(7.444, l.getLongitude());
		assertEquals(40.0, l.getAccuracy());
		assertNull(l.getName());
		assertEquals("Bundesplatz 3, 3003 Bern, Switzerland", l.getAddress());
	}

	@Test
	public void decode_should_decode_location_and_name_and_address() throws IOException {
		var encoded = "46.947,7.444,40.000\nBundeshaus\nBundesplatz 3, 3003 Bern, Switzerland"
				.getBytes(StandardCharsets.UTF_8);
		var l = new Location(new DataInputStream(new ByteArrayInputStream(encoded)));
		assertEquals(46.947, l.getLatitude());
		assertEquals(7.444, l.getLongitude());
		assertEquals(40.0, l.getAccuracy());
		assertEquals("Bundeshaus", l.getName());
		assertEquals("Bundesplatz 3, 3003 Bern, Switzerland", l.getAddress());
	}

	@Test
	public void toString_should_return_location() {
		var msg = new Location(46.947, 7.444, 40.0);
		msg.setNameAndAddress("Bundeshaus", "Bern");

		assertEquals("Location[46.947 7.444, Bundeshaus, Bern]", msg.toString());
	}

}
