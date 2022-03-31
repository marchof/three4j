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

import static com.mountainminds.three4j.Hash.newMAC;
import static com.mountainminds.three4j.HttpSupport.decodeUrlParams;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Immutable data structure containing the structured data of a gateway callback
 * to receive messages.
 */
public class GatewayCallback {

	private static final Charset ENCODING = US_ASCII;

	private final ThreemaId from;
	private final ThreemaId to;
	private final MessageId messageId;
	private final Instant date;
	private final EncryptedMessage message;
	private final String nickname;

	/**
	 * Decodes and verifies a callback HTTP body.
	 * 
	 * @param body   url encoded body of the callback request.
	 * @param secret gateway API secret
	 * @throws IllegalArgumentException if the data cannot be validated with the
	 *                                  signature
	 */
	public GatewayCallback(String body, String secret) throws IllegalArgumentException {
		var params = decodeUrlParams(body);

		var m = newMAC(secret.getBytes(ENCODING));
		Stream.of("from", "to", "messageId", "date", "nonce", "box") //
				.map(key -> getMandatoryValue(params, key)) //
				.map(s -> s.getBytes(ENCODING)) //
				.forEach(m::update);
		if (!ByteArrayValue.toHex(m.doFinal()).equals(getMandatoryValue(params, "mac"))) {
			throw new IllegalArgumentException("Invalid signature");
		}

		from = ThreemaId.of(params.get("from"));
		to = ThreemaId.of(params.get("to"));
		messageId = MessageId.of(params.get("messageId"));
		date = Instant.ofEpochSecond(Long.parseLong(params.get("date")));
		message = new EncryptedMessage(params.get("box"), Nonce.of(params.get("nonce")));
		nickname = params.get("nickname");
	}

	private static String getMandatoryValue(Map<String, String> params, String key) {
		var value = params.get(key);
		if (value == null) {
			throw new IllegalArgumentException("Missing parameter " + key);
		}
		return value;
	}

	/**
	 * Decodes and verifies a callback HTTP body.
	 * 
	 * @param body   url encoded body of the callback request.
	 * @param secret gateway API secret
	 * @throws IllegalArgumentException if the data cannot be validated with the
	 *                                  signature
	 */
	public GatewayCallback(byte[] body, String secret) throws IllegalArgumentException {
		this(new String(body, ENCODING), secret);
	}

	/**
	 * @return sender Threema ID
	 */
	public ThreemaId getFrom() {
		return from;
	}

	/**
	 * @return receiver API identity
	 */
	public ThreemaId getTo() {
		return to;
	}

	/**
	 * @return message ID assigned by the sender (8 bytes, hex encoded)
	 */
	public MessageId getMessageId() {
		return messageId;
	}

	/**
	 * @return timestamp provided by the sender
	 */
	public Instant getDate() {
		return date;
	}

	/**
	 * @return the encrypted message in this callback
	 */
	public EncryptedMessage getMessage() {
		return message;
	}

	/**
	 * @return the sender's public nickname or <code>null</code>
	 */
	public String getNickname() {
		return nickname;
	}

}
