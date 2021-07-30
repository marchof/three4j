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

import static com.mountainminds.three4j.BulkLookup.writeRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class BulkLookupTest {

	@Test
	public void writeRequest_should_create_json() {
		var actual = writeRequest(Set.of(Hash.ofPhone("41791111111")), Set.of(Hash.ofEmail("test@example.com")));
		var expected = "{\"phoneHashes\":[\"6fdcfe848aa3d4e43f0760a9fd4dc57155b99897da3943d58bee7990a01ef0f4\"],"
				+ "\"emailHashes\":[\"bb4b64e6e3e9c1222ddac9c6f6d947a0ab74c0b230b7075b5ea8b5a32027222c\"]}";
		assertEquals(expected, actual);
	}

	@Test
	public void readResponse_should_parse_json() {
		var json = "[{\"phoneHash\":\"6fdcfe848aa3d4e43f0760a9fd4dc57155b99897da3943d58bee7990a01ef0f4\",\"identity\":\"AAAAAAAA\",\"publicKey\": \"e58771baf2db70989d0724ef77ba6bf867d46aaa24fc2c3f8f0f144d89a6264b\"},"
				+ "{\"emailHash\":\"bb4b64e6e3e9c1222ddac9c6f6d947a0ab74c0b230b7075b5ea8b5a32027222c\",\"identity\":\"BBBBBBBB\",\"publicKey\": \"6a2bd9a0912d4ce0e5c6fc6c9b8ac14a8fdb6282a34c7e0f5fe57d57c54fb69f\"}]";
		var actual = BulkLookup.readResponse(json);
		var k1 = actual.get(Hash.ofPhone("41791111111"));
		assertEquals(ThreemaId.of("AAAAAAAA"), k1.getId());
		assertEquals(KeyEncoder.decodePublicKey("e58771baf2db70989d0724ef77ba6bf867d46aaa24fc2c3f8f0f144d89a6264b"),
				k1.getKey());
		var k2 = actual.get(Hash.ofEmail("test@example.com"));
		assertEquals(ThreemaId.of("BBBBBBBB"), k2.getId());
		assertEquals(KeyEncoder.decodePublicKey("6a2bd9a0912d4ce0e5c6fc6c9b8ac14a8fdb6282a34c7e0f5fe57d57c54fb69f"),
				k2.getKey());
	}

}
