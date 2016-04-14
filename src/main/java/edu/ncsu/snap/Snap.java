package edu.ncsu.snap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Snap {

	// TODO : configurable parameters
	static int gradientReps = 50;
	static int reps = 25;

	static void addNode(Graph gd, Node node) {
		// compute the theta and alpha values for the given graph and circles
		List<BigTheta> bigTheta = train(gd);

		// TODO : add the node to gd based on log-likelihood
	}

	// co ordinate ascent to find theta and alpha
	private static List<BigTheta> train(Graph gd) {

		// graph parameters
		int K = gd.clusters.size();
		int nEdgeFeatures = gd.nEdgeFeatures;

		// this is the output required
		ArrayList<Set<Integer>> chat = new ArrayList<Set<Integer>>();
		List<BigTheta> bigTheta = new ArrayList<BigTheta>();
		for (int i = 0; i < K; i++) {
			bigTheta.add(new BigTheta(nEdgeFeatures));
		}

		// log likelihood and its partial derivatives for gradiant ascent
		double ll_prev;
		double ll;
		double[] dlda = new double[K];
		double[][] dldt = new double[K][nEdgeFeatures];

		// learning rate
		double increment = 1.0 / (1.0 * gd.nNodes * gd.nNodes);
		if (gd.directed) {
			increment *= 0.5;
		}

		// repetitions
		for (int rep = 0; rep < reps; rep++) {
			
			// If it's the first repetition or the solution is degenerate,
			// randomly initialize the weights
			for (int k = 0; k < K; k++) {
				if (rep == 0 || chat.get(k).size() == 0 || (int) chat.get(k).size() == gd.nNodes) {
					
					Random rand = new Random();
					
					// initialize clusters
					chat.get(k).clear();
					for (int i = 0; i < gd.nNodes; i ++) {
						if (rand.nextInt(100) % 2 == 0) {
							chat.get(k).add(i);
						}
					}
					
					// initialize all theta to 0
					for (int f = 0; f < gd.nEdgeFeatures; f++) {
						bigTheta.get(k).theta[f] = 0;
					}
					// Just set a single feature to 1 as a random initialization.
					bigTheta.get(k).theta[rand.nextInt(gd.nEdgeFeatures)] = 1.0;
					bigTheta.get(k).theta[0] = 1;
					
					// initialize alpha
					bigTheta.get(k).alpha = 1;
				}
			}
			
			// TODO :  should we do this?
			// Update the latent variables (cluster assignments) in a random order.
			
			// loglikelihood before starting gradient ascent
			ll_prev = logLikelihood(bigTheta, chat, gd);

			// gradient ascent
			for (int iteration = 0; iteration < gradientReps; iteration++) {

				// TODO : compute dlda and dldt

				// update bigtheta using dlda and dldt
				for (int k = 0; k < K; k++) {
					for (int f = 0; f < nEdgeFeatures; f++) {
						bigTheta.get(k).theta[f] += increment * dldt[k][f];	
					}
				}
				for (int k = 0; k < K; k++) {
					bigTheta.get(k).alpha += increment * dlda[k];
				}
				
				// just to show something is happening
				System.out.print(".");

				// compute new log likelihood
				ll = logLikelihood(bigTheta, chat, gd);

				// If we reduced the objective, undo the update and stop.
				if (ll < ll_prev) {
					for (int k = 0; k < K; k++) {
						for (int f = 0; f < nEdgeFeatures; f++) {
							bigTheta.get(k).theta[f] -= increment * dldt[k][f];
						}
					}
					for (int k = 0; k < K; k++) {
						bigTheta.get(k).alpha -= increment * dlda[k];
					}
					ll = ll_prev;
					break;
				}
				
				// keep the new likelihood
				ll_prev = ll;
			}
			System.out.println("");
		}

		return bigTheta;
	}

	private static double logLikelihood(List<BigTheta> bigTheta, ArrayList<Set<Integer>> chat, Graph gd) {

		double ll = 0.0;
		int K = chat.size();

		for (Map.Entry<Pair<Integer, Integer>, Map<Integer, Integer>> entry : gd.edgeFeatures.entrySet()) {
			double inp_ = 0;

			Pair<Integer, Integer> e = entry.getKey();
			// TODO : what is this "val"? may be phi(e)?
			Map<Integer, Integer> val = entry.getValue();
			int e1 = e.getFirst();
			int e2 = e.getSecond();

			boolean exists = gd.edgeSet.contains(e) ? true : false;

			for (int k = 0; k < K; k++) {
				double d = chat.get(k).contains(e1) && chat.get(k).contains(e2) ? 1 : -bigTheta.get(k).alpha;
				inp_ += d * Util.inp(val, bigTheta.get(k).theta);
			}

			if (exists) {
				ll += inp_;
			}

			double ll_ = Math.log(1 + Math.exp(inp_));
			ll += -ll_;
		}

		return ll;
	}
	
	public static void main(String[] args) {
		
		// input data to be used for analysis
		String filePrefix = "./data/facebook/698"; //
		String nodeFeatureFile = filePrefix + ".feat"; // TODO
		String selfFeatureFile = filePrefix + ".egofeat";
		String clusterFile = filePrefix + ".circles";
		String edgeFile = filePrefix + ".edges";
		String which = "FRIENDFEATURES";
		boolean directed = false;
		
		// create graph object using input file names
		Graph gd = new Graph();
		boolean status = gd.loadGraphData(nodeFeatureFile, selfFeatureFile, 
				clusterFile, edgeFile, which, directed);
		if (status == false) {
			System.out.println("Could not build graph");
			return;
		}
		
		// TODO : create the node to be added
		Node node = new Node();
		
		// TODO : run SNAP algo to add a new node to the existing clusters
		Snap.addNode(gd, node);
		
		return;
	}
}
