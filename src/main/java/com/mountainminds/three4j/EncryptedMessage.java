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

import software.pando.crypto.nacl.CryptoBox;

/**
 * An encrypted Threema message with its encrypted content and the corresponding
 * nonce.
 */
public final class EncryptedMessage {

	public static final int MAX_CONTENT_LENGTH = 4000;

	private byte[] content;
	private Nonce nonce;

	/**
	 * @param content encrypted binary content up to 4000 bytes
	 * @param nonce   24 byte nonce used for encryption
	 * @throws IllegalArgumentException if the required byte length do not match the
	 *                                  specifications
	 */
	public EncryptedMessage(byte[] content, Nonce nonce) throws IllegalArgumentException {
		if (content.length > MAX_CONTENT_LENGTH) {
			throw new IllegalArgumentException("Content too large: " + content.length);
		}
		this.content = content;
		this.nonce = nonce;
	}

	/**
	 * @param hexContent encrypted hex encoded content up to 4000 bytes
	 * @param nonce      24 byte nonce used for encryption
	 * @throws IllegalArgumentException if the required byte length do not match the
	 *                                  specifications
	 */
	public EncryptedMessage(String hexContent, Nonce nonce) throws IllegalArgumentException {
		this(KeyEncoder.fromHex(hexContent), nonce);
	}

	/**
	 * @return encrypted content
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * @return hex encoded content
	 */
	public String getHexContent() {
		return KeyEncoder.toHex(content);
	}

	/**
	 * @return nonce used for encryption
	 */
	public Nonce getNonce() {
		return nonce;
	}

	/**
	 * Decrypts this message into a plain message of the respective type.
	 * 
	 * @param sender   sender public key
	 * @param receiver private key
	 * @return subclass of {@link PlainMessage} depending on the type
	 * @see PlainMessage#getType()
	 */
	public PlainMessage decrypt(PublicKey sender, PrivateKey receiver) {
		var box = CryptoBox.fromCombined(nonce.getValue(), content);
		return PlainMessage.decode(box.decrypt(receiver, sender));
	}

}
