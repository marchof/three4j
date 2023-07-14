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

import static com.mountainminds.three4j.GatewayException.STATUS_OK;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Internal collection of support utilities to issue HTTP requests.
 */
final class HttpSupport {

	static final StatusHandler UNKNOWN_RESPONSE = (status) -> {
		throw new GatewayException(status, "Unknown response");
	};

	/**
	 * Builder and handler to declare possible HTTP response codes per request.
	 */
	interface StatusHandler {

		abstract void handle(int status) throws GatewayException;

		default StatusHandler error(int errorstatus, Supplier<String> errormessage) {
			return (status) -> {
				if (errorstatus == status) {
					throw new GatewayException(errorstatus, errormessage.get());
				}
				StatusHandler.this.handle(status);
			};
		}

		default StatusHandler error(int errorstatus, String errormessage) {
			return error(errorstatus, () -> errormessage);
		}

		default StatusHandler ok() {
			return (status) -> {
				if (status != STATUS_OK) {
					StatusHandler.this.handle(status);
				}
			};
		}
	}

	/**
	 * Encode binary content as multipart/form-data body according to
	 * <a href="https://tools.ietf.org/html/rfc2046">RFC 2046</a>.
	 */
	static class MultipartEncoder {

		private static ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(SecureRandom::new);

		private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

		private static final int LEN = 28;

		private final byte[] content;
		private final String boundary;

		MultipartEncoder(byte[] content) {
			this(content, RANDOM.get());
		}

		MultipartEncoder(byte[] content, Random rand) {
			this.content = content;
			this.boundary = createBoundary(rand);
		}

		private static String createBoundary(Random rand) {
			return new String(rand.ints(LEN, 0, CHARS.length()).map(CHARS::charAt).toArray(), 0, LEN);
		}

		String getContentType() {
			return "multipart/form-data;boundary=" + boundary;
		}

		byte[] getBody() {
			try (var out = new ByteArrayOutputStream() {
				public void println() throws IOException {
					write('\r');
					write('\n');
				};

				public void println(String text) throws IOException {
					write(text.getBytes(US_ASCII));
					println();
				};
			}) {
				out.println("--" + boundary);
				out.println("Content-Disposition: form-data;name=\"blob\";filename=\"blob\"");
				out.println();
				out.write(content);
				out.println();
				out.println("--" + boundary + "--");
				return out.toByteArray();
			} catch (IOException e) {
				// Must not happen with ByteArrayOutputStream
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * Holder and encoder for URL parameters.
	 */
	static class UrlParams {

		private final StringBuilder buffer = new StringBuilder();

		UrlParams add(String key, String value) {
			if (buffer.length() > 0) {
				buffer.append('&');
			}
			buffer.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
			buffer.append('=');
			buffer.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
			return this;
		}

		BodyPublisher toBody() {
			return HttpRequest.BodyPublishers.ofString(toString());
		}

		@Override
		public String toString() {
			return buffer.toString();
		}
	}

	/**
	 * Decodes URL encoded parameters.
	 * 
	 * @param paramstr encoded parameters
	 * @return Map of parameters
	 */
	static Map<String, String> decodeUrlParams(String paramstr) {
		return Arrays.stream(paramstr.split("&")) //
				.map(s -> s.split("=")) //
				.filter(array -> array.length == 2) //
				.collect(Collectors.toMap(k -> URLDecoder.decode(k[0], US_ASCII),
						v -> URLDecoder.decode(v[1], US_ASCII)));
	}

	private HttpSupport() {
		// no instances
	}

}
