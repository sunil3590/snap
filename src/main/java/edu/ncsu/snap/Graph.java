package edu.ncsu.snap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Graph {

	int nNodes;
	Map<String, Integer> nodeIndex;
	Map<Integer, String> indexNode;
	Set<Pair<Integer, Integer>> edgeSet;
	int nNodeFeatures;
	int nEdgeFeatures;
	Map<Pair<Integer, Integer>, Map<Integer, Integer>> edgeFeatures;
	List<Set<Integer>> clusters;
	boolean directed;

	public Graph() {

		nodeIndex = new HashMap<String, Integer>();
		indexNode = new HashMap<Integer, String>();
		edgeSet = new HashSet<Pair<Integer, Integer>>();
		edgeFeatures = new HashMap<Pair<Integer, Integer>, Map<Integer, Integer>>();
		clusters = new ArrayList<Set<Integer>>();

	}

	public boolean loadGraphData(String nodeFeatureFile, String selfFeatureFile, String clusterFile, String edgeFile,
			String which, boolean directed) {
		try {

			Util util = new Util();

			Map<Integer, List<Integer>> nodeFeatures = new HashMap<Integer, List<Integer>>();
			Map<Integer, List<Integer>> simFeatures = new HashMap<Integer, List<Integer>>();

			List<Integer> selfFeatures;

			File f = null;

			File f2 = new File(nodeFeatureFile);
			if (!f2.exists()) {
				System.out.println("Couldn't open " + nodeFeatureFile);
				return false;
			}

			int i = 0;

			Scanner sc = new Scanner(f2);

			while (sc.hasNextLine()) {
				String[] arr = sc.nextLine().split(" ");

				String nodeid = arr[0];

				if (nodeIndex.containsKey(nodeid)) {
					System.out.println("Got duplicate feature for " + nodeid);
				}

				nodeIndex.put(nodeid, i);
				indexNode.put(i, nodeid);
				nNodeFeatures = arr.length - 1;
				List<Integer> features = new ArrayList<Integer>();

				for (int j = 1; j < arr.length; j++) {
					features.add(Integer.parseInt(arr[j]));
				}
				nodeFeatures.put(i, features);
				i++;
			}

			nNodes = i;

			if (nNodes > 1200) {
				System.out.println("This code will probably run out of memory with more than 1000 nodes");
				sc.close();
				return false;
			}

			f = new File(selfFeatureFile);
			selfFeatures = new ArrayList<Integer>();
			sc.close();
			sc = new Scanner(f);

			for (int k = 0; k < nNodeFeatures; k++) {
				selfFeatures.add(sc.nextInt());
			}

			for (int k = 0; k < nNodes; k++) {
				List<Integer> feature = util.diff(selfFeatures, nodeFeatures.get(i), nNodeFeatures);
				simFeatures.put(k, feature);
			}

			f = new File(clusterFile);

			sc.close();

			sc = new Scanner(f);

			while (sc.hasNextLine()) {

				String[] arr = sc.nextLine().split(" ");
				Set<Integer> circle = new HashSet<Integer>();

				for (int k = 1; k < arr.length; k++) {
					String nodeId = arr[k];

					if (nodeIndex.containsKey(nodeId)) {
						circle.add(nodeIndex.get(nodeId));
					} else {
						System.out.println("Got unknown entry in label file: " + nodeId);
					}
				}
				clusters.add(circle);
			}

			nEdgeFeatures = nNodeFeatures + 1;

			if (which.equals("BOTH")) {
				nEdgeFeatures += nNodeFeatures;
			}

			for (int m = 0; m < nNodes; m++) {
				for (int n = (directed ? 0 : m + 1); n < nNodes; n++) {
					if (m == n)
						continue;
					int[] d = new int[nEdgeFeatures];
					int l = 0;
					List<List<Integer>> list = new ArrayList<List<Integer>>();
					d[0] = l;
					List<Integer> first = new ArrayList<Integer>();
					first.add(1);
					list.add(first);
					int k = 0;

					if (which.equals("EGOFEATURES")) {
						list.add(util.diff(simFeatures.get(m), simFeatures.get(n), nNodeFeatures));
						l++;
						k++;
						d[k] = l;
					} else if (which.equals("FRIENDFEATURES")) {
						util.diff(nodeFeatures.get(m), nodeFeatures.get(n), nNodeFeatures);
						l++;
						k++;
						d[k] = l;
					} else {
						list.add(util.diff(simFeatures.get(m), simFeatures.get(n), nNodeFeatures));
						l++;
						k++;
						d[k] = l;
						util.diff(nodeFeatures.get(m), nodeFeatures.get(n), nNodeFeatures);
						l++;
						k++;
						d[k + nNodeFeatures] = l;
					}

					edgeFeatures.put(new Pair(m, n), util.makeSparse(list, d, nEdgeFeatures));
				}
			}

			sc.close();

		} catch (Exception ex) {
			System.out.println(ex);
			return false;
		}
		
		return true;
	}

	public static void main(String[] args) {
		
		// input data to be used for analysis
		String filePrefix = "./data/facebook/698";
		String nodeFeatureFile = filePrefix + ".feat";
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
	}
}
