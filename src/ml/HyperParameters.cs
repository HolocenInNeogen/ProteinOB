[System.Serializable]
public class HyperParameters
{
	public int[] layerSizes;
	public Activation.ActivationType activationType;
	public Activation.ActivationType outputActivationType;
	public Cost.CostType costType;

	public double initialLearningRate;
	public double learnRateDecay;
	public int minibatchSize;
	public double momentum;
	public double regularization;

	public HyperParameters()
	{
		activationType = Activation.ActivationType.ReLU;
		outputActivationType = Activation.ActivationType.SiLU;
		costType = Cost.CostType.MeanSquareError;
		initialLearningRate = 0.04;
		learnRateDecay = 0.01;
		minibatchSize = 32;
		momentum = 0.9;
		regularization = 0.1;
	}
}
