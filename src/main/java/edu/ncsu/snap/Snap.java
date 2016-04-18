package edu.ncsu.snap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Snap {

	static int gradientReps = 50;
	static int reps = 1; // TODO : do we need the outer rep loop?
	static double lambda = 1.0;

	static List<Integer> whichCircle(Graph gd, Node node) {

		// compute the theta and alpha values for the given graph and circles
		List<BigTheta> bigThetas = train(gd);

		// create a copy of original circles which can be modified later
		List<Set<Integer>> newChat = new ArrayList<Set<Integer>>();
		for (Set<Integer> circle : gd.clusters) {
			Set<Integer> newCircle = new HashSet<Integer>();
			for (Integer n : circle) {
				newCircle.add(n);
			}
			newChat.add(newCircle);
		}

		// add new node to the graph temporarily
		// this node does not have cluster info
		gd.addNode(node);

		// the store the best prediction and its log likelihood
		List<Integer> predCID = new ArrayList<Integer>();
		double max_ll = Double.NEGATIVE_INFINITY;

		// the node can belong or not belong to any of the existing circles
		// try all possibilities
		for (int i = 0; i < Math.pow(2, gd.clusters.size()); i++) {
			List<Integer> curPredCID = new ArrayList<Integer>();
			// add node ID to all clusters represented by i
			for (int k = 0; k < gd.clusters.size(); k++) {
				int belongs = (i >> k) & 1;
				if (belongs == 1) {
					newChat.get(k).add(node.nodeId);
					curPredCID.add(k);
				}
			}

			// compute log likelihood of this circle assignment
			double ll = Snap.logLikelihood(bigThetas, newChat, gd.edgeSet, gd.edgeFeatures);
			if (ll > max_ll) {
				predCID = new ArrayList<Integer>(curPredCID);
				max_ll = ll;
			}

			// remove the node from clusters before next try
			for (int k : curPredCID) {
				newChat.get(k).remove(node.nodeId);
			}
		}

		// remove the new node that was added temporarily
		gd.removeNode(node.nodeId);

		return predCID;
	}

	// co ordinate ascent to find theta and alpha on original graph
	private static List<BigTheta> train(Graph gd) {

		// graph parameters
		int K = gd.clusters.size();
		int nEdgeFeatures = gd.nEdgeFeatures;
		List<Set<Integer>> chat = gd.clusters;

		// this is the output required
		List<BigTheta> bigThetas = new ArrayList<BigTheta>();
		for (int i = 0; i < K; i++) {
			bigThetas.add(new BigTheta(nEdgeFeatures));
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
		// TODO : since we randomly initialize bigtheta, we might want to consider
		// running gradient ascent more than once and keeping the best bigtheta
		// for that we must keep a copy of bigtheta and we need a clone()
		for (int rep = 0; rep < reps; rep++) {

			// randomly initialize the weights
			Random rand = new Random();
			for (int k = 0; k < K; k++) {
				// initialize all theta to 0
				for (int f = 0; f < gd.nEdgeFeatures; f++) {
					bigThetas.get(k).theta[f] = 0;
				}
				// Just set a single feature to 1 as a random initialization.
				bigThetas.get(k).theta[rand.nextInt(gd.nEdgeFeatures)] = 1.0;
				// this is the random reason for the circle creation
				// not any of the node feature
				bigThetas.get(k).theta[0] = 1;

				// initialize alpha
				bigThetas.get(k).alpha = 1;
			}
			
			// log likelihood before starting gradient ascent
			ll_prev = logLikelihood(bigThetas, chat, gd.edgeSet, gd.edgeFeatures);

			// gradient ascent
			for (int iteration = 0; iteration < gradientReps; iteration++) {

				// TODO : full gd?
				dl(dldt, dlda, K, lambda, gd, bigThetas);

				// update bigtheta using dlda and dldt
				for (int k = 0; k < K; k++) {
					for (int f = 0; f < nEdgeFeatures; f++) {
						bigThetas.get(k).theta[f] += increment * dldt[k][f];
					}
				}
				for (int k = 0; k < K; k++) {
					bigThetas.get(k).alpha += increment * dlda[k];
				}

				// compute new log likelihood
				ll = logLikelihood(bigThetas, chat, gd.edgeSet, gd.edgeFeatures);

				// If we reduced the objective, undo the update and stop.
				if (ll < ll_prev) {
					for (int k = 0; k < K; k++) {
						for (int f = 0; f < nEdgeFeatures; f++) {
							bigThetas.get(k).theta[f] -= increment * dldt[k][f];
						}
					}
					for (int k = 0; k < K; k++) {
						bigThetas.get(k).alpha -= increment * dlda[k];
					}
					ll = ll_prev;
					break;
				}

				// keep the new likelihood
				ll_prev = ll;
			}
		}

		return bigThetas;
	}

	private static void dl(double[][] dldt, double[] dlda, int K, double lambda, Graph gd, List<BigTheta> bigThetas) {

		for (int k = 0; k < K; k++) {
			for (int f = 0; f < gd.nEdgeFeatures; f++) {
				dldt[k][f] = -lambda * Math.signum(bigThetas.get(k).theta[f]);
			}
		}

		for (int k = 0; k < K; k++) {
			dlda[k] = 0;
		}

		List<Set<Integer>> chat = gd.clusters;
		double inps[] = new double[K];

		for (Map.Entry<Pair<Integer, Integer>, Map<Integer, Integer>> efm : gd.edgeFeatures.entrySet()) {
			double inp_ = 0;
			Pair<Integer, Integer> e = efm.getKey();
			int n1 = e.getFirst();
			int n2 = e.getSecond();
			boolean exists = gd.edgeSet.contains(e) ? true : false;
			for (int k = 0; k < K; k++) {
				inps[k] = Util.inp(efm.getValue(), bigThetas.get(k).theta);
				double d = chat.get(k).contains(n1) && chat.get(k).contains(n2) ? 1.0 : -bigThetas.get(k).alpha;
				inp_ += d * inps[k];
			}

			double expinp = Math.exp(inp_);
			double q = expinp / (1 + expinp);
			if (Double.isNaN(q)) {
				q = 1.0; // Avoids nan in the case of overflow.
			}

			for (int k = 0; k < K; k++) {
				boolean d_ = chat.get(k).contains(n1) && chat.get(k).contains(n2);
				double d = d_ ? 1.0 : -bigThetas.get(k).alpha;

				for (Map.Entry<Integer, Integer> ef : efm.getValue().entrySet()) {
					int i = ef.getKey();
					int f = ef.getValue();
					if (exists) {
						dldt[k][i] += d * f;
					}
					dldt[k][i] += -d * f * q;
				}

				if (!d_) {
					if (exists) {
						dlda[k] += -inps[k];
					}
					dlda[k] += inps[k] * q;
				}
			}
		}
	}

	private static double logLikelihood(List<BigTheta> bigThetas, List<Set<Integer>> chat,
			Set<Pair<Integer, Integer>> edgeSet, Map<Pair<Integer, Integer>, Map<Integer, Integer>> edgeFeatures) {

		double ll = 0.0;
		int K = chat.size();

		for (Map.Entry<Pair<Integer, Integer>, Map<Integer, Integer>> entry : edgeFeatures.entrySet()) {
			double inp_ = 0;

			Pair<Integer, Integer> e = entry.getKey();
			Map<Integer, Integer> val = entry.getValue(); // phi(e)
			int e1 = e.getFirst();
			int e2 = e.getSecond();

			boolean exists = edgeSet.contains(e) ? true : false;

			for (int k = 0; k < K; k++) {
				double d = chat.get(k).contains(e1) && chat.get(k).contains(e2) ? 1 : -bigThetas.get(k).alpha;
				inp_ += d * Util.inp(val, bigThetas.get(k).theta);
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
		
		// input arguments
		if (args.length != 1) {
			System.out.println("Ego node ID not passed");
			return;
		}

		// print start time
		System.out.println("Started at " + new Date().toString());
		System.out.println("---------------------------");
		
		// input data to be used for analysis
		String filePrefix = "./data/facebook/" + args[0];
		String nodeFeatureFile = filePrefix + ".feat";
		String selfFeatureFile = filePrefix + ".egofeat";
		String clusterFile = filePrefix + ".circles";
		String edgeFile = filePrefix + ".edges";
		String which = "FRIENDFEATURES";
		boolean directed = false;

		// create graph object using input file names
		Graph gd = new Graph();
		boolean status = gd.loadGraphData(nodeFeatureFile, selfFeatureFile, clusterFile, edgeFile, which, directed);
		if (status == false) {
			System.out.println("Could not build graph");
			return;
		}

		int tp = 0;
		int tn = 0;
		int fp = 0;
		int fn = 0;
		for (int i = 0; i < gd.nNodes; i++) {
			// remove a node and its cluster info from the full graph
			System.out.println("Predicting circles for node " + gd.indexNode.get(i));
			Node node = gd.removeNode(i);
			List<Integer> trueCircles = node.circles;

			// eliminate the circle info from the node
			node.circles = new ArrayList<Integer>();

			// predict which circle the node belongs to
			List<Integer> predCircles = Snap.whichCircle(gd, node);

			// evaluate the prediction
			for (int c = 0; c < gd.clusters.size(); c++) {
				boolean isTrueCircle = trueCircles.contains(c);
				boolean isPredCircle = predCircles.contains(c);
				if (isTrueCircle == true) {
					if (isPredCircle == true) {
						tp++;
					} else {
						fn++;
					}
					
				} else {
					if (isPredCircle == true) {
						fp++;
					} else {
						tn++;
					}					
				}
			}

			// add the node and its cluster info back
			node.circles = trueCircles;
			gd.addNode(node);

			// print some metrics
			float precision = (float) tp / (tp + fp);
			float recall = (float) tp / (tp + fn);
			float accuracy = (float) (tp + tn) / (tp + fp + tn + fn);
			float fMeasure = 2 * precision * recall / (precision + recall);
			System.out.println("True Circles " + trueCircles.toString());
			System.out.println("Predicted Circles " + predCircles.toString());
			System.out.println("Precision  = " + precision);
			System.out.println("Recall     = " + recall);
			System.out.println("Accuracy   = " + accuracy);
			System.out.println("F-Measure  = " + fMeasure);
			System.out.println("% complete = " + (float) (i + 1) / gd.nNodes * 100 + "%");
			System.out.println("---------------------------");
		}
		
		// print end time
		System.out.println("Ended at " + new Date().toString());

		return;
	}
}
