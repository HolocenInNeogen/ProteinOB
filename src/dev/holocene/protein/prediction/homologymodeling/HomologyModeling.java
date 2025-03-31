package dev.holocene.protein.prediction.homologymodeling;

import dev.holocene.protein.prediction.common.Prediction;
import dev.holocene.protein.prediction.common.Predictor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class HomologyModeling implements Predictor {
	private HomologyModeling() {}

	@Override
	public String predict(String aminoacids) {
		var aminoacidsCount = models.get(aminoacids).aminoacidsCount;
		var bestModel = "";
		var bestScore = 0;
		for (var model : models.entrySet()) {
			if (!aminoacids.equals(model.getKey())) {
				var score = Prediction.scoreStrings(aminoacids, aminoacidsCount, model.getKey(), model.getValue().aminoacidsCount);
				if (bestModel.equals("") || score > bestScore) {
					bestModel = model.getValue().structure;
					bestScore = score;
				}
			}
		}
		var generatedLength = bestModel.length();
		var length = aminoacids.length();
		if (generatedLength > length)
			return bestModel.substring(0, length);
		return bestModel + "C".repeat(length - generatedLength);
	}

	@Override
	public Learner getLearner() {
		return (aminoacids, structures) -> {
			models.put(aminoacids, new Model(aminoacids, structures));
		};
	}

	private final Map<String, Model> models = new HashMap<>();

	private static class Model {
		public final String structure;
		public final Map<Character, Set<Integer>> aminoacidsCount = new HashMap<>();

		public Model(String aminoacids, String structure) {
			this.structure = structure;
			for (var i = 0; i < aminoacids.length(); i++) {
				var aminoacid = aminoacids.charAt(i);
				aminoacidsCount.putIfAbsent(aminoacid, new HashSet<>());
				aminoacidsCount.get(aminoacid).add(i);
			}
		}
	}

	public static void main(String[] args) {
		Prediction.execute(new HomologyModeling());
	}
}
