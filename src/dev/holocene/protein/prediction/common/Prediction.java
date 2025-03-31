package dev.holocene.protein.prediction.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Prediction {
	private Prediction() {}

	private static String currentStructure;

	@SuppressWarnings("resource")
	public static void execute(Predictor predictor) {
		var inputRoot = Paths.get("amnacd");
		var templateRoot = Paths.get("struct");
		var outputRoot = Paths.get("struct_prdctd");
		try {
			var learner = predictor.getLearner();
			if (learner != null) {
				Files.walk(inputRoot).forEach(input -> {
					try {
						if (Files.isRegularFile(input)) {
							var filename = inputRoot.relativize(input).toString();
							var template = templateRoot.resolve(filename.substring(0, filename.lastIndexOf(".")) + ".struct");
							learner.learn(new String(Files.readAllBytes(input)), new String(Files.readAllBytes(template)));
						}
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			predictor.prepare();
			var statistics = new HashMap<String, Statistics>();
			Files.walk(inputRoot).forEach(input -> {
				try {
					if (Files.isRegularFile(input)) {
						var filename = inputRoot.relativize(input).toString();
						currentStructure = filename.substring(0, filename.lastIndexOf(".")) + ".struct";
						var prediction = predictor.predict(new String(Files.readAllBytes(input)));
						var output = outputRoot.resolve(currentStructure);
						Files.createDirectories(output.getParent());
						Files.write(output, prediction.getBytes());
						var template = templateRoot.resolve(currentStructure);
						var templateStructure = new String(Files.readAllBytes(template));
						var statId = Paths.get(filename).getParent().toString();
						statistics.putIfAbsent(statId, new Statistics());
						for (var i = 0; i < prediction.length(); i++)
							statistics.get(statId).mark(prediction.charAt(i), templateStructure.charAt(i));
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			for (var i : statistics.entrySet())
				System.out.println(i.getKey().toUpperCase() + "\n" + i.getValue() + "\n");
			System.out.println("TOTAL\n" + Statistics.sum(statistics.values()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int scoreStrings(String a, Map<Character, Set<Integer>> aMap, String b, Map<Character, Set<Integer>> bMap) {
		var all = new HashSet<>(aMap.keySet());
		all.addAll(bMap.keySet());
		var score = 0;
		var cost = Math.abs(a.length() - b.length());
		for (var c : all) {
			var aSet = aMap.getOrDefault(c, Set.of());
			var bSet = bMap.getOrDefault(c, Set.of());
			cost += Math.abs(aSet.size() - bSet.size());
			var common = new HashSet<>(aSet);
			common.retainAll(bSet);
			score += common.size();
		}
		return score - cost / 2;
	}

	public static String getCustomData() {
		try {
			return new String(Files.readAllBytes(Paths.get("custom").resolve(currentStructure)));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
