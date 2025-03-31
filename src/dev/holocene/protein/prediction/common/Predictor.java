package dev.holocene.protein.prediction.common;

@FunctionalInterface
public interface Predictor {
	@FunctionalInterface
	public interface Learner {
		void learn(String aminoacids, String structures);
	}

	String predict(String aminoacids);

	default void prepare() {}

	default Learner getLearner() {
		return null;
	}
}
