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
package com.mountainminds.three4j.guide;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.mountainminds.three4j.Blob;
import com.mountainminds.three4j.EncryptedMessage;
import com.mountainminds.three4j.Gateway;
import com.mountainminds.three4j.GatewayCallback;
import com.mountainminds.three4j.Hash;
import com.mountainminds.three4j.KeyEncoder;
import com.mountainminds.three4j.MessageId;
import com.mountainminds.three4j.PlainMessage;
import com.mountainminds.three4j.PlainMessage.File.RenderingType;
import com.mountainminds.three4j.ThreemaId;
import com.mountainminds.three4j.UploadedBlob;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Compilable and executable example code which is used to generate the
 * documentation in <code>README.md</code>.
 */
public class ApiGuide {

	public static void main(String[] args) throws Exception {

		// ### Setup
		//
		// To use the gateway API client you need a gateway Threema ID and the
		// corresponding secret which was issued by the gateway admin interface:

		// <CODE>
		ThreemaId from = ThreemaId.of("*YOURGWY"); // Insert your ID here
		from = ThreemaId.of(args[0]); // HIDE
		String secret = // e.g. "JSH5y9DfvOROm2Iw", retrieve this from a secure location
				args[1]; // HIDE
		// </CODE>

		// All requests are send through a `Gateway` instance:

		// <CODE>
		Gateway gw = new Gateway(from, secret);
		// </CODE>

		// If you want to send end-to-end encrypted messages (which is recommended)
		// you need your private 32 byte Threema key. The hexadecimal string
		// representation has 64 characters. Make sure you store this key securely
		// and do not make it available to others:

		// <CODE>
		String myPrivateKeyStr = // retrieve this from a secure location
				args[2]; // HIDE
		PrivateKey myPrivateKey = KeyEncoder.decodePrivateKey(myPrivateKeyStr);
		// </CODE>

		ThreemaId receiverId = lookups(gw);
		sendtextmessage(gw, myPrivateKey, receiverId);
		simplemessage(gw);
		threemaidqrcode(from, myPrivateKey);

		callbackserver(gw, secret, myPrivateKey);

	}

	static ThreemaId lookups(Gateway gw) throws IOException {

		// ### Lookups
		//
		// Sending a Threema message requires you to know the eight character
		// long Threema ID of the receiver. Users may choose to register their
		// telephone number or email address with their account which can then
		// be queried to lookup their Threema ID. Note that we do not disclose
		// the actual data but only send hash values:

		// <CODE>
		ThreemaId receiverId = gw.getIdByPhoneNumber(Hash.ofPhone("+41791234567"));
		System.out.println(receiverId);
		// </CODE>

		// Or by Email address:

		// <CODE>
		receiverId = gw.getIdByEmailAddress(Hash.ofEmail("test@example.com"));
		System.out.println(receiverId);
		// </CODE>

		// Depending on the client the receiver might be able to process certain
		// message types only. You can check the capabilities of a given Threema
		// ID:

		// <CODE>
		System.out.println(gw.getCapabilities(receiverId));
		// </CODE>

		return receiverId;
	}

	static void sendtextmessage(Gateway gw, PrivateKey myPrivateKey, ThreemaId receiverId) throws IOException {

		// ### Encrypted Text Messages
		//
		// To send a encrypted message we need the public key of the receiver
		// which can be obtained via the gateway API. For better information
		// security you should consider obtaining the public key physically
		// from the receivers device e.g. from the QR code.

		// <CODE>
		PublicKey receiverPublicKey = gw.getPublicKey(receiverId);
		// </CODE>

		// To ensure end-to-end encryption you create and encrypt the message
		// locally before you send it to the gateway:

		// <CODE>
		String text = String.format("Secret message at %s.", Instant.now());

		PlainMessage msg = new PlainMessage.Text(text);
		EncryptedMessage encrypted = msg.encrypt(myPrivateKey, receiverPublicKey);
		MessageId messageId = gw.sendMessage(receiverId, encrypted);

		System.out.println(messageId);
		// </CODE>
	}

	static void sendimagemessage(Gateway gw, PrivateKey myPrivateKey, ThreemaId receiverId) throws IOException {

		PublicKey receiverPublicKey = gw.getPublicKey(receiverId);

		// ### Encrypted Image Messages
		//
		// Sending images requires two steps. First we uploading the image as a
		// encrypted blob. Similarly as for the actual message the encryption
		// key for the blob is calculated from our private key and the receivers
		// public key.

		// <CODE>
		byte[] image = Files.readAllBytes(Path.of("src/test/resources/image.jpg"));

		Blob blob = Blob.newImage(myPrivateKey, receiverPublicKey);
		UploadedBlob uploadedBlob = gw.enrcryptAndUploadBlob(blob, image);
		// </CODE>

		// A reference to the uploaded blob needs then to be used in the image message:

		// <CODE>
		PlainMessage imgMsg = new PlainMessage.Image(uploadedBlob);
		EncryptedMessage encrypted = imgMsg.encrypt(myPrivateKey, receiverPublicKey);
		gw.sendMessage(receiverId, encrypted);
		// </CODE>

		// We can also download and decrypt our image blob again:

		// <CODE>
		byte[] downloadedImage = gw.downloadAndDecryptBlob(uploadedBlob);
		Files.write(Path.of("target/download.jpg"), downloadedImage);
		// </CODE>

	}

