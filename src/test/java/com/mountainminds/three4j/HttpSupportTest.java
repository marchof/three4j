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

import static com.mountainminds.three4j.HttpSupport.UNKNOWN_RESPONSE;
import static com.mountainminds.three4j.HttpSupport.decodeUrlParams;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.mountainminds.three4j.HttpSupport.MultipartEncoder;
import com.mountainminds.three4j.HttpSupport.UrlParams;

public class HttpSupportTest {

	@Test
	public void UNKNOWN_RESPONSE_should_always_throw_a_exception() {
		var e = assertThrows(GatewayException.class, () -> UNKNOWN_RESPONSE.handle(123));
		assertEquals(123, e.getStatus());
		assertEquals("Unknown response (123)", e.getMessage());
	}

	@Test
	public void StatusHandler_ok_should_accept_200() throws GatewayException {
		UNKNOWN_RESPONSE.ok().handle(200);
	}

	@Test
	public void StatusHandler_ok_should_not_handle_other_codes_than_200() {
		assertThrows(GatewayException.class, () -> UNKNOWN_RESPONSE.ok().handle(201));
	}

	@Test
	public void StatusHandler_error_should_throw_specific_exception() {
		var e = assertThrows(GatewayException.class, () -> UNKNOWN_RESPONSE.error(555, "test error").handle(555));
		assertEquals(555, e.getStatus());
		assertEquals("test error (555)", e.getMessage());
	}

	@Test
	public void StatusHandler_error_should_not_handle_other_codes() {
		var e = assertThrows(GatewayException.class, () -> UNKNOWN_RESPONSE.error(555, "error").handle(500));
		assertEquals(500, e.getStatus());
		assertEquals("Unknown response (500)", e.getMessage());
	}

	@Test
	public void multipartEncoder_should_create_correct_multipart_content() throws IOException {
		var encoder = new MultipartEncoder("hello".getBytes(US_ASCII), new Random(0));
		assertEquals("multipart/form-data;boundary=22Pbd7157KhLr8Ry8RZmz66hYkdm", encoder.getContentType());
		assertEquals("--22Pbd7157KhLr8Ry8RZmz66hYkdm\r\n" //
				+ "Content-Disposition: form-data;name=\"blob\";filename=\"blob\"\r\n" //
				+ "\r\n" //
				+ "hello\r\n" //
				+ "--22Pbd7157KhLr8Ry8RZmz66hYkdm--\r\n", new String(encoder.getBody(), US_ASCII));
	}

	@Test
	public void urlparams_should_encode_parameters() {
		var params = new UrlParams();
		params.add("a", "123").add("b", "Hello+World!");
		assertEquals("a=123&b=Hello%2BWorld%21", params.toString());
	}

	@Test
	public void decodeUrlParams_should_decode_params() {
		assertEquals(Map.of("a", "123", "b", "Hello World!"), decodeUrlParams("a=123&b=Hello+World%21"));
	}

	@Test
	public void decodeUrlParams_should_handle_empty_string() {
		assertEquals(Map.of(), decodeUrlParams(""));
	}

	@Test
	public void decodeUrlParams_should_handle_invalid_input1() {
		assertEquals(Map.of(), decodeUrlParams("justthis"));
	}

	@Test
	public void decodeUrlParams_should_handle_invalid_input2() {
		assertEquals(Map.of("a", "123"), decodeUrlParams("a=123&justthis"));
	}

}
