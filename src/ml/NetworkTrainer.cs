using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class NetworkTrainer : MonoBehaviour
{
	[Range(0, 1)] public float trainingSplit = 0.75f;

	public HyperParameters hyperParameters;

	[Space()]
	public TrainingSessionInfo sessionInfo;

	DataLoader loader;
	DataPoint[] allData;
	DataPoint[] trainingData;
	DataPoint[] validationData;
	Batch[] trainingBatches;

	public NeuralNetwork neuralNetwork { get; private set; }
	bool trainingActive;
	int batchIndex;
	double currentLearnRate;
	bool hasLoaded;
	int epochCount;
	CalcuateStatistics printer;

	void Awake()
    {
        printer = FindObjectOfType<CalcuateStatistics>();
    }

	void Start()
	{
		StartTrainingSession();
	}

	void Update()
	{
		if (trainingActive)
		{
			Run();
		}
	}

	public void StartTrainingSession()
	{
		if (!hasLoaded)
		{
			LoadData();
		}

		neuralNetwork = new NeuralNetwork(hyperParameters.layerSizes);
		var activation = Activation.GetActivationFromType(hyperParameters.activationType);
		var outputLayerActivation = Activation.GetActivationFromType(hyperParameters.outputActivationType);
		neuralNetwork.SetActivationFunction(activation, outputLayerActivation);
		neuralNetwork.SetCostFunction(Cost.GetCostFromType(hyperParameters.costType));

		sessionInfo = new TrainingSessionInfo(trainingBatches.Length);
		sessionInfo.StartTimer();

		currentLearnRate = hyperParameters.initialLearningRate;
		batchIndex = 0;
		epochCount = 0;
		trainingActive = true;
	}

	void LoadData()
	{
		loader = FindObjectOfType<DataLoader>();
		allData = loader.GetAllData();
		(trainingData, validationData) = DataSetHelper.SplitData(allData, trainingSplit);
		trainingBatches = DataSetHelper.CreateMiniBatches(trainingData, hyperParameters.minibatchSize);
		hasLoaded = true;
	}

	void Run()
	{
		var sw = System.Diagnostics.Stopwatch.StartNew();

		while (sw.ElapsedMilliseconds < 16)
		{
			neuralNetwork.Learn(trainingBatches[batchIndex].data, currentLearnRate, hyperParameters.regularization, hyperParameters.momentum);
			sessionInfo.BatchCompleted();
			batchIndex++;

			if (batchIndex >= trainingBatches.Length)
			{
				EpochCompleted();
			}
		}

		UpdateSessionInfo();
	}

	void EpochCompleted()
	{
		printer.Print();
		batchIndex = 0;
		epochCount++;
		DataSetHelper.ShuffleBatches(trainingBatches);
		currentLearnRate = (1.0 / (1.0 + hyperParameters.learnRateDecay * epochCount)) * hyperParameters.initialLearningRate;
	}

	void UpdateSessionInfo()
	{
		sessionInfo.currentLearnRate = currentLearnRate;
	}

	public void Save()
	{
		string path = System.IO.Path.Combine(System.IO.Directory.GetCurrentDirectory(), "Assets", "network.json");
		NetworkSaveData.SaveToFile(neuralNetwork, path);
		Debug.Log("Saved network to: " + path);
	}

	public void Save(string path, string fileName)
	{
		string fullPath = System.IO.Path.Combine(path, fileName + ".json");
		NetworkSaveData.SaveToFile(neuralNetwork, fullPath);
	}

	public Statistics GetStatistics(DataPoint[] data)
	{
		Statistics m = new Statistics();
		System.Threading.Tasks.Parallel.ForEach(data, (data) =>
			{
				int predicted = neuralNetwork.MaxValueIndex(neuralNetwork.CalculateOutputs(data.inputs));
				lock (m)
				{
					m.statistics[predicted,data.label]++;
				}
			});
		return m;
	}
}
