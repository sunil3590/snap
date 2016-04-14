package edu.ncsu.snap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Snap {

	// TODO : configurable parameters
	static int gradientReps = 50;

	static void addNode(Graph gd, Node node) {
		// compute the theta and alpha values for the given graph and circles
		List<BigTheta> bigTheta = train(gd);

		// TODO : add the node to gd based on log-likelihood
	}

	// co ordinate ascent to find theta and alpha
	private static List<BigTheta> train(Graph gd) {

		// graph parameters
		int K = gd.clusters.size();
		int nNodeFeatures = gd.nNodeFeatures;
		int nEdgeFeatures = gd.nEdgeFeatures;

		// this is the output
		int nTheta = nEdgeFeatures;
		List<BigTheta> bigTheta = new ArrayList<BigTheta>();
		for (int i = 0; i < K; i++) {
			bigTheta.add(new BigTheta(nTheta));
		}

		double l1 = 0; // gain
		double[] dlda = new double[K]; // alpha parameter
		double[] dldt = new double[nTheta];

		// learning rate
		double increment = 1.0 / (1.0 * 1);

		// TODO : gradient ascent
		for (int iteration = 0; iteration < gradientReps; iteration++) {
			for (int i = 0; i < nTheta; i++) {

			}
		}

		return bigTheta;
	}

	private static double logLikelihood(List<BigTheta> bigTheta, ArrayList<Set<Integer>> chat, Graph gd) {
		
		double ll = 0.0;
		int K = chat.size();
		
		for (Map.Entry<Pair<Integer,Integer>,Map<Integer,Integer>> entry : gd.edgeFeatures.entrySet()) {
		    double inp_ = 0;
		    
		    Pair<Integer,Integer> e = entry.getKey();
		    Map<Integer, Integer> val = entry.getValue(); // TODO : what is this value?  may be phi(e)?
		    int e1 = e.getFirst();
		    int e2 = e.getSecond();
		    
		    boolean exists = gd.edgeSet.contains(e) ? true : false;
		    
		    for (int k = 0; k < K; k++)
		    {
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
}
