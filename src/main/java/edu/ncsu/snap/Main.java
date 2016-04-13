package edu.ncsu.snap;

public class Main {
	
	public static void main(String[] args) {
		// TODO : create graph object using input file names
		Graph gd = new Graph();
		
		
		// TODO : create the node to be added
		Node node = new Node();
		
		// TODO : run SNAP algo to add a new node to the existing clusters
		Snap.addNode(gd, node);
		
		return;
	}
}