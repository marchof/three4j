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
 * Gateway</a>. The class {@link Gateway} represents the client to the API,
 * while {@link GatewayCallback} is used to decode incoming requests.
 * </p>
 * 
 * <p>
 * While all messages are transfered as a {@link EncryptedMessage} the
 * unencrypted {@link PlainMessage} exists in the following flavours:
 * </p>
 * 
 * <ul>
 * <li>{@link PlainMessage.Text}
 * <li>{@link PlainMessage.Image}
 * <li>{@link PlainMessage.File}
 * <li>{@link PlainMessage.DeliveryReceipt}
 * </ul>
 * 
 * <p>
 * For type safety the API uses specific wrapper types instead of
 * <code>byte[]</code> of <code>String</code> values:
 * </p>
 * 
 * <ul>
 * <li>{@link MessageId}
 * <li>{@link Nonce}
 * <li>{@link BlobId}
 * <li>{@link Hash}
 * <li>{@link java.security.PublicKey}
 * <li>{@link java.security.PrivateKey}
 * </ul>
 */
package com.mountainminds.three4j;
