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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import com.google.gson.Gson;

import software.pando.crypto.nacl.Bytes;
import software.pando.crypto.nacl.CryptoBox;

/**
 * A unencrypted Threema message. There is a subclasses for each message type.
 */
public abstract class PlainMessage {

	/**
	 * @return code of the specific message type: {@link Text#TYPE},
	 *         {@link Image#TYPE}, {@link File#TYPE} oder
	 *         {@link DeliveryReceipt#TYPE}
	 */
	public abstract int getType();

	public EncryptedMessage encrypt(PrivateKey privateKey, PublicKey publicKey) {
		var box = CryptoBox.encrypt(privateKey, publicKey, encode());
		return new EncryptedMessage(box.getCiphertextWithTag(), Nonce.of(box.getNonce()));
	}

	private final byte[] encode() {
		try (var buffer = new ByteArrayOutputStream(); var out = new DataOutputStream(buffer)) {
			out.write(getType());
			encode(out);
			// add random padding of 1 - 255 bytes (PKCS#7 style)
			int padding = Math.max(1, 0xFF & Bytes.secureRandom(1)[0]);
			for (int i = 0; i < padding; i++) {
				out.write(padding);
			}
			return buffer.toByteArray();
		} catch (IOException e) {
			// Must not happen with ByteArrayOutputStream
			throw new RuntimeException("Unexpected IOException", e);
		}
	}

	abstract void encode(DataOutputStream out) throws IOException;

	/**
	 * Decodes the binary message in the respective subtype of {@link PlainMessage}.
	 * 
	 * @param bytes encrypted binary content
	 * @return decoded message of the respective subtype
	 */
	public static PlainMessage decode(byte[] bytes) throws IllegalArgumentException {
		int padding = 0xFF & bytes[bytes.length - 1];
		try (var in = new DataInputStream(new ByteArrayInputStream(bytes, 0, bytes.length - padding))) {
			int type = in.read();
			switch (type) {
			case Text.TYPE:
				return new Text(in);
			case Image.TYPE:
				return new Image(in);
			case File.TYPE:
				return new File(in);
			case DeliveryReceipt.TYPE:
				return new DeliveryReceipt(in);
			}
			throw new IllegalArgumentException("Unknown message type: " + type);
		} catch (IOException e) {
			throw new IllegalArgumentException("Invalid message format", e);
		}
	}

	/**
	 * Simple text message.
	 */
	public final static class Text extends PlainMessage {

		public static final int TYPE = 0x01;

		private final String text;

		private Text(DataInputStream in) throws IOException {
			text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}

