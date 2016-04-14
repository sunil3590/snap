package edu.ncsu.snap;

import java.util.*;

public class Node {
	
	public int nodeId;
	
	public List<Integer> circles;
	
	public List<Integer> features;
	
	public Map<Pair<Integer, Integer>, Map<Integer, Integer>> edFeatures;
	
	public Node(){
		circles = new ArrayList<Integer>();
		features = new ArrayList<Integer>();
		edFeatures = new HashMap<Pair<Integer, Integer>, Map<Integer, Integer>>();
	}
	
}
