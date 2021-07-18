package com.mountainminds.three4j;

import static com.mountainminds.three4j.KeyEncoder.encode;
import static java.lang.System.out;

import java.security.KeyPair;

import software.pando.crypto.nacl.CryptoBox;

/**
 * Utility to create key pairs that can be used for a new Threema ID. Please
 * keep the generated key confidential if you use them with a Threema ID. If you
 * loose the key of a threema ID it cannot be used any more for messaging.
 */
public final class KeyGenerator {

	private KeyGenerator() {
	}

	/**
	 * Create a new random key pair. You can use the methods
	 * {@link KeyEncoder#encode(java.security.PublicKey)} and
	 * {@link KeyEncoder#encode(java.security.PrivateKey)} to print text
	 * representations of your key.
	 * 
	 * @return new random key pair
	 */
	public static KeyPair generate() {
		return CryptoBox.keyPair();
	}

	public static void main(String[] args) {
		var keyPair = generate();
		out.println("PLEASE KEEP THE GENERATED KEYS CONFIDENTIAL IF YOU USE THEM WITH A THREEMA ID");
		out.println("IF YOU LOOSE THE KEY OF A THREEMA ID IT CANNOT BE USED ANY MORE FOR MESSAGING");
		out.println();
		out.println(" public key: " + encode(keyPair.getPublic()));
		out.println("private key: " + encode(keyPair.getPrivate()));
	}

}
