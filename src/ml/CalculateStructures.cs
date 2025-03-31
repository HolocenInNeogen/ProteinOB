using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CalculateStructures : MonoBehaviour
{
	DataLoader loader;
	NeuralNetwork network;

    void Start()
    {
		string path = System.IO.Path.Combine(System.IO.Directory.GetCurrentDirectory(), "Assets", "network.json");
		network = NetworkSaveData.LoadNetworkFromFile(path);
		loader = FindObjectOfType<DataLoader>();
		DoFor(loader.a, "alpha");
		DoFor(loader.b, "beta");
		DoFor(loader.m, "mixed");
    }

	void DoFor(DataPoint[] x, string q) {
		foreach (var data in x) {
			int predicted = network.MaxValueIndex(network.CalculateOutputs(data.inputs));
			string path = System.IO.Path.Combine(System.IO.Directory.GetCurrentDirectory(), "Assets", q, data.filename);
			using (var writer = System.IO.File.AppendText(path)) {
				writer.Write(loader.labelNames[predicted]);
			}
		}
	}
}
