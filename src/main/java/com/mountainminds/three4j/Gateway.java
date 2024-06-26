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

import static com.mountainminds.three4j.GatewayException.STATUS_BADREQUEST;
import static com.mountainminds.three4j.GatewayException.STATUS_FORBIDDEN;
import static com.mountainminds.three4j.GatewayException.STATUS_INTERNALSERVERERROR;
import static com.mountainminds.three4j.GatewayException.STATUS_NOTFOUND;
import static com.mountainminds.three4j.GatewayException.STATUS_PAYLOADTOOLARGE;
import static com.mountainminds.three4j.GatewayException.STATUS_PAYMENTREQUIRED;
import static com.mountainminds.three4j.GatewayException.STATUS_UNAUTHORIZED;
import static com.mountainminds.three4j.HttpSupport.UNKNOWN_RESPONSE;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.mountainminds.three4j.HttpSupport.MultipartEncoder;
import com.mountainminds.three4j.HttpSupport.StatusHandler;

/**
 * Client for the Threema gateway.
 */
public final class Gateway {

	private static final String THREEMA_GATEWAY_URL = "https://msgapi.threema.ch/";

	private static final StatusHandler DEFAULT_STATUS = UNKNOWN_RESPONSE //
			.ok() //
			.error(STATUS_UNAUTHORIZED, "API identity or secret incorrect") //
			.error(STATUS_PAYMENTREQUIRED, "No credits remaining") //
			.error(STATUS_FORBIDDEN, "Forbidden") //
			.error(STATUS_INTERNALSERVERERROR, "Temporary internal server error");

	private final ThreemaId from;
	private final String secret;
	private final HttpClient httpclient;

	/**
	 * Creates a new gateway client for the given sender using a default HTTP
	 * client.
	 * 
	 * @param from   Threema ID of the sender (*XXXXXXX)
	 * @param secret API secret as obtained from the management console
	 */
	public Gateway(ThreemaId from, String secret) {
		this(from, secret, HttpClient.newHttpClient());
	}

	/**
	 * Creates a new gateway client for the given sender with the given HTTP client.
	 * 
	 * @param from       Threema ID of the sender (*XXXXXXX)
	 * @param secret     API secret as obtained from the management console
	 * @param httpclient preconfigured HTTP client
	 */
	public Gateway(ThreemaId from, String secret, HttpClient httpclient) {
		this.from = from;
		this.secret = secret;
		this.httpclient = httpclient;
	}

	/**
	 * Find Threema ID by phone number. Prefer using the hash based lookups to not
	 * disclose telephone numbers.
	 * 
	 * @param number phone number in E.164 format, without the leading +
	 * @return Threema ID
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 * @see #from {@link #getIdByPhoneNumber(Hash)}
	 */
	public ThreemaId getIdByPhoneNumber(String number) throws GatewayException, IOException {
		var request = gwRequest(auth(), "lookup", "phone", number).build();
		return ThreemaId.of(send(request, BodyHandlers.ofString(), DEFAULT_STATUS //
				.error(STATUS_NOTFOUND, () -> "No matching ID for " + number)));
	}

	/**
	 * Find Threema ID by phone number hash.
	 * 
	 * @param number phone number hashed with {@link Hash#ofPhone(String)}
	 * @return Threema ID
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 * @see Hash#ofPhone(String)
	 */
	public ThreemaId getIdByPhoneNumber(Hash number) throws GatewayException, IOException {
		var request = gwRequest(auth(), "lookup", "phone_hash", number.getHexValue()).build();
		return ThreemaId.of(send(request, BodyHandlers.ofString(), DEFAULT_STATUS //
				.error(STATUS_NOTFOUND, () -> "No matching ID for " + number)));
	}

	/**
	 * Find Threema ID by email address. Prefer using the hash based lookups to not
	 * disclose email addresses.
	 * 
	 * @param address email address in lower case without white spaces
	 * @return Threema ID
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 * @see #from {@link #getIdByEmailAddress(Hash)}
	 */
	public ThreemaId getIdByEmailAddress(String address) throws GatewayException, IOException {
		var request = gwRequest(auth(), "lookup", "email", address).build();
		return ThreemaId.of(send(request, BodyHandlers.ofString(), DEFAULT_STATUS //
				.error(STATUS_NOTFOUND, () -> "No matching ID for " + address)));
	}

	/**
	 * Find Threema ID by email address hash.
	 * 
	 * @param address address hashed as returned by {@link Hash#ofEmail(String)}
	 * @return Threema ID
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 * @see Hash#ofEmail(String)
	 */
	public ThreemaId getIdByEmailAddress(Hash address) throws GatewayException, IOException {
		var request = gwRequest(auth(), "lookup", "email_hash", address.getHexValue()).build();
		return ThreemaId.of(send(request, BodyHandlers.ofString(), DEFAULT_STATUS //
				.error(STATUS_NOTFOUND, () -> "No matching ID for " + address)));
	}

	/**
	 * Return value for {@link Gateway#bulkLookup(Set, Set)} wrapping a Threema ID
	 * with its corresponding public key.
	 */
	public static class IDKey {

