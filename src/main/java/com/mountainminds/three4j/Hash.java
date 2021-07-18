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

import static com.mountainminds.three4j.KeyEncoder.fromHex;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.security.GeneralSecurityException;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * A 32 byte hash value of user information telephone number or email address.
 */
public final class Hash extends ByteArrayValue {

	private static final String HMAC_ALGO = "HmacSHA256";

	private static final byte[] PHONE_HMAC_KEY = fromHex(
			"85adf8226953f3d96cfd5d09bf29555eb955fcd8aa5ec4f9fcd869e258370723");

	private static final byte[] EMAIL_HMAC_KEY = fromHex(
			"30a5500fed9701fa6defdb610841900febb8e430881f7ad816826264ec09bad7");

	/**
	 * Number of bytes of a hash.
	 */
	public static final int SIZE = 32;

	private Hash(byte[] value) {
		super(value, SIZE);
	}

	private static Hash of(String input, byte[] key) {
		final Mac mac = newMAC(key);
		return new Hash(mac.doFinal(input.getBytes(US_ASCII)));
	}

	/**
	 * Calculates a hash code from the given international telephone number. All non
	 * decimal digits are removed from the string before hashing.
	 * 
	 * @param phonenumber input number
	 * @return hash as specified by Threema
	 */
	public static Hash ofPhone(String phonenumber) {
		return of(phonenumber.replaceAll("[^0-9]", ""), PHONE_HMAC_KEY);
	}

	/**
	 * Calculates a hash code from the given email address. All leading and trailing
	 * white space characters are removed and all characters are converted to lower
	 * case before hashing.
	 * 
	 * @param emailaddress input email
	 * @return hash as specified by Threema
	 */
	public static Hash ofEmail(String emailaddress) {
		return of(emailaddress.strip().toLowerCase(Locale.ENGLISH), EMAIL_HMAC_KEY);
	}

	static Mac newMAC(byte[] key) {
		try {
			final Mac mac = Mac.getInstance(HMAC_ALGO);
			mac.init(new SecretKeySpec(key, HMAC_ALGO));
			return mac;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

}
