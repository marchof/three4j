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

import java.io.IOException;

/**
 * Exception thrown when the gateway answers with a HTTP status codes different
 * from 200 (OK).
 */
public class GatewayException extends IOException {

	public static int STATUS_OK = 200;

	public static int STATUS_BADREQUEST = 400;
	public static int STATUS_UNAUTHORIZED = 401;
	public static int STATUS_PAYMENTREQUIRED = 402;
	public static int STATUS_NOTFOUND = 404;
	public static int STATUS_PAYLOADTOOLARGE = 413;

	public static int STATUS_INTERNALSERVERERROR = 500;

	private static final long serialVersionUID = 1L;

	private int status;

	/**
	 * New exception with the given status and message.
	 * 
	 * @param status  HTTP status
	 * @param message specific message
	 */
	public GatewayException(int status, String message) {
		super(message + " (" + status + ")");
		this.status = status;
	}

	/**
	 * @return HTTP status
	 */
	public int getStatus() {
		return status;
	}

}
