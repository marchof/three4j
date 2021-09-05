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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class KeyEncoderTest {

	@Test
	public void getPublicKey_should_return_corresponding_public_key() {
		var pair = KeyGenerator.generate();
		var publicKey = KeyEncoder.getPublicKey(pair.getPrivate());
		assertEquals(pair.getPublic(), publicKey);
	}

	@Test
	public void qrcode_should_create_threema_qr_code() {
		var threemaid = ThreemaId.of("*GWYTEST");
		var publicKey = KeyEncoder.decodePublicKey("1234567812345678123456781234567812345678123456781234567812345678");
		var qr = KeyEncoder.qrcode(threemaid, publicKey);
		assertEquals("3mid:*GWYTEST,1234567812345678123456781234567812345678123456781234567812345678", qr);
	}

}
