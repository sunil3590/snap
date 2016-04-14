package edu.ncsu.snap;

public class Main {
	
	public static void main(String[] args) {
		
		// input data to be used for analysis
		String filePrefix = "3980";
		String nodeFeatureFile = filePrefix + ".feat";
		String selfFeatureFile = filePrefix + ".egofeat";
		String clusterFile = filePrefix + ".circles";
		String edgeFile = filePrefix + ".edges";
		String which = "FRIENDFEATURES";
		boolean directed = false;
		
		// create graph object using input file names
		Graph gd = new Graph();
		gd.loadGraphData(nodeFeatureFile, selfFeatureFile, clusterFile, edgeFile, which, directed);
		
		// TODO : create the node to be added
		Node node = new Node();
		
		// TODO : run SNAP algo to add a new node to the existing clusters
		Snap.addNode(gd, node);
		
		return;
	}
}