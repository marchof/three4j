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

import com.mountainminds.three4j.PlainMessage.File.RenderingType;

public class PlainMessageFileTest {

	@Test
	public void toString_should_return_blobid_mimetype_renderingtype() {
		var blob = new UploadedBlob(
				KeyEncoder.decodeSecretKey("1111222233334444555566667777888811112222333344445555666677778888"),
				Nonce.of("111122223333111122223333111122223333111122223333"),
				BlobId.of("11112222333344445555666677778888"), 42000);
		var msg = new PlainMessage.File(blob, "image/jpg", RenderingType.MEDIA);
		assertEquals("File[11112222333344445555666677778888, image/jpg, MEDIA]", msg.toString());
	}

}
