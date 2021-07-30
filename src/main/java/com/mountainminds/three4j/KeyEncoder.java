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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.XECPrivateKey;
import java.security.interfaces.XECPublicKey;

import javax.crypto.SecretKey;

import software.pando.crypto.nacl.CryptoBox;
import software.pando.crypto.nacl.SecretBox;

/**
 * Utilities methods to encode and decode keys.
 */
public final class KeyEncoder {

	// === key encoding/decoding

	/**
	 * Encodes a public Threema key.
	 * 
	 * @param key public key
	 * @return 64 digits hex string
	 */
	public static String encode(PublicKey key) {
		return toHex(reverse(((XECPublicKey) key).getU().toByteArray()));
	}

	/**
	 * Encodes a private Threema key.
	 * 
	 * @param key private key
	 * @return 64 digits hex string
	 */
	public static String encode(PrivateKey key) {
		return toHex(((XECPrivateKey) key).getScalar().get());
	}

	/**
	 * Encodes a secret Threema key (the combination of public key and a private
	 * key).
	 * 
	 * @param key secret key
	 * @return 64 digits hex string
	 */
	public static String encode(SecretKey key) {
		return toHex(key.getEncoded());
	}

	/**
	 * Decodes a public Threema key.
	 * 
	 * @param hex 64 digits hex string
	 * @return public key
	 */
	public static PublicKey decodePublicKey(String hex) {
		return CryptoBox.publicKey(fromHex(hex));
	}

	/**
	 * Decodes a private Threema key.
	 * 
	 * @param hex 64 digits hex string
	 * @return private key
	 */
	public static PrivateKey decodePrivateKey(String hex) {
		return CryptoBox.privateKey(fromHex(hex));
	}

	/**
	 * Decodes a secret Threema key.
	 * 
	 * @param hex 64 digits hex string
	 * @return secret key
	 */
	public static SecretKey decodeSecretKey(String hex) {
		return SecretBox.key(fromHex(hex));
	}

	/**
	 * Creates the QR code text to exchange Threema ids. This can be used for
	 * example for gateway ids to establish a direct trust relationship with users.
	 * 
	 * @param threemaid 8 character long Threema id
	 * @param publicKey public key
	 * @return QR code text
	 */
	public String qrcode(ThreemaId threemaid, PublicKey publicKey) {
		return String.format("3mid:%s,%s", threemaid.getValue(), encode(publicKey));
	}

	/**
	 * Converts a byte array into its hex representation
	 * 
	 * @param bytes byte array of arbitrary size
	 * @return hex string (twice as many digits as bytes)
	 */
	static String toHex(byte[] bytes) {
		var sb = new StringBuilder();
		for (var b : bytes) {
			sb.append(Character.forDigit((b & 0xff) >> 4, 16));
			sb.append(Character.forDigit(b & 0xf, 16));
		}
		return sb.toString();
	}

	/**
	 * Converts a hex string into a byte array.
	 * 
	 * @param hex hex string
	 * @return byte array (half the size than hex digits)
	 */
	static byte[] fromHex(String hex) {
		var bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			var d1 = Character.digit(hex.charAt(2 * i), 16);
			var d2 = Character.digit(hex.charAt(2 * i + 1), 16);
			bytes[i] = (byte) ((d1 << 4) | d2);
		}
		return bytes;
	}

	private static byte[] reverse(byte[] array) {
		var reversed = new byte[array.length];
		for (int i = array.length, j = 0; --i >= 0; j++) {
			reversed[i] = array[j];
		}
		return reversed;
	}

	private KeyEncoder() {
		// no instances
	}

}
