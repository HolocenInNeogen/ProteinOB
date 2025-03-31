package dev.holocene.protein.tool;

import java.util.Scanner;

public final class CalculateCorrelation {
	private CalculateCorrelation() {}

	public static void main(String[] args) {
		@SuppressWarnings("resource") var in = new Scanner(System.in);
		while (true) {
			System.out.println(in.nextLine());
			var m = new double[3][3];
			for (var i = 0; i < 3; i++)
				for (var j = 0; j < 3; j++)
					m[i][j] = in.nextInt();
			System.out.println("T " + mcc(m));
			System.out.println("C " + mcc(m, 0));
			System.out.println("H " + mcc(m, 1));
			System.out.println("S " + mcc(m, 2));
			in.nextLine();
		}
	}

	private static double mcc(double[][] m, int d) {
		var p = m[d][d];
		var n = 0.0;
		var u = 0.0;
		var o = 0.0;
		for (var i = 0; i < 3; i++)
			for (var j = 0; j < 3; j++)
				if (i != d && j != d)
					n += m[i][j];
				else if (i == d && j != d)
					o += m[i][j];
				else if (i != d && j == d)
					u += m[i][j];
		return (p * n - u * o) / Math.sqrt((p + u) * (p + o) * (n + u) * (n + o));
	}

	private static double mcc(double[][] m) {
		var t = new double[3];
		var p = new double[3];
		var c = 0.0;
		var s = 0.0;
		for (var i = 0; i < 3; i++)
			for (var j = 0; j < 3; j++) {
				s += m[i][j];
				if (i == j)
					c += m[i][j];
				t[j] += m[i][j];
				p[i] += m[i][j];
			}
		return (c * s - t[0] * p[0] - t[1] * p[1] - t[2] * p[2]) / Math.sqrt(s * s - p[0] * p[0] - p[1] * p[1] - p[2] * p[2]) / Math.sqrt(s * s - t[0] * t[0] - t[1] * t[1] - t[2] * t[2]);
	}
}
