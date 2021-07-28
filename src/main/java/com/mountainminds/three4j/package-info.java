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

/**
 * <p>
 * Java client for the <a href="https://gateway.threema.ch/">Threema
 * Gateway</a>. A {@link Gateway} instances can send requests to the Threema
 * gateway. A {@link GatewayCallback} is used to decode incoming requests.
 * </p>
 * 
 * <p>
 * A {@link PlainMessage} exists in the following flavours:
 * </p>
 * 
 * <ul>
 * <li>{@link PlainMessage.Text}</li>
 * <li>{@link PlainMessage.Image}</li>
 * <li>{@link PlainMessage.File}</li>
 * <li>{@link PlainMessage.DeliveryReceipt}</li>
 * </ul>
 * 
 * <p>
 * It can be encrypted with
 * {@link PlainMessage#encrypt(java.security.PrivateKey, java.security.PublicKey)}
 * to a {@link EncryptedMessage}, which can be decrypted with
 * {@link EncryptedMessage#decrypt(java.security.PublicKey, java.security.PrivateKey)}.
 * </p>
 *
 * <p>
 * Binary content like images od files are transmitted separately as a
 * {@link com.mountainminds.three4j.Blob}.
 * </p>
 * 
 * <p>
 * For type safety the API uses specific wrapper types instead of
 * <code>byte[]</code> of <code>String</code> values:
 * </p>
 * 
 * <ul>
 * <li>{@link ThreemaId}</li>
 * <li>{@link MessageId}</li>
 * <li>{@link Nonce}</li>
 * <li>{@link BlobId}</li>
 * <li>{@link Hash}</li>
 * <li>{@link java.security.PublicKey}</li>
 * <li>{@link java.security.PrivateKey}</li>
 * <li>{@link javax.crypto.SecretKey}</li>
 * </ul>
 */
package com.mountainminds.three4j;
