package com.mountainminds.three4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EncryptedMessageTest {

	@Test
	public void should_allow_up_to_4000_bytes() {
		var value = new byte[4000];
		var nounce = Nonce.of(new byte[24]);
		new EncryptedMessage(value, nounce);
	}

	@Test
	public void should_not_allow_more_than_4000_bytes() {
		var value = new byte[4001];
		var nounce = Nonce.of(new byte[24]);
		var e = assertThrows(IllegalArgumentException.class, () -> new EncryptedMessage(value, nounce));
		assertEquals("Content too large: 4001", e.getMessage());
	}

}
