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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyPair;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mountainminds.three4j.PlainMessage.DeliveryReceipt;
import com.mountainminds.three4j.PlainMessage.DeliveryReceipt.ReceiptType;
import com.mountainminds.three4j.PlainMessage.File;
import com.mountainminds.three4j.PlainMessage.Image;
import com.mountainminds.three4j.PlainMessage.Location;
import com.mountainminds.three4j.PlainMessage.Text;

import software.pando.crypto.nacl.CryptoBox;

public class EncryptionDecryptionTest {

	KeyPair alice;
	KeyPair bob;

	@BeforeEach
	public void create_keys() {
		alice = CryptoBox.keyPair();
		bob = CryptoBox.keyPair();
	}

	@Test
	public void should_encrypt_and_decrypt_text_messages() {
		var msg = new Text("secret123");

		var encrypted = msg.encrypt(alice.getPrivate(), bob.getPublic());
		var decrypted = (Text) encrypted.decrypt(alice.getPublic(), bob.getPrivate());

		assertEquals("secret123", decrypted.getText());
	}

	@Test
	public void should_encrypt_and_decrypt_location_messages() {
		var msg = new Location(46.947, 7.444, 40.0);
		msg.setNameAndAddress("Bundeshaus", "Bern");

		var encrypted = msg.encrypt(alice.getPrivate(), bob.getPublic());
		var decrypted = (Location) encrypted.decrypt(alice.getPublic(), bob.getPrivate());

		assertEquals(46.947, decrypted.getLatitude());
		assertEquals(7.444, decrypted.getLongitude());
		assertEquals(40.0, decrypted.getAccuracy());
		assertEquals("Bundeshaus", decrypted.getName());
		assertEquals("Bern", decrypted.getAddress());
	}

	@Test
	public void should_encrypt_and_decrypt_image_messages() {
		var pair = KeyGenerator.generate();
		var blob = Blob.newImage(pair.getPrivate(), pair.getPublic())
				.uploaded(BlobId.of("00001111222233334444555566667777"), 42);
		var msg = new Image(blob);

		var encrypted = msg.encrypt(alice.getPrivate(), bob.getPublic());
		var decrypted = (Image) encrypted.decrypt(alice.getPublic(), bob.getPrivate());

		assertEquals(BlobId.of("00001111222233334444555566667777"), decrypted.getBlobId());
		assertEquals(42, decrypted.getSize());
		assertEquals(blob.getNonce(), decrypted.getNonce());
	}

	@Test
	public void should_encrypt_and_decrypt_file_messages() {
		var fileblob = Blob.newFile().uploaded(BlobId.of("01234567012345670123456701234567"), 1024);
		var msg = new File(fileblob, "application/test", PlainMessage.File.RenderingType.STICKER);
		msg.setFileName("document.pdf");
		msg.setDescription("Confidential document!");
		msg.setCorrelationId("xyz");

		var thumbnailblob = fileblob.thumbnail().uploaded(BlobId.of("00001111222233334444555566667777"), 128);
		msg.setThumbnail(thumbnailblob);

		var encrypted = msg.encrypt(alice.getPrivate(), bob.getPublic());
		var decrypted = (File) encrypted.decrypt(alice.getPublic(), bob.getPrivate());

		assertEquals(fileblob.getId(), decrypted.getFile().getId());
		assertArrayEquals(fileblob.getKey().getEncoded(), decrypted.getFile().getKey().getEncoded());
		assertEquals(1024, decrypted.getFile().getSize());
		assertEquals(thumbnailblob.getId(), decrypted.getThumbnail().getId());
		assertEquals("application/test", decrypted.getMimetype());
		assertEquals(PlainMessage.File.RenderingType.STICKER, decrypted.getRenderingType());
		assertEquals("document.pdf", decrypted.getFilename());
		assertEquals("Confidential document!", decrypted.getDescription());
		assertEquals("xyz", decrypted.getCorrelationId());
	}

	@Test
	public void should_encrypt_and_decrypt_deliveryreceipt_messages() {
		var msg = new DeliveryReceipt(ReceiptType.THUMBSUP,
				List.of(MessageId.of("aaaaaaaaaaaaaaaa"), MessageId.of("bbbbbbbbbbbbbbbb")));

		var encrypted = msg.encrypt(alice.getPrivate(), bob.getPublic());
		var decrypted = (DeliveryReceipt) encrypted.decrypt(alice.getPublic(), bob.getPrivate());

		assertEquals(ReceiptType.THUMBSUP, decrypted.getReceiptType());
		assertEquals(2, decrypted.getMessageIds().size());
		assertEquals(MessageId.of("aaaaaaaaaaaaaaaa"), decrypted.getMessageIds().get(0));
		assertEquals(MessageId.of("bbbbbbbbbbbbbbbb"), decrypted.getMessageIds().get(1));
	}

	@Test()
	public void decryption_should_fail_when_you_use_the_wrong_keys() {
		var msg = new Text("secret123");
		var encrypted = msg.encrypt(alice.getPrivate(), bob.getPublic());

		var cleve = CryptoBox.keyPair();
		assertThrows(IllegalArgumentException.class, () -> encrypted.decrypt(alice.getPublic(), cleve.getPrivate()));
	}

	@Test()
	public void decryption_should_fail_when_the_message_is_altered() {
		var msg = new Text("secret123");
		var encrypted = msg.encrypt(alice.getPrivate(), bob.getPublic());
		var content = encrypted.getValue();
		content[8] = (byte) (content[8] ^ 0xff);
		new EncryptedMessage(content, encrypted.getNonce());

		assertThrows(IllegalArgumentException.class, () -> encrypted.decrypt(alice.getPublic(), bob.getPrivate()));
	}

}
