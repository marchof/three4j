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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mountainminds.three4j.PlainMessage.DeliveryReceipt.ReceiptType;

public class PlainMessageDeliveryReceiptTest {

	@Test
	public void toString_should_return_type_and_messageids() {
		var ids = List.of(MessageId.of("1111111111111111"), MessageId.of("2222222222222222"));
		var msg = new PlainMessage.DeliveryReceipt(ReceiptType.READ, ids);
		assertEquals("DeliveryReceipt[READ, 1111111111111111, 2222222222222222]", msg.toString());
	}

}
