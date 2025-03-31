package dev.holocene.protein.tool;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class ConvertToMLInputs {
	private ConvertToMLInputs() {}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		var inputRoot = Paths.get("amnacd");
		var outputRoot = Paths.get("struct");
		try (var aIn = new FileOutputStream("ain.bytes"); var aOut = new FileOutputStream("aout.bytes");
			var bIn = new FileOutputStream("bin.bytes"); var bOut = new FileOutputStream("bout.bytes");
			var cIn = new FileOutputStream("min.bytes"); var cOut = new FileOutputStream("mout.bytes");
			var in = new FileOutputStream("in.bytes"); var out = new FileOutputStream("out.bytes");
		) {
			Files.walk(inputRoot).forEach(input -> {
				if (Files.isRegularFile(input)) {
					try {
						var outIn = select(inputRoot.relativize(input).getParent().toString(), aIn, bIn, cIn);
						var outOut = select(inputRoot.relativize(input).getParent().toString(), aOut, bOut, cOut);
						var protein = new String(Files.readAllBytes(input));
						var filename = inputRoot.relativize(input).toString();
						var output = outputRoot.resolve(filename.substring(0, filename.lastIndexOf(".")) + ".struct");
						var structure = new String(Files.readAllBytes(output));
						for (var j = 0; j < protein.length(); j++) {
							if (output.getFileName().toString().getBytes(StandardCharsets.UTF_8).length != 13)
								System.err.println(output.getFileName().toString());
							outIn.write(output.getFileName().toString().getBytes(StandardCharsets.UTF_8));
							for (var i = j - 31; i <= j + 31; i++) {
								if (i < protein.length() && i >= 0) {
									outIn.write(AMINOACIDS.indexOf(protein.charAt(i)) + 1);
									in.write(AMINOACIDS.indexOf(protein.charAt(i)) + 1);
								} else {
									outIn.write(0);
									in.write(0);
								}
							}
							if (structure.charAt(j) == 'H') {
								outOut.write(0);
								out.write(0);
							} else if (structure.charAt(j) == 'S') {
								outOut.write(1);
								out.write(1);
							} else {
								outOut.write(2);
								out.write(2);
							}
						}
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static FileOutputStream select(String x, FileOutputStream a, FileOutputStream b, FileOutputStream c) {
		switch (x) {
			case "alpha":
				return a;
			case "beta":
				return b;
			case "mixed":
				return c;
		}
		return null;
	}

	private static final List<Character> AMINOACIDS = List.of('A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'Y');
}