		public Text(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		@Override
		public int getType() {
			return TYPE;
		}

		@Override
		void encode(DataOutputStream out) throws IOException {
			out.write(text.getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public String toString() {
			return "Text[" + text + "]";
		}

	}

	/**
	 * Simple message with a reference to a image blob.
	 */
	public final static class Image extends PlainMessage {

		public static final int TYPE = 0x02;

		private final BlobId blobId;
		private final int size;
		private final Nonce nonce;

		private Image(DataInputStream in) throws IOException {
			blobId = BlobId.read(in);
			size = in.readInt();
			nonce = Nonce.read(in);
		}

		public Image(UploadedBlob blob) throws IllegalArgumentException {
			this.blobId = blob.getId();
			this.size = blob.getSize();
			this.nonce = blob.getNonce();
		}

		public BlobId getBlobId() {
			return blobId;
		}

		public int getSize() {
			return size;
		}

		public Nonce getNonce() {
			return nonce;
		}

		public UploadedBlob getBlob(SecretKey key) {
			return new UploadedBlob(key, nonce, blobId, size);
		}

		@Override
		public int getType() {
			return TYPE;
		}

		@Override
		void encode(DataOutputStream out) throws IOException {
			out.write(blobId.getValue());
			out.writeInt(size);
			out.write(nonce.getValue());
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer("File[");
			sb.append(", ").append(size);
			sb.append("]");
			return sb.toString();
		}

	}

	/**
	 * Advanced file messaged with a blob reference to the actual content and an
	 * optional thumbnail image.
	 */
	public final static class File extends PlainMessage {

		/**
		 * Hint how the content should be rendered.
		 */
		public enum RenderingType {
			DEFAULT, MEDIA, STICKER
		}

		public static final int TYPE = 0x17;

		private final UploadedBlob file;
		private final String mimetype;
		private final RenderingType renderingType;

		private UploadedBlob thumbnail;
		private String fileName;
		private String description;
		private String correlationId;

		private File(DataInputStream in) throws IOException {
			var w = new Gson().fromJson(new InputStreamReader(in, UTF_8), JsonWrapper.class);
			file = Blob.ofFile(KeyEncoder.decodeSecretKey(w.k)).uploaded(BlobId.of(w.b), w.s);
			mimetype = w.m;
			renderingType = RenderingType.values()[w.j];
			fileName = w.n;
			description = w.d;
			correlationId = w.c;
			if (w.t != null) {
				thumbnail = file.thumbnail().uploaded(BlobId.of(w.t), w.s);
			}
		}

		public File(UploadedBlob file, String mimetype, RenderingType renderingType) {
			this.file = file;
			this.mimetype = mimetype;
			this.renderingType = renderingType;
		}

		@Override
		public int getType() {
			return TYPE;
		}

		public UploadedBlob getFile() {
			return file;
		}

		public String getMimetype() {
			return mimetype;
		}

		public RenderingType getRenderingType() {
			return renderingType;
		}

		public void setThumbnail(UploadedBlob thumbnail) {
			this.thumbnail = thumbnail;
		}

		public UploadedBlob getThumbnail() {
			return thumbnail;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFilename() {
			return fileName;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public void setCorrelationId(String correlationId) {
			this.correlationId = correlationId;
		}

		public String getCorrelationId() {
			return correlationId;
		}

		@Override
		void encode(DataOutputStream out) throws IOException {
			var w = new JsonWrapper();
			w.b = file.getId().getHexValue();
			w.s = file.getSize();
			w.k = KeyEncoder.encode(file.getKey());
			w.m = mimetype;
			w.j = renderingType.ordinal();
			w.n = fileName;
			w.d = description;
			w.c = correlationId;
			if (thumbnail != null) {
				w.t = thumbnail.getId().getHexValue();
			}
			out.write(new Gson().toJson(w).getBytes(UTF_8));
		}

		private static class JsonWrapper {
			String b, t, k, m, n, d, c;
			int s, j;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer("File[");
			sb.append(file.getId());
			if (thumbnail != null) {
				sb.append(", ").append(thumbnail.getId());
			}
			sb.append(", ").append(mimetype);
			if (description != null) {
				sb.append(", ").append(description);
			}
			sb.append("]");
			return sb.toString();
		}
	}

	/**
	 * Delivery receipt.
	 */
	public final static class DeliveryReceipt extends PlainMessage {

		/**
		 * The type of receipt.
		 */
		public enum ReceiptType {
			UNDEFINED, // just to reserve 0x00
			RECEIVED, //
			READ, //
			THUMBSUP, //
			THUMBSDOWN
		}

		public static final int TYPE = 0x80;

		private final ReceiptType receiptType;
		private final List<MessageId> messageIds;

		private DeliveryReceipt(DataInputStream in) throws IOException {
			receiptType = ReceiptType.values()[in.read()];
			messageIds = new ArrayList<>();
			MessageId id;
			while ((id = MessageId.read(in)) != null) {
				messageIds.add(id);
			}
		}

		public DeliveryReceipt(ReceiptType receiptType, List<MessageId> messageIds) {
			this.receiptType = receiptType;
			this.messageIds = messageIds;
		}

		@Override
		public int getType() {
			return TYPE;
		}

		public ReceiptType getReceiptType() {
			return receiptType;
		}

		public List<MessageId> getMessageIds() {
			return messageIds;
		}

		@Override
		void encode(DataOutputStream out) throws IOException {
			out.write(receiptType.ordinal());
			for (var id : messageIds) {
				out.write(id.getValue());
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("DeliveryReceipt[");
			sb.append(receiptType);
			for (MessageId id : messageIds) {
				sb.append(", ").append(id);
			}
			sb.append("]");
			return sb.toString();
		}

	}

}
