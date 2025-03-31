package dev.holocene.protein.prediction.statisticalimproved;

import dev.holocene.protein.prediction.common.Prediction;
import dev.holocene.protein.prediction.common.Predictor;
import java.util.HashMap;
import java.util.Map;

public final class StatisticalImproved implements Predictor {
	private StatisticalImproved() {}

	@Override
	public String predict(String aminoacids) {
		var structure = new StringBuilder();
		for (var i = 0; i < aminoacids.length(); i++) {
			var aminoacid = aminoacids.charAt(i);
			structure.append(freq.get(aminoacid).top());
		}
		var generated = structure.toString();
		var result = new StringBuilder();
		for (var i = 0; i < aminoacids.length(); i++) {
			var b2 = charAt(generated, i - 2);
			var b1 = charAt(generated, i - 1);
			var a1 = charAt(generated, i + 1);
			var a2 = charAt(generated, i + 2);
			var c = generated.charAt(i);
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
		return result.toString();
	}

	@Override
	public Learner getLearner() {
		return (aminoacids, structures) -> {
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

	private char charAt(String s, int i) {
		if (i >= s.length() || i < 0)
			return ' ';
		return s.charAt(i);
	}

	public static void main(String[] args) {
		Prediction.execute(new StatisticalImproved());
	}
}
