Three4J - Threema Client API for Java
=====================================

A client API implementation for the [Threema Gateway](https://gateway.threema.ch/)
which supports exchanging end-to-end encrypted messages including images and
arbitrary files. The primary purpose of this implementation is to provide a
small Maven artifact which can be easily integrated in any Java applications.
The implementation is based on the [Java 11 HTTP client](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/package-summary.html)
and [Salty Coffee](https://github.com/NeilMadden/salty-coffee) for NaCl encryption.

The following resources have been used to implement this API:

* [Threema Message API](https://gateway.threema.ch/en/developer/api)
* [Cryptography Whitepaper](https://threema.ch/press-files/2_documentation/cryptography_whitepaper.pdf)
* [Threema Encryption Validation](https://threema.ch/validation/)


## Prerequisites

Three4J requires Java 11 or later.

To use this library you need a [Threema Gateway](https://gateway.threema.ch/)
account with at least one Threema ID and the corresponding key pair. Three4J
comes with a main class to generate a new key pair:

```
$ java com.mountainminds.three4j.KeyGenerator

PLEASE KEEP THE GENERATED KEYS CONFIDENTIAL IF YOU USE THEM WITH A THREEMA ID
IF YOU LOOSE THE KEY OF A THREEMA ID IT CANNOT BE USED ANY MORE FOR MESSAGING

 public key: e25f5e489c458a9eb9d0c07ae44fba34a91b4bd20093e854400998abxxxxxxxx
private key: 93f9873676db87ed2e3ec603645a360c529eb998b35afec17d7cd066xxxxxxxx
```

Please consult [Threema's documentation](https://gateway.threema.ch/en/developer/api)
how to create a gateway Threema ID.


## API Usage Guide

### Setup

To use the gateway API client you need a gateway Threema ID and the
corresponding secret which was issued by the gateway admin interface:

```java
ThreemaID from = ThreemaID.of("*YOURGWY"); // Insert your ID here
String secret = // e.g. "JSH5y9DfvOROm2Iw", retrieve this from a secure location
```

All requests are send through a `Gateway` instance:

```java
Gateway gw = new Gateway(from, secret);
```

If you want to send end-to-end encrypted messages (which is recommended)
you need your private 32 byte Threema key. The hexadecimal string
representation has 64 characters. Make sure you store this key securely
and do not make it available to others:

```java
String myPrivateKeyStr = // retrieve this from a secure location
PrivateKey myPrivateKey = KeyEncoder.decodePrivateKey(myPrivateKeyStr);
```

### Lookups

Sending a Threema message requires you to know the eight character
long Threema ID of the receiver. Users my choose to register their
telephone number or email address with their account which can then
be queried to lookup their Threema ID. Note that we do not disclose
the actual data but only send hash values:

```java
ThreemaID receiverId = gw.getIdByPhoneNumber(Hash.ofPhone("+411234567"));
System.out.println(receiverId);
```

Or by Email address:

```java
receiverId = gw.getIdByEmailAddress(Hash.ofEmail("test@example.com"));
System.out.println(receiverId);
```

Depending on the client the receiver might be able to process certain
message types only. You can check the capabilities of a given Threema
ID:

```java
System.out.println("Capabilities: " + gw.getCapabilities(receiverId));
```

### Encrypted Text Messages

To send a encrypted message we need the public key of the receiver
which can be obtained via the gateway API. For better information
security you should consider obtaining the public key physically
from the receivers device e.g. from the QR code.

```java
PublicKey receiverPublicKey = gw.getPublicKey(receiverId);
```

To ensure end-to-end encryption you create and encrypt the message
locally before you send it to the gateway:

```java
String text = String.format("Secret message at %s.", Instant.now());

PlainMessage msg = new PlainMessage.Text(text);
EncryptedMessage encrypted = msg.encrypt(myPrivateKey, receiverPublicKey);
MessageId messageId = gw.sendMessage(receiverId, encrypted);

System.out.println("Message ID: " + messageId);
```

### Encrypted Image Messages

Sending images requires two steps. First we uploading the image as a
encrypted blob. Similarly as for the actual message the encryption
key for the blob is calculated from our private key and the receivers
public key.

```java
byte[] image = Files.readAllBytes(Path.of("src/test/resources/image.jpg"));

Blob blob = Blob.newImage(myPrivateKey, receiverPublicKey);
UploadedBlob uploadedBlob = gw.enrcryptAndUploadBlob(blob, image);
```

A reference to the uploaded blob needs then to be used in the image message:

```java
PlainMessage imgMsg = new PlainMessage.Image(uploadedBlob);
EncryptedMessage encrypted = imgMsg.encrypt(myPrivateKey, receiverPublicKey);
gw.sendMessage(receiverId, encrypted);
```

We can also download and decrypt our image blob again:

```java
byte[] downloadedImage = gw.downloadAndDecryptBlob(uploadedBlob);
Files.write(Path.of("target/download.jpg"), downloadedImage);
```

### Encrypted File Messages

Like images we can encrypt and send arbitrary files. But unlike
images files are encrypted with a random key which is then
transmitted the with the corresponding message. Also for files an
optional preview image can be added which must be encrypted with the
same key than the file. The Three4J API makes this process simple:

```java
byte[] file = Files.readAllBytes(Path.of("src/test/resources/document.pdf"));
UploadedBlob uploadedFileBlob = gw.enrcryptAndUploadBlob(Blob.newFile(), file);

byte[] thumbnail = Files.readAllBytes(Path.of("src/test/resources/thumbnail.png"));
Blob thumbnailBlob = uploadedFileBlob.thumbnail();
UploadedBlob uploadedThumbnailBlob = gw.enrcryptAndUploadBlob(thumbnailBlob, thumbnail);
```

Construction a file message requires a bit of meta data like the MIME
type of the file.

```java
PlainMessage.File fileMsg = new PlainMessage.File(uploadedFileBlob, "application/pdf", RenderingType.DEFAULT);
fileMsg.setThumbnail(uploadedThumbnailBlob);
fileMsg.setFileName("document.pdf");

EncryptedMessage encrypted = fileMsg.encrypt(myPrivateKey, receiverPublicKey);
gw.sendMessage(receiverId, encrypted);
```

### Simple, Unencrypted Messages

With a *basic mode* gateway ID you can directly send a plain text
message to a given Threema ID without local encryption. The key pair
is managed for you on the gateway server. Please rather consider
using end-to-end encryption as described above.

```java
gw.sendSimpleMessage(ThreemaID.of("ABCDEFGH"), "Not so secret message.");
```

Alternatively you can also use a international telephone number or a
email address to send a message to if the users has registered them
with Threema. Note that the telephone number or the email address is
disclosed to the Threema gateway.

```java
gw.sendSimpleMessageToPhoneNumber("41791234567", "Not so secret message.");
gw.sendSimpleMessageToEmailAddress("test@example.com", "Not so secret message.");
```

### Account Information

Threema charges you for messages and blob uploads via the gateway.
You can query the remaining credits via API:

```java
System.out.println("Remaining credits: " + gw.getRemainingCredits());
```

### Callback Handling

You can configure your own HTTP server to receive messages from the
Threema gateway. The corresponding endpoint must be visible from the
public internet of course. The payload can be decoded with the
`GatewayCallback` class:

```java
byte[] body = // unprocessed body received from the HTTP server of your choice

GatewayCallback callback = new GatewayCallback(body, secret);
PublicKey publicKey = gw.getPublicKey(callback.getFrom());
PlainMessage message = callback.getMessage().decrypt(publicKey, myPrivateKey);

System.out.println(message);
```


## Security Disclaimer

This project has been implemented for personal use only. There was no
independent security audit. Please perform your own security audit before using
this software to send sensitive content!

Also make sure you never disclose a private key of a Threema ID. The private
key will allow to send messages in your name and decode all content that has
been sent to you.


## License

This code is provided "as is" under the [MIT License](LICENSE.md), without warranty of any kind.


## Trademarks

The [Threema Messenger](https://threema.ch/) and the [Messaging Gateway](https://gateway.threema.ch/)
are products of Threema GmbH, Switzerland. This project has no affiliation to
Threema GmbH. Please have a look at their [website](https://threema.ch/) and
consider using their products for secure end-to-end encrypted communication.