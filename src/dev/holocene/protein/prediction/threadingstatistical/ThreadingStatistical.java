package dev.holocene.protein.prediction.threadingstatistical;

import dev.holocene.protein.prediction.common.Prediction;
import dev.holocene.protein.prediction.common.Predictor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ThreadingStatistical implements Predictor {
	private ThreadingStatistical() {}

	@Override
	public String predict(String aminoacids) {
		var structure = new StringBuilder();
		for (var i = 0; i < aminoacids.length(); i++) {
			var aminoacid = aminoacids.charAt(i);
			structure.append(freq.get(aminoacid).top());
		}
		var pregenerated = structure.toString();
		var result = new StringBuilder();
		for (var i = 0; i < aminoacids.length(); i++) {
			var b2 = charAt(pregenerated, i - 2);
			var b1 = charAt(pregenerated, i - 1);
			var a1 = charAt(pregenerated, i + 1);
			var a2 = charAt(pregenerated, i + 2);
			var c = pregenerated.charAt(i);
			if (c == 'H')
				if (b2 == 'H' && b1 == 'H'
					|| b1 == 'H' && a1 == 'H'
					|| a1 == 'H' && a2 == 'H'
				)
					result.append('H');
				else if (b1 == 'S' || a1 == 'S')
					result.append('S');
				else
					result.append('C');
			else
				result.append(c);
		}
		var generated = result.toString();
		var structureCount = new HashMap<Character, Set<Integer>>();
		for (var i = 0; i < generated.length(); i++) {
			var struct = generated.charAt(i);
			structureCount.putIfAbsent(struct, new HashSet<>());
			structureCount.get(struct).add(i);
		}
		var bestModel = "";
		var bestScore = 0;
		for (var model : models.entrySet()) {
			if (!aminoacids.equals(model.getKey())) {
				var score = Prediction.scoreStrings(generated, structureCount, model.getValue().structure, model.getValue().structureCount);
				if (bestModel.equals("") || score > bestScore) {
					bestModel = model.getValue().structure;
					bestScore = score;
				}
			}
		}
		var modelLength = bestModel.length();
		var generatedLength = generated.length();
		if (modelLength > generatedLength)
			return bestModel.substring(0, generatedLength);
		return bestModel + "C".repeat(generatedLength - modelLength);
	}

	@Override
	public Learner getLearner() {
		return (aminoacids, structures) -> {
			models.put(aminoacids, new Model(structures));
			for (var i = 0; i < aminoacids.length(); i++) {
				var aminoacid = aminoacids.charAt(i);
				var structure = structures.charAt(i);
				freq.putIfAbsent(aminoacid, new AminoacidData());
				if (structure == 'H')
					freq.get(aminoacid).helix++;
				else if (structure == 'S')
					freq.get(aminoacid).strand++;
				else
					freq.get(aminoacid).coil++;
			}
		};
	}

	private final Map<Character, AminoacidData> freq = new HashMap<>();

	private static class AminoacidData {
		public int coil;
		public int helix;
		public int strand;

		public AminoacidData() {
			coil = helix = strand = 0;
		}

		public char top() {
			if (helix > Math.max(coil, strand))
				return 'H';
			if (strand > Math.max(coil, helix))
				return 'S';
			return 'C';
		}
	}

	private final Map<String, Model> models = new HashMap<>();

	private static class Model {
		public final String structure;
		public final Map<Character, Set<Integer>> structureCount = new HashMap<>();

		public Model(String structure) {
			this.structure = structure;
			for (var i = 0; i < structure.length(); i++) {
				var struct = structure.charAt(i);
				structureCount.putIfAbsent(struct, new HashSet<>());
				structureCount.get(struct).add(i);
			}
		}
	}

	private char charAt(String s, int i) {
		if (i >= s.length() || i < 0)
			return ' ';
		return s.charAt(i);
	}

	public static void main(String[] args) {
		Prediction.execute(new ThreadingStatistical());
	}
}
