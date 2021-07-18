package com.mountainminds.three4j.guide;

import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Extracts Markdown documentation from the example code.
 */
public class ApiGuide2MD {

	private static final String SOURCE = "src/test/java/com/mountainminds/three4j/guide/ApiGuide.java";

	public static void main(String[] args) throws IOException {

		boolean code = false;

		for (String line : Files.readAllLines(Path.of(SOURCE), UTF_8)) {
			line = line.strip();
			if (line.contains("// HIDE")) {
				continue;
			}
			if (line.equals("// <CODE>")) {
				out.println();
				out.println("```java");
				code = true;
				continue;
			}
			if (line.equals("// </CODE>")) {
				out.println("```");
				out.println();
				code = false;
				continue;
			}
			if (line.startsWith("//")) {
				out.println(line.substring(2).strip());
				continue;
			}
			if (code) {
				out.println(line);
			}
		}
	}

}