		private final ThreemaId id;
		private final PublicKey key;

		IDKey(String id, String key) {
			this.id = ThreemaId.of(id);
			this.key = KeyEncoder.decodePublicKey(key);
		}

		/**
		 * @return Threema ID
		 */
		public ThreemaId getId() {
			return id;
		}

		/**
		 * @return corresponding public key
		 */
		public PublicKey getKey() {
			return key;
		}

		@Override
		public String toString() {
			return "IDKey[" + id.getValue() + ", " + KeyEncoder.encode(key) + "]";
		}

	}

	/**
	 * Lookup multiple Threema IDs by email or phone hashes. The response contains
	 * the Threema ID along with the corresponding public key ({@link IDKey}).
	 * 
	 * @param phoneHashes set of hashes created with {@link Hash#ofPhone(String)}
	 * @param emailHashes set of hashes created with {@link Hash#ofEmail(String)}
	 * @return map with entries for every hash where a Threema ID was found for
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 */
	public Map<Hash, IDKey> bulkLookup(Set<Hash> phoneHashes, Set<Hash> emailHashes)
			throws GatewayException, IOException {
		var requestbody = BulkLookup.writeRequest(phoneHashes, emailHashes);
		var request = gwRequest(auth(), "lookup", "bulk") //
				.POST(HttpRequest.BodyPublishers.ofString(requestbody)) //
				.build();
		return BulkLookup.readResponse(send(request, BodyHandlers.ofString(), DEFAULT_STATUS //
				.error(STATUS_BADREQUEST, "invalid JSON or hash length") //
				.error(STATUS_PAYLOADTOOLARGE, "too many hashes")));
	}

	/**
	 * Returns the number of available credits for this account.
	 * 
	 * @return number of credits
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 */
	public int getRemainingCredits() throws GatewayException, IOException {
		var request = gwRequest(auth(), "credits").build();
		var result = send(request, BodyHandlers.ofString(), DEFAULT_STATUS);
		return Integer.parseInt(result);
	}

	/**
	 * Returns the public key for the given Threema ID. Please consider obtaining
	 * the public key from the receiver directly to establish a trust relationship.
	 * 
	 * @param threemaid Threema ID
	 * @return corresponding public key
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 */
	public PublicKey getPublicKey(ThreemaId threemaid) throws GatewayException, IOException {
		var request = gwRequest(auth(), "pubkeys", threemaid.getValue()).build();
		return KeyEncoder.decodePublicKey(send(request, BodyHandlers.ofString(), DEFAULT_STATUS //
				.error(STATUS_NOTFOUND, () -> "No matching ID for " + threemaid)));
	}

	/**
	 * Currently known capabilities.
	 */
	public static enum Capability {

		text, image, video, audio, group, ballot, file, call, videocall, pfs, groupcall,

		/** Capability not known by this Threema client implementation */
		unknown;

		private static final Map<String, Capability> BY_NAME = //
				Arrays.stream(values()).collect(toMap(Enum::name, identity()));

		static Capability of(String name) {
			return BY_NAME.getOrDefault(name, unknown);
		}
	}

	/**
	 * Query the messaging capabilities of the given Threema ID.
	 * 
	 * @param threemaid Threema ID
	 * @return set of {@link Capability} strings
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 */
	public Set<Capability> getCapabilities(ThreemaId threemaid) throws GatewayException, IOException {
		var request = gwRequest(auth(), "capabilities", threemaid.getValue()).build();
		var result = send(request, BodyHandlers.ofString(), DEFAULT_STATUS //
				.error(STATUS_NOTFOUND, () -> "No matching ID for " + threemaid));
		return stream(result.split(",")).map(Capability::of).collect(toSet());
	}

	/**
	 * Sends a message using in simple mode. Note that the content is transmitted
	 * unencrypted to the gateway, which is not recommended. The sending gateway
	 * Threema ID must have been created in simple mode.
	 * 
	 * @param toThreemid the recipient's Threema ID
	 * @param text       text to transmit
	 * @return message id
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 * @see #sendMessage(ThreemaId, EncryptedMessage)
	 */
	public MessageId sendSimpleMessage(ThreemaId toThreemid, String text) throws GatewayException, IOException {
		return sendSimpleMessage("to", toThreemid.getValue(), text);
	}

	/**
	 * Sends a message using in simple mode. Note that the content is transmitted
	 * unencrypted to the gateway, which is not recommended. The sending gateway
	 * Threema ID must have been created in simple mode.
	 * 
	 * @param phone phone number of the recipient for ID lookup
	 * @param text  text to transmit
	 * @return message id
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 * @see #sendMessage(ThreemaId, EncryptedMessage)
	 */
	public MessageId sendSimpleMessageToPhoneNumber(String phone, String text) throws GatewayException, IOException {
		return sendSimpleMessage("phone", phone, text);
	}

	/**
	 * Sends a message using in simple mode. Note that the content is transmitted
	 * unencrypted to the gateway, which is not recommended. The sending gateway
	 * Threema ID must have been created in simple mode.
	 * 
	 * @param email email of the recipient for ID lookup
	 * @param text  text to transmit
	 * @return message id
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 * @see #sendMessage(ThreemaId, EncryptedMessage)
	 */
	public MessageId sendSimpleMessageToEmailAddress(String email, String text) throws GatewayException, IOException {
		return sendSimpleMessage("email", email, text);
	}

