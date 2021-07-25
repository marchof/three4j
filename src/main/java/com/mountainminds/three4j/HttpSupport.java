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

import static com.mountainminds.three4j.GatewayException.STATUS_OK;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
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

	static final String MULTIPART_BOUNDARY = "xZK2aOVCeCybl1bbgvCEas6n4cdntpzkpcLWA12SahAiBrDrkIBj3W2HMPghi3Bo";

	/**
	 * Encode binary content as multipart/form-data body according to
	 * <a href="https://tools.ietf.org/html/rfc2046">RFC 2046</a>.
	 */
	static byte[] blobBody(byte[] blob) {
		try (var out = new ByteArrayOutputStream(); var printer = new PrintWriter(out, true, US_ASCII) {
			public void println() {
				// Ensure CRLF line endings on every platform
				write('\r');
				write('\n');
				flush();
			};
		}) {
			printer.println("--" + MULTIPART_BOUNDARY);
			printer.println("Content-Disposition: form-data;name=\"blob\";filename=\"blob\"");
			printer.println();
			out.write(blob);
			printer.println();
			printer.println("--" + MULTIPART_BOUNDARY + "--");
			return out.toByteArray();
		} catch (IOException e) {
			// Must not happen with ByteArrayOutputStream
			throw new RuntimeException(e);
		}
	}

	/**
	 * Holder and encoder for URL parameters.
	 */
	static class UrlParams {

		private StringBuilder buffer = new StringBuilder();

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
				.collect(Collectors.toMap(k -> URLDecoder.decode(k[0], US_ASCII),
						v -> URLDecoder.decode(v[1], US_ASCII)));
	}

	private HttpSupport() {
		// no instances
	}

}
