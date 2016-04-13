package edu.ncsu.snap;

import java.util.ArrayList;
import java.util.Set;

public class Snap {

	// TODO : configurable parameters
	static int gradientReps = 50;

	static void addNode(Graph gd, Node node) {
		// compute the theta and alpha values for the given graph and circles
		BigTheta bigTheta = train(gd);
		
		// TODO : add the node to gd based on log-likelihood
	}

	// co ordinate ascent to find theta and alpha
	private static BigTheta train(Graph gd) {

		// graph parameters
		int k = gd.k;
		int nNodeFeatures = gd.nNodeFeatures;
		int nEdgeFeatures = gd.nEdgeFeatures;

		// this is the output
		int nTheta = nEdgeFeatures;
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
	
	private static double logLikelihood(BigTheta bigTheta, ArrayList<Set<Integer>> chat, Graph gd) {
		
		double ll = 0.0;
		
		int k = chat.size();
		int n = gd.nNodes;
		// chat.get(i).contains(n)
		
		return ll;
	}
}
