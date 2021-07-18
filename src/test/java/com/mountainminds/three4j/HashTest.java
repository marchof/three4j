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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HashTest {

	@Test
	void ofPhone_should_create_hashes_as_specified() {
		assertEquals("ad398f4d7ebe63c6550a486cc6e07f9baa09bd9d8b3d8cb9d9be106d35a7fdbc",
				Hash.ofPhone("+41 79 123 45-67").getHexValue());
	}

	@Test
	void ofEmail_should_create_hashes_as_specified() {
		assertEquals("1ea093239cc5f0e1b6ec81b866265b921f26dc4033025410063309f4d1a8ee2c",
				Hash.ofEmail("  Test@Threema.ch ").getHexValue());
	}

}