	static void sendfilemessage(Gateway gw, PrivateKey myPrivateKey, ThreemaId receiverId) throws IOException {

		PublicKey receiverPublicKey = gw.getPublicKey(receiverId);

		// ### Encrypted File Messages
		//
		// We can encrypt and send arbitrary files. Files are encrypted with a
		// random key which is then transmitted with the corresponding message.
		// An optional preview image can be added which must be encrypted with
		// the same key than the file:

		// <CODE>
		byte[] file = Files.readAllBytes(Path.of("src/test/resources/document.pdf"));
		UploadedBlob uploadedFileBlob = gw.enrcryptAndUploadBlob(Blob.newFile(), file);

		byte[] thumbnail = Files.readAllBytes(Path.of("src/test/resources/thumbnail.png"));
		Blob thumbnailBlob = uploadedFileBlob.thumbnail();
		UploadedBlob uploadedThumbnailBlob = gw.enrcryptAndUploadBlob(thumbnailBlob, thumbnail);
		// </CODE>

		// Construction a file message requires a bit of meta data like the MIME
		// type of the file.

		// <CODE>
		PlainMessage.File fileMsg = new PlainMessage.File(uploadedFileBlob, "application/pdf", RenderingType.DEFAULT);
		fileMsg.setThumbnail(uploadedThumbnailBlob);
		fileMsg.setFileName("document.pdf");

		EncryptedMessage encrypted = fileMsg.encrypt(myPrivateKey, receiverPublicKey);
		gw.sendMessage(receiverId, encrypted);
		// </CODE>

	}

	static void simplemessage(Gateway gw) throws IOException {

		// ### Simple, Unencrypted Messages
		//
		// With a *basic mode* gateway ID you can directly send a plain text
		// message to a given Threema ID without local encryption. The key pair
		// is managed for you on the gateway server. Please rather consider
		// using end-to-end encryption as described above.

		// <CODE>
		gw.sendSimpleMessage(ThreemaId.of("ABCDEFGH"), "Not so secret message.");
		// </CODE>

		// Alternatively you can also use a international telephone number or a
		// email address to send a message to if the users has registered them
		// with Threema. Note that the telephone number or the email address is
		// disclosed to the Threema gateway.

		// <CODE>
		gw.sendSimpleMessageToPhoneNumber("41791234567", "Not so secret message.");
		gw.sendSimpleMessageToEmailAddress("test@example.com", "Not so secret message.");
		// </CODE>

	}

	static void accountinfo(Gateway gw) throws IOException {

		// ### Account Information
		//
		// Threema charges you for messages and blob uploads via the gateway.
		// You can query the remaining credits via API:

		// <CODE>
		System.out.println("Remaining credits: " + gw.getRemainingCredits());
		// </CODE>

	}

	private static void threemaidqrcode(ThreemaId threemaid, PrivateKey privateKey) {
		// ### Threema QR Code
		//
		// To establich trust with your users you can create a QR code with your
		// Threema ID and your public key. When the user scans that QR code your
		// gateway ID will show "green" in their contact list. The `qrcode()`
		// method creates the text content which need to be converted to a QR
		// graphic with a library of your choice (e.g.
		// [zxing](https://github.com/zxing/zxing)).

		// <CODE>
		PublicKey publicKey = KeyEncoder.getPublicKey(privateKey);
		String qrtext = KeyEncoder.qrcode(threemaid, publicKey);
		System.out.println(qrtext);
		// </CODE>

	}

	static void callbackserver(Gateway gw, String secret, PrivateKey myPrivateKey) throws Exception {

		// ### Callback Handling
		//
		// You can configure your own HTTP server to receive messages from the
		// Threema gateway. The corresponding endpoint must be visible from the
		// public internet of course. The payload can be decoded with the
		// `GatewayCallback` class:

		Server server = new Server(8888);
		server.setHandler(new AbstractHandler() {

			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request,
					HttpServletResponse response) throws IOException, ServletException {
				// <CODE>
				byte[] body = // unprocessed body received from the HTTP server of your choice
						request.getInputStream().readAllBytes(); // HIDE

				GatewayCallback callback = new GatewayCallback(body, secret);
				PublicKey publicKey = gw.getPublicKey(callback.getFrom());
				PlainMessage message = callback.getMessage().decrypt(publicKey, myPrivateKey);

				System.out.println(message);
				// </CODE>

				response.getWriter().println("ok");
				baseRequest.setHandled(true);
			}
		});

		server.start();

	}

}
