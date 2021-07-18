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

import javax.crypto.SecretKey;

import software.pando.crypto.nacl.CryptoBox;
import software.pando.crypto.nacl.SecretBox;

/**
 * Representation of the encryption of binary blob data. This class contains the
 * key and nonce to encrypt or decrypt blobs. Depending to the blob type (image,
 * file or preview) different keys and nonces are used.
 */
public class Blob {

	private static final Nonce FILE_NONCE = Nonce.of("000000000000000000000000000000000000000000000001");
	private static final Nonce THUMBNAIL_NONCE = Nonce.of("000000000000000000000000000000000000000000000002");

	private SecretKey key;
	private Nonce nonce;

	Blob(SecretKey key, Nonce nonce) {
		this.key = key;
		this.nonce = nonce;
	}

	public SecretKey getKey() {
		return key;
	}

	public Nonce getNonce() {
		return nonce;
	}

	public byte[] encrypt(byte[] plaincontent) {
		return SecretBox.encrypt(key, nonce.getValue(), plaincontent).getCiphertextWithTag();
	}

	public byte[] decrypt(byte[] encryptedcontent) {
		return SecretBox.fromCombined(nonce.getValue(), encryptedcontent).decrypt(key);
	}

	/**
	 * Returns a thumbnail blob which has the same key as this blob.
	 * 
	 * @return new blob with same key
	 */
	public Blob thumbnail() {
		return new Blob(key, THUMBNAIL_NONCE);
	}

	/**
	 * Returns copy of this blob with upload information.
	 * 
	 * @param blobId blob ID
	 * @param size   size of the encrypted file in bytes
	 * @return new {@link UploadedBlob} instance
	 */
	public UploadedBlob uploaded(BlobId blobId, int size) {
		return new UploadedBlob(key, nonce, blobId, size);
	}

	// Factory methods

	public static Blob newImage(PrivateKey privatekey, PublicKey publicKey) {
		return new Blob((SecretKey) CryptoBox.agree(privatekey, publicKey), Nonce.random());
	}

	public static Blob newFile() {
		return ofFile(SecretBox.key());
	}

	static Blob ofFile(SecretKey key) {
		return new Blob(key, FILE_NONCE);
	}

}
