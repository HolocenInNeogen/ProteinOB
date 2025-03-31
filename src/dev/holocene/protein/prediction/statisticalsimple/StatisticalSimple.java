package dev.holocene.protein.prediction.statisticalsimple;

import dev.holocene.protein.prediction.common.Prediction;
import dev.holocene.protein.prediction.common.Predictor;
import java.util.HashMap;
import java.util.Map;

public final class StatisticalSimple implements Predictor {
	private StatisticalSimple() {}

	@Override
	public String predict(String aminoacids) {
		var structure = new StringBuilder();
		for (var i = 0; i < aminoacids.length(); i++) {
			var aminoacid = aminoacids.charAt(i);
			structure.append(frequencies.get(aminoacid).top());
		}
		return structure.toString();
	}

	@Override
	public Learner getLearner() {
		return (aminoacids, structures) -> {
			for (var i = 0; i < aminoacids.length(); i++) {
				var aminoacid = aminoacids.charAt(i);
				var structure = structures.charAt(i);
				frequencies.putIfAbsent(aminoacid, new AminoacidData());
				if (structure == 'H')
					frequencies.get(aminoacid).helix++;
				else if (structure == 'S')
					frequencies.get(aminoacid).strand++;
				else
					frequencies.get(aminoacid).coil++;
			}
		};
	}

	private final Map<Character, AminoacidData> frequencies = new HashMap<>();

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

	public static void main(String[] args) {
		Prediction.execute(new StatisticalSimple());
	}
}
