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
package com.mountainminds.three4j.guide;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Extracts Markdown documentation from the example code.
 */
public class ApiGuide2MD {

	static final Path README = Path.of("README.md");

	static final Path SOURCE = Path.of("src/test/java/com/mountainminds/three4j/guide/ApiGuide.java");

	static final String API_CHAPTER = "## API Usage Guide";

	public static void main(String[] args) throws IOException {
		List<String> original = Files.readAllLines(README, UTF_8);
		try (var out = new PrintStream(Files.newOutputStream(README), true, UTF_8)) {
			writeUpdatedAPIGuide(original, out::println);
		}
	}

	public static void writeUpdatedAPIGuide(List<String> original, Consumer<String> out) throws IOException {
		boolean keep = true;
		for (var line : original) {
			if (API_CHAPTER.equals(line)) {
				out.accept(line);
				out.accept("");
				writeAPIGuide(out);
				out.accept("");
				keep = false;
			} else if (line.startsWith("## ")) {
				keep = true;
			}
			if (keep) {
				out.accept(line);
			}
		}
	}

	public static void writeAPIGuide(Consumer<String> out) throws IOException {

		boolean code = false;

		for (String line : Files.readAllLines(SOURCE, UTF_8)) {
			line = line.strip();
			if (line.contains("// HIDE")) {
				continue;
			}
			if (line.equals("// <CODE>")) {
				out.accept("");
				out.accept("```java");
				code = true;
				continue;
			}
			if (line.equals("// </CODE>")) {
				out.accept("```");
				out.accept("");
				code = false;
				continue;
			}
			if (line.startsWith("//")) {
				out.accept(line.substring(2).strip());
				continue;
			}
			if (code) {
				out.accept(line);
			}
		}
	}

}
