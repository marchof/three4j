// *****************************************************************************
// Copyright (c) 2021 Mountainminds GmbH & Co. KG
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
// SPDX-License-Identifier: MIT
// *****************************************************************************

/**
 * Java client for the <a href="https://gateway.threema.ch/">Threema
 * Gateway</a>. See <a href="https://github.com/marchof/three4j">the project page</a>
 * for more documentation and updates.
 */
module com.mountainminds.three4j {
	exports com.mountainminds.three4j;

	requires software.pando.crypto.nacl;
	requires com.google.gson;
	requires java.net.http;
	
	opens com.mountainminds.three4j to com.google.gson;
}
