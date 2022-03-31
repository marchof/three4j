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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

public class GatewayCallbackTest {

	@Test
	public void init_should_decode_body() {
		var body = "from=SENDERXY" //
				+ "&to=RECEIVER" //
				+ "&messageId=0011223344556677" //
				+ "&date=1650000000" //
				+ "&nonce=001122334455667700112233445566770011223344556677" //
				+ "&box=0123456789abcdef" //
				+ "&nickname=three4j" //
				+ "&mac=c1d77e5a605511635c9150fb7c1f6ad9eaedf02352e981b6a1d687c742e84c15";
		var callback = new GatewayCallback(body, "secret");

		assertEquals(ThreemaId.of("SENDERXY"), callback.getFrom());
		assertEquals(ThreemaId.of("RECEIVER"), callback.getTo());
		assertEquals(MessageId.of("0011223344556677"), callback.getMessageId());
		assertEquals(Instant.ofEpochSecond(1650000000), callback.getDate());
		assertEquals("three4j", callback.getNickname());
		assertEquals("0123456789abcdef", callback.getMessage().getHexValue());
		assertEquals(Nonce.of("001122334455667700112233445566770011223344556677"), callback.getMessage().getNonce());
	}

	@Test
	public void init_should_throw_IllegalArgumentException_when_parameter_is_missing() {
		var ex = assertThrows(IllegalArgumentException.class,
				() -> new GatewayCallback("from=x&to=x&date=x&nonce=x&box=x&mac=x", "x"));
		assertEquals("Missing parameter messageId", ex.getMessage());
	}

	@Test
	public void init_should_throw_IllegalArgumentException_when_mac_is_missing() {
		var ex = assertThrows(IllegalArgumentException.class,
				() -> new GatewayCallback("from=x&to=x&messageId=x&date=x&nonce=x&box=x", "x"));
		assertEquals("Missing parameter mac", ex.getMessage());
	}

	@Test
	public void init_should_throw_IllegalArgumentException_when_signature_is_invalid() {
		var ex = assertThrows(IllegalArgumentException.class,
				() -> new GatewayCallback("from=x&to=x&messageId=x&date=x&nonce=x&box=x&mac=x", "x"));
		assertEquals("Invalid signature", ex.getMessage());
	}
}
