package edu.ncsu.snap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

//Building the graph object to contain the ego nodes and their respective ego network
public class Graph {

	int nNodes;
	Map<String, Integer> nodeIndex; //Holds the nodeID to internal nodeID mapping
	Map<Integer, String> indexNode;  //Holds the reverse nodeID to internal nodeID mapping
	Set<Pair<Integer, Integer>> edgeSet;  //Holds the edges information of the nodes in the ego network
	int nNodeFeatures;   //Holds the number of features to represent a node. Varies for different ego node networks
	int nEdgeFeatures;   //Holds the corresponding number of edge features information in the given ego network
	Map<Pair<Integer, Integer>, Map<Integer, Integer>> edgeFeatures;  //Holds the similarity metric feature set based on nodes/edges between the different nodes in an ego network
	List<Set<Integer>> clusters;  //Holds the social circles membership information of all the nodes in an ego network
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

			this.directed = directed;
			Util util = new Util();

			Map<Integer, List<Integer>> nodeFeatures = new HashMap<Integer, List<Integer>>();  //holds the node features of the nodes in the ego network
			Map<Integer, List<Integer>> simFeatures = new HashMap<Integer, List<Integer>>();  //holds the similarity metrics between the ego node and the different nodes in the ego network

			List<Integer> selfFeatures;   //holds the node features of the ego node

			File f = null;
			//Fetching the node features for all the nodes in the ego network
			File f2 = new File(nodeFeatureFile);
			if (!f2.exists()) {
				System.out.println("Couldn't open " + nodeFeatureFile);
				return false;
			}

			int i = 0;
			//Loading the node features into a graph data structure
			Scanner sc = new Scanner(f2);

			while (sc.hasNextLine()) {
				String[] arr = sc.nextLine().split(" ");

				String nodeid = arr[0];

				if (nodeIndex.containsKey(nodeid)) {
					System.out.println("Got duplicate feature for " + nodeid);
				}
				//Remapping the nodeID to an incremental temporary nodeID for easier knowledge representation purpose
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
			//Creating an upper treshold for our system. We cap the ego nodes, with greater than 1000 children nodes in their ego network
			if (nNodes > 1200) {
				System.out.println("This code will probably run out of memory with more than 1000 nodes");
				sc.close();
				return false;
			}

			//Fetching the feature set for the ego node
			f = new File(selfFeatureFile);
			selfFeatures = new ArrayList<Integer>(); //Contains the features of the ego node
			sc.close();
			sc = new Scanner(f);

			for (int k = 0; k < nNodeFeatures; k++) {
				selfFeatures.add(sc.nextInt());
			}
			//computing the similarity metrics between the egonode feature set and the nodes in the network of the ego node
			for (int k = 0; k < nNodes; k++) {
				List<Integer> feature = util.diff(selfFeatures, nodeFeatures.get(k), nNodeFeatures);
				simFeatures.put(k, feature);
			}
			
			//Fetching the social circles and the node memberships in different social circles
			f = new File(clusterFile);

			sc.close();

			sc = new Scanner(f);

			while (sc.hasNextLine()) {

				String[] arr = sc.nextLine().split("\t");
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
			//Loading the similarity metrics based on node/edge fetaures between the node members in the ego network
			for (int m = 0; m < nNodes; m++) {
				for (int n = (directed ? 0 : m + 1); n < nNodes; n++) {
					if (m == n)
						continue;
					int ind = 0;
					int[] d = new int[nEdgeFeatures];

					for (int o = 0; o < d.length; o++) {
						d[o] = -1;
					}

					d[0] = 1;
					ind++;

					List<Integer> list;
					List<Integer> list2 = new ArrayList<Integer>();

					if (which.equals("EGOFEATURES")) {
						list = util.diff(simFeatures.get(m), simFeatures.get(n), nNodeFeatures);

					} else if (which.equals("FRIENDFEATURES")) {
						//Our system uses the similarity metric based on the node feature set
						list = util.diff(nodeFeatures.get(m), nodeFeatures.get(n), nNodeFeatures);
					} else {
						list = util.diff(simFeatures.get(m), simFeatures.get(n), nNodeFeatures);
						list2 = util.diff(nodeFeatures.get(m), nodeFeatures.get(n), nNodeFeatures);
					}

					for (int val : list) {
						d[ind] = val;
						ind++;
					}

					ind += nNodeFeatures;

					for (int val : list2) {
						d[ind] = val;
					}

					edgeFeatures.put(new Pair<Integer, Integer>(m, n), util.makeSparse(d, nEdgeFeatures));
				}
			}

			sc.close();
			//Loads the edge information for the given ego network
			f = new File(edgeFile);

			sc = new Scanner(f);

			while (sc.hasNextLine()) {
				String src = sc.next();
				String dest = sc.next();
				sc.nextLine();
				edgeSet.add(new Pair<Integer, Integer>(nodeIndex.get(src), nodeIndex.get(dest)));
			}

			sc.close();
			return true;

		} catch (Exception ex) {
			//display any exceptions
			System.out.println(ex.getMessage());
			return false;
		}

	}

