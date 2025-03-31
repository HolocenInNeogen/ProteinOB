using UnityEngine;
using System.Collections.Generic;

public class DataLoader : MonoBehaviour
{
	[SerializeField] TextAsset input;
	[SerializeField] TextAsset output;
	[SerializeField] TextAsset aIn;
	[SerializeField] TextAsset aOut;
	[SerializeField] TextAsset bIn;
	[SerializeField] TextAsset bOut;
	[SerializeField] TextAsset mIn;
	[SerializeField] TextAsset mOut;
	[SerializeField] public string[] labelNames;
	public DataPoint[] data;
	public DataPoint[] a;
	public DataPoint[] b;
	public DataPoint[] m;

	public string[] LabelNames => labelNames;

	void Awake()
	{
		data = LoadData(input, output);
		a = LoadData(aIn, aOut);
		b = LoadData(bIn, bOut);
		m = LoadData(mIn, mOut);
	}

	public DataPoint[] GetAllData()
	{
		return data;
	}

	DataPoint[] LoadData(TextAsset input, TextAsset output)
	{
		byte[] rawInput = input.bytes;
		byte[] rawOutput = output.bytes;
		int inputSize = rawInput.Length;
		int nPoints = rawOutput.Length;
		var points = new DataPoint[nPoints];
		System.Threading.Tasks.Parallel.For(0, nPoints, (i) =>
		{
			double[] input = new double[63];
			System.Threading.Tasks.Parallel.For(13, 76, (j) =>
			{
				input[j - 13] = rawInput[i * 76 + j] / 22.0;
			});
			string s = System.Text.Encoding.UTF8.GetString(rawInput, i * 76, 13);
			points[i] = new DataPoint(input, rawOutput[i], 3, s);
		});
		return points;
	}
}
