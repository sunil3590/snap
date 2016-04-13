package edu.ncsu.snap;

public class Snap {

	// TODO : configurable parameters
	static int gradientReps = 50;
	
	private static class BigTheta {

		double[] theta;
		double[] alpha;

		BigTheta(int nTheta, int k) {
			theta = new double[nTheta];
			alpha = new double[k];
		}
	}

	static void addNode(Graph gd, Node node) {
		// TODO : use train to add the node to gd
		train(gd);
	}

	// co ordinate ascent to find theta and alpha
	private static BigTheta train(Graph gd) {

		// TODO : to be taken from Graph
		int k = gd.k; //  TODO : based on gd
		int nNodeFeatures = gd.nNodeFeatures; // TODO based on gd
		int nEdgeFeatures = 1 + nNodeFeatures;

		int nTheta = nEdgeFeatures;
		// this is the output
		BigTheta bigTheta = new BigTheta(nTheta, k);

		double l1 = 0; // gain
		double[] dlda = new double[k]; // alpha parameter
		double[] dldt = new double[nTheta];

		// learning rate
		double increment = 1.0 / (1.0 * 1);

		for (int iteration = 0; iteration < gradientReps; iteration++) {
			for (int i = 0; i < nTheta; i++) {

			}
		}

		return bigTheta;
	}
}
