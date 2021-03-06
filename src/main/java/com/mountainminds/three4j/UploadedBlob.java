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

import javax.crypto.SecretKey;

/**
 * Subclass of {@link Blob} which adds the meta information for a specific
 * upload (blob id and encrypted file size).
 * 
 * @see Blob#uploaded(BlobId, int)
 */
public class UploadedBlob extends Blob {

	private final BlobId id;
	private final int size;

	UploadedBlob(SecretKey key, Nonce nonce, BlobId id, int size) {
		super(key, nonce);
		this.id = id;
		this.size = size;
	}

	/**
	 * Returns the ID of the uploaded blob
	 * 
	 * @return ID
	 */
	public BlobId getId() {
		return id;
	}

	/**
	 * Returns the size in bytes of the uploaded blob
	 * 
	 * @return size in bytes
	 */
	public int getSize() {
		return size;
	}

}
