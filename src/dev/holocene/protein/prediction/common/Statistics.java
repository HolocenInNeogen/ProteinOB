package dev.holocene.protein.prediction.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;

public class Statistics {
	@SuppressWarnings("resource")
	public final static void main(String[] args) {
		var predictedRoot = Paths.get("struct_prdctd");
		var templateRoot = Paths.get("struct");
		var statistics = new HashMap<String, Statistics>();
		try {
			Files.walk(predictedRoot).forEach(predicted -> {
				if (Files.isRegularFile(predicted)) {
					try {
						var predictedStructure = new String(Files.readAllBytes(predicted));
						var filename = predictedRoot.relativize(predicted);
						var template = templateRoot.resolve(filename);
						var templateStructure = new String(Files.readAllBytes(template));
						var statId = filename.getParent().toString();
						statistics.putIfAbsent(statId, new Statistics());
						for (var i = 0; i < predictedStructure.length(); i++)
							statistics.get(statId).mark(predictedStructure.charAt(i), templateStructure.charAt(i));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			});
			for (var i : statistics.entrySet())
				System.out.println(i.getKey().toUpperCase() + "\n" + i.getValue() + "\n");
			System.out.println("TOTAL\n" + sum(statistics.values()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int[][] stats = new int[3][3];

	public void mark(char prediction, char correct) {
		stats[charToInt(prediction)][charToInt(correct)]++;
	}

	@Override
	public String toString() {
		var result = new StringBuilder();
		result.append(accuracy());
		result.append("%\nEXP ->\tC\tH\tS\n");
		result.append("C\t");
		for (var i = 0; i < 3; i++) {
			result.append(stats[0][i]);
			result.append("\t");
		}
		result.append("\nH\t");
		for (var i = 0; i < 3; i++) {
			result.append(stats[1][i]);
			result.append("\t");
		}
		result.append("\nS\t");
		for (var i = 0; i < 3; i++) {
			result.append(stats[2][i]);
			result.append("\t");
		}
		return result.toString();
	}

	public double accuracy() {
		var total = 0;
		for (var i = 0; i < 3; i++)
			for (var j = 0; j < 3; j++)
				total += stats[i][j];
		return 100.0 * (stats[0][0] + stats[1][1] + stats[2][2]) / total;
	}

	public static Statistics sum(Collection<Statistics> statistics) {
		var sum = new Statistics();
		for (var stat : statistics) {
			for (var i = 0; i < 3; i++)
				for (var j = 0; j < 3; j++)
					sum.stats[i][j] += stat.stats[i][j];
		}
		return sum;
	}

	private int charToInt(char c) {
		if (c == 'H')
			return 1;
		if (c == 'S')
			return 2;
		return 0;
	}
}