	private MessageId sendSimpleMessage(String receipientKey, String receipientValue, String text)
			throws GatewayException, IOException {
		var body = auth() //
				.add(receipientKey, receipientValue) //
				.add("text", text).toBody();
		var request = gwRequest("send_simple") //
				.header("Content-Type", "application/x-www-form-urlencoded") //
				.POST(body).build();
		return MessageId.of(send(request, BodyHandlers.ofString(), DEFAULT_STATUS //
				.error(STATUS_BADREQUEST,
						"the recipient identity is invalid or the account is not set up for basic mode") //
				.error(STATUS_NOTFOUND, () -> "no matching id for " + receipientValue) //
				.error(STATUS_PAYLOADTOOLARGE, "message is too long")));
	}

	/**
	 * Sends a end-to-end encrypted message. The sending gateway Threema ID must
	 * have been created for end-to-end mode. The content must be encrypted with the
	 * private key of the sender and the public key of the receiver.
	 * 
	 * @param toThreemid the recipient's Threema ID
	 * @param msg        encrypted message
	 * @return message id
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 */
	public MessageId sendMessage(ThreemaId toThreemid, EncryptedMessage msg) throws GatewayException, IOException {
		var body = auth() //
				.add("to", toThreemid.getValue()) //
				.add("box", msg.getHexValue()) //
				.add("nonce", msg.getNonce().getHexValue()).toBody();
		var request = gwRequest("send_e2e") //
				.header("Content-Type", "application/x-www-form-urlencoded") //
				.POST(body).build();
		return MessageId.of(send(request, BodyHandlers.ofString(), DEFAULT_STATUS //
				.error(STATUS_BADREQUEST,
						"recipient identity is invalid or the account is not set up for end-to-end mode") //
				.error(STATUS_PAYLOADTOOLARGE, "message is too long")));
	}

	/**
	 * Uploads the given encrypted content.
	 * 
	 * @param encryptedcontent encrypted binary content
	 * @return blob id
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 */
	public BlobId uploadBlob(byte[] encryptedcontent) throws GatewayException, IOException {
		var encoder = new MultipartEncoder(encryptedcontent);
		var request = gwRequest(auth(), "upload_blob") //
				.header("Content-Type", encoder.getContentType()) //
				.POST(BodyPublishers.ofByteArray(encoder.getBody())).build();
		return BlobId.of(send(request, BodyHandlers.ofString(), DEFAULT_STATUS //
				.error(STATUS_BADREQUEST, "required parameters missing or blob empty") //
				.error(STATUS_PAYLOADTOOLARGE, "blob is too big")));
	}

	/**
	 * Encrypts and uploads the given content.
	 * 
	 * @param blob         parameters used to encrypt the content
	 * @param plaincontent unencrypted content
	 * @return description of the uploaded content
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 */
	public UploadedBlob enrcryptAndUploadBlob(Blob blob, byte[] plaincontent) throws GatewayException, IOException {
		var encryptedcontent = blob.encrypt(plaincontent);
		var blobId = uploadBlob(encryptedcontent);
		return blob.uploaded(blobId, encryptedcontent.length);
	}

	/**
	 * Downloads a encrypted blob with the given id.
	 * 
	 * @param blobid id of the blob
	 * @return encrypted blob content
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 */
	public byte[] downloadBlob(BlobId blobid) throws GatewayException, IOException {
		var request = gwRequest(auth(), "blobs", blobid.getHexValue()).build();
		return send(request, BodyHandlers.ofByteArray(), DEFAULT_STATUS//
				.error(STATUS_NOTFOUND, () -> "no blob with " + blobid));
	}

	/**
	 * Downloads and decrypts the given blob.
	 * 
	 * @param blob information how to download and decrypt the blob
	 * @return decrypted content
	 * @throws GatewayException when the Gateway reports an error status
	 * @throws IOException      when a technical communication problem occurs
	 */
	public byte[] downloadAndDecryptBlob(UploadedBlob blob) throws GatewayException, IOException {
		return blob.decrypt(downloadBlob(blob.getId()));
	}

	private Builder gwRequest(String... path) {
		return HttpRequest.newBuilder(URI.create(gwRequestUrl(path)));
	}

	private Builder gwRequest(HttpSupport.UrlParams urlparams, String... path) {
		return HttpRequest.newBuilder(URI.create(gwRequestUrl(path) + "?" + urlparams));
	}

	private String gwRequestUrl(String... path) {
		return THREEMA_GATEWAY_URL + String.join("/", path);
	}

	private HttpSupport.UrlParams auth() {
		return new HttpSupport.UrlParams().add("from", from.getValue()).add("secret", secret);
	}

	private <T> T send(HttpRequest request, BodyHandler<T> handler, StatusHandler statusHandler)
			throws GatewayException, IOException {
		try {
			var response = httpclient.send(request, handler);
			statusHandler.handle(response.statusCode());
			return response.body();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

}
