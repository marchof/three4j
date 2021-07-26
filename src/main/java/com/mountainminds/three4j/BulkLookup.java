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

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Internal utility to create bulk lookup requests and parse responses.
 */
final class BulkLookup {

	static String writeRequest(Set<Hash> phoneHashes, Set<Hash> emailHashes) {
		return new Gson().toJson(new Request(phoneHashes, emailHashes));
	}

	static class Request {
		List<String> phoneHashes, emailHashes;

		public Request(Set<Hash> phoneHashes, Set<Hash> emailHashes) {
			this.phoneHashes = phoneHashes.stream().map(Hash::getHexValue).collect(toList());
			this.emailHashes = emailHashes.stream().map(Hash::getHexValue).collect(toList());
		}
	}

	static HashMap<Hash, Gateway.IDKey> readResponse(String body) {
		List<ResponseItem> items = new Gson().fromJson(body, new TypeToken<List<ResponseItem>>() {
		}.getType());
		var result = new HashMap<Hash, Gateway.IDKey>();
		for (var i : items) {
			var id = new Gateway.IDKey(i.identity, i.publicKey);
			if (i.phoneHash != null) {
				result.put(Hash.of(i.phoneHash), id);
			}
			if (i.emailHash != null) {
				result.put(Hash.of(i.emailHash), id);
			}
		}
		return result;
	}

	static class ResponseItem {
		String phoneHash, emailHash, identity, publicKey;
	}

	private BulkLookup() {
		// no instances
	}

}
