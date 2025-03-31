public struct DataPoint
{
	public readonly double[] inputs;
	public readonly double[] expectedOutputs;
	public readonly int label;
	public readonly string filename;

	public DataPoint(double[] inputs, int label, int numLabels, string s = "")
	{
		this.inputs = inputs;
		this.label = label;
		expectedOutputs = CreateOneHot(label, numLabels);
		this.filename = s;
	}

	public static double[] CreateOneHot(int index, int num)
	{
		double[] oneHot = new double[num];
			oneHot[index] = 1;
		return oneHot;
	}
}
