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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.XECPrivateKey;
import java.security.interfaces.XECPublicKey;
import java.security.spec.NamedParameterSpec;

import javax.crypto.SecretKey;

import software.pando.crypto.nacl.CryptoBox;
import software.pando.crypto.nacl.SecretBox;

/**
 * Utilities methods to encode and decode keys.
 */
public final class KeyEncoder {

	private static final String KEY_ALGORITHM = "X25519";

	/**
	 * Encodes a public Threema key.
	 * 
	 * @param key public key
	 * @return 64 digits hex string
	 */
	public static String encode(PublicKey key) {
		return ByteArrayValue.toHex(reverse(((XECPublicKey) key).getU().toByteArray()));
	}

	/**
	 * Encodes a private Threema key.
	 * 
	 * @param key private key
	 * @return 64 digits hex string
	 */
	public static String encode(PrivateKey key) {
		return ByteArrayValue.toHex(getBytes(key));
	}

	/**
	 * Encodes a secret Threema key (the combination of public key and a private
	 * key).
	 * 
	 * @param key secret key
	 * @return 64 digits hex string
	 */
	public static String encode(SecretKey key) {
		return ByteArrayValue.toHex(key.getEncoded());
	}

	/**
	 * Decodes a public Threema key.
	 * 
	 * @param hex 64 digits hex string
	 * @return public key
	 */
	public static PublicKey decodePublicKey(String hex) {
		return CryptoBox.publicKey(ByteArrayValue.fromHex(hex));
	}

	/**
	 * Decodes a private Threema key.
	 * 
	 * @param hex 64 digits hex string
	 * @return private key
	 */
	public static PrivateKey decodePrivateKey(String hex) {
		return CryptoBox.privateKey(ByteArrayValue.fromHex(hex));
	}

	/**
	 * Decodes a secret Threema key.
	 * 
	 * @param hex 64 digits hex string
	 * @return secret key
	 */
	public static SecretKey decodeSecretKey(String hex) {
		return SecretBox.key(ByteArrayValue.fromHex(hex));
	}

	/**
	 * Derives the corresponding public key from the given private key.
	 * 
	 * @param privateKey private key
	 * @return corresponding public key
	 */
	public static PublicKey getPublicKey(PrivateKey privateKey) {
		var keyBytes = getBytes(privateKey);
		@SuppressWarnings("serial")
		var notrandom = new SecureRandom() {
			@Override
			public void nextBytes(byte[] bytes) {
				System.arraycopy(keyBytes, 0, bytes, 0, keyBytes.length);
			}
		};
		try {
			var generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
			generator.initialize(new NamedParameterSpec(KEY_ALGORITHM), notrandom);
			return generator.generateKeyPair().getPublic();
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates the QR code text to exchange Threema ids. This can be used for
	 * example for gateway ids to establish a direct trust relationship with users.
	 * 
	 * @param threemaid 8 character long Threema id
	 * @param publicKey public key
	 * @return QR code text
	 */
	public static String qrcode(ThreemaId threemaid, PublicKey publicKey) {
		return String.format("3mid:%s,%s", threemaid.getValue(), encode(publicKey));
	}

	private static byte[] reverse(byte[] array) {
		var reversed = new byte[array.length];
		for (int i = array.length, j = 0; --i >= 0; j++) {
			reversed[i] = array[j];
		}
		return reversed;
	}

	private static byte[] getBytes(PrivateKey privateKey) {
		return ((XECPrivateKey) privateKey).getScalar().get();
	}

	private KeyEncoder() {
		// no instances
	}

}
