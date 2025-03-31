package dev.holocene.protein.prediction.threadingml;

import dev.holocene.protein.prediction.common.Prediction;
import dev.holocene.protein.prediction.common.Predictor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ThreadingML implements Predictor {
	private ThreadingML() {}

	@Override
	public String predict(String aminoacids) {
		var generated = Prediction.getCustomData();
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
		return bestModel + generated.substring(modelLength);
	}

	@Override
	public Learner getLearner() {
		return (aminoacids, structures) -> {
			models.put(aminoacids, new Model(structures));
		};
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

	public static void main(String[] args) {
		Prediction.execute(new ThreadingML());
	}
}
