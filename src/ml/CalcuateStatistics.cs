using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CalcuateStatistics : MonoBehaviour
{
	NetworkTrainer trainer;
	DataLoader loader;
	NeuralNetwork network;

    void Awake()
    {
        trainer = FindObjectOfType<NetworkTrainer>();
		loader = FindObjectOfType<DataLoader>();
		network = trainer.neuralNetwork;
    }

	public void Print() {
		Statistics a = trainer.GetStatistics(loader.a);
		Statistics b = trainer.GetStatistics(loader.b);
		Statistics m = trainer.GetStatistics(loader.m);
		Statistics t = a.Sum(b).Sum(m);
		string x = "ALPHA\n";
		x += a.Percent();
		x += "\n";
		x += a.ToString();
		x += "\n\nMIXED\n";
		x += m.Percent();
		x += "\n";
		x += m.ToString();
		x += "\n\nBETA\n";
		x += b.Percent();
		x += "\n";
		x += b.ToString();
		x += "\n\nTOTAL\n";
		x += t.Percent();
		x += "\n";
		x += t.ToString();
		Debug.Log(x);
	}
}

public class Statistics {
	public int[,] statistics = new int[3,3];

	public double Percent() {
		int total = 0;
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				total += statistics[i,j];
		return 100.0 * (statistics[0,0] + statistics[1,1] + statistics[2,2]) / total;
	}

	public Statistics Sum(Statistics x) {
		Statistics s = new Statistics();
		for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					s.statistics[i,j] = statistics[i,j] + x.statistics[i,j];
		return s;
	}

	public string ToString() {
		string s = "EXP ->\tH\tS\tC\n";
		s += "H\t";
		for (int i = 0; i < 3; i++) {
			s += statistics[0,i];
			s += "\t";
		}
		s += "\nS\t";
		for (int i = 0; i < 3; i++) {
			s += statistics[1,i];
			s += "\t";
		}
		s += "\nC\t";
		for (int i = 0; i < 3; i++) {
			s += statistics[2,i];
			s += "\t";
		}
		return s;
	}
}