	public Node removeNode(int nodeid) {
		//To facilitate the removal of a node from the ego network
		Node node = new Node();

		node.nodeId = nodeid;

		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).contains(nodeid)) {
				clusters.get(i).remove(nodeid);
				node.circles.add(i);
			}
		}

		Iterator<Pair<Integer, Integer>> it = edgeSet.iterator();

		while (it.hasNext()) {
			Pair<Integer, Integer> pair = it.next();
			if (pair.getFirst() == nodeid || pair.getSecond() == nodeid) {
				node.edges.add(pair);
				it.remove();
			}
		}

		/*
		 * for(Pair<Integer, Integer> pair : edgeSet){ if(pair.getFirst() ==
		 * nodeid || pair.getSecond() == nodeid) edgeSet.remove(pair); }
		 */

		Iterator<Map.Entry<Pair<Integer, Integer>, Map<Integer, Integer>>> it2 = edgeFeatures.entrySet().iterator();

		while (it2.hasNext()) {
			Map.Entry<Pair<Integer, Integer>, Map<Integer, Integer>> item = (Map.Entry<Pair<Integer, Integer>, Map<Integer, Integer>>) it2
					.next();
			if (item.getKey().getFirst() == nodeid || item.getKey().getSecond() == nodeid) {
				node.edFeatures.put(item.getKey(), edgeFeatures.get(item.getKey()));
				it2.remove();
			}
		}

		/*
		 * for(Pair<Integer, Integer> pair : edgeFeatures.keySet()){
		 * if(pair.getFirst() == nodeid || pair.getSecond() == nodeid){
		 * node.edFeatures.put(pair, edgeFeatures.get(pair));
		 * edgeFeatures.remove(pair); } else { for(int key :
		 * edgeFeatures.get(pair).keySet()){ if(key == nodeid)
		 * edgeFeatures.get(pair).remove(key); } } }
		 */

		nNodes--;

		return node;
	}

	// circle info of node is not added here
	public void addNode(Node node) {
	// To facilitate the addition of a node into the ego network
		for (int cId : node.circles) {
			clusters.get(cId).add(node.nodeId);
		}
		
		for (Pair<Integer, Integer> pair : node.edges) {
			edgeSet.add(pair);
		}

		for (Pair<Integer, Integer> pair : node.edFeatures.keySet()) {
			edgeFeatures.put(pair, node.edFeatures.get(pair));
		}

		nNodes++;
	}

	public static void main(String[] args) {
		//A simple unit testing block.
		try {
			Graph graph = new Graph();

			String filePrefix = "./data/facebook/698";
			String nodeFeatureFile = filePrefix + ".feat";
			String selfFeatureFile = filePrefix + ".egofeat";
			String clusterFile = filePrefix + ".circles";
			String edgeFile = filePrefix + ".edges";
			String which = "FRIENDFEATURES";
			boolean directed = false;

			boolean status = graph.loadGraphData(nodeFeatureFile, selfFeatureFile, clusterFile, edgeFile, which,
					directed);

			Node node = graph.removeNode(0);

			graph.addNode(node);

			if (status == false) {
				System.out.println("Could not build graph");
				return;
			}
		} catch (Exception ex) {

		}
	}

}
