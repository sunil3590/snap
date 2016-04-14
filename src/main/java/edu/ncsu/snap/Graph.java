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

public class Graph implements Cloneable {

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
	
	protected Object clone() throws CloneNotSupportedException{
		Graph cloned = (Graph)super.clone();
		return cloned;
	}

	public boolean loadGraphData(String nodeFeatureFile, String selfFeatureFile,
			String clusterFile, String edgeFile, String which, boolean directed){
		try{
			
			this.directed = directed;
			Util util = new Util();
			
			Map<Integer, List<Integer>> nodeFeatures = new HashMap<Integer, List<Integer>>();
			Map<Integer, List<Integer>> simFeatures = new HashMap<Integer, List<Integer>>();
			
			List<Integer> selfFeatures;
			
			File f = null;
			
			File f2 = new File(nodeFeatureFile);
			if(!f2.exists()){
				System.out.println("Couldn't open " + nodeFeatureFile);
				return false;
			}
			
			int i = 0;
			
			Scanner sc = new Scanner(f2);
			
			while(sc.hasNextLine()){
				String[] arr = sc.nextLine().split(" ");
				
				String nodeid = arr[0];
				
				if(nodeIndex.containsKey(nodeid)){
					System.out.println("Got duplicate feature for " + nodeid);
				}
				
				nodeIndex.put(nodeid, i);
				indexNode.put(i, nodeid);
				nNodeFeatures = arr.length-1;
				List<Integer> features = new ArrayList<Integer>();
				
				for(int j = 1; j < arr.length; j++){
					features.add(Integer.parseInt(arr[j]));
				}
				nodeFeatures.put(i, features);
				i++;
			}
			
			nNodes = i;
			
			if(nNodes > 1200){
				System.out.println("This code will probably run out of memory with more than 1000 nodes");
				sc.close();
				return false;
			}
			
			f = new File(selfFeatureFile);
			selfFeatures = new ArrayList<Integer>();
			sc.close();
			sc = new Scanner(f);
			
			for(int k = 0; k < nNodeFeatures; k++){
				selfFeatures.add(sc.nextInt());
			}
			
			for(int k = 0; k < nNodes; k++){
				List<Integer> feature = util.diff(selfFeatures, nodeFeatures.get(k), nNodeFeatures);
				simFeatures.put(k, feature);
			}
			
			f = new File(clusterFile);
			
			sc.close();
			
			sc = new Scanner(f);
			
			while(sc.hasNextLine()){
				
				String[] arr = sc.nextLine().split("\t");
				Set<Integer> circle = new HashSet<Integer>();
				
				for(int k = 1; k < arr.length; k++){
					String nodeId = arr[k];
					
					if(nodeIndex.containsKey(nodeId)){
						circle.add(nodeIndex.get(nodeId));
					} else {
						System.out.println("Got unknown entry in label file: " + nodeId);
					}
				}
				clusters.add(circle);
			}
			
			nEdgeFeatures = nNodeFeatures + 1;
			
			if(which.equals("BOTH")){
				nEdgeFeatures += nNodeFeatures;
			}
			
			for(int m = 0; m < nNodes; m++){
				int mn = 0;
				mn++;
				for(int n = (directed? 0 : m+1); n < nNodes; n++){
					if(m == n)
						continue;
					int ind = 0;
					int[] d = new int[nEdgeFeatures];
					
					for(int o = 0; o < d.length; o++){
						d[o] = -1;
					}
					
					d[0] = 1;
					ind++;
					
					List<Integer> list;
					List<Integer> list2 = new ArrayList<Integer>();
					
					if(which.equals("EGOFEATURES")){
						list = util.diff(simFeatures.get(m),simFeatures.get(n), nNodeFeatures);
						
					} else if(which.equals("FRIENDFEATURES")){
						list = util.diff(nodeFeatures.get(m), nodeFeatures.get(n), nNodeFeatures);
					} else {
						list = util.diff(simFeatures.get(m),simFeatures.get(n), nNodeFeatures);
						list2 = util.diff(nodeFeatures.get(m), nodeFeatures.get(n), nNodeFeatures);
					}
					
					for(int val : list){
						d[ind] = val;
						ind++;
					}
					
					ind += nNodeFeatures;
					
					for(int val : list2){
							d[ind] = val;
					}
					
					edgeFeatures.put(new Pair<Integer, Integer>(m,n), util.makeSparse(d, nEdgeFeatures));
				}
			}
			
			sc.close();
			
			f = new File(edgeFile);
			
			sc = new Scanner(f);
			
			while(sc.hasNextLine()){
				String src = sc.next();
				String dest = sc.next();
				sc.nextLine();
				edgeSet.add(new Pair<Integer, Integer>(nodeIndex.get(src), nodeIndex.get(dest)));
			}
			
			sc.close();
			return true;
			
		} catch(Exception ex){
			System.out.println(ex.getMessage());
			return false;
		}
		
	}
	
	public Node removeNode(int nodeid){
		
		Node node = new Node();
		
		for(int i = 0; i < clusters.size(); i++){
			if(clusters.get(i).contains(nodeid))
				clusters.get(i).remove(nodeid);
			node.circles.add(i);
		}
		
		Iterator<Pair<Integer,Integer>> it = edgeSet.iterator();
		
		while(it.hasNext()){
			Pair<Integer, Integer> pair = it.next();
			if(pair.getFirst() == nodeid || pair.getSecond() == nodeid)
				it.remove();
		}
		
		/*for(Pair<Integer, Integer> pair : edgeSet){
			if(pair.getFirst() == nodeid || pair.getSecond() == nodeid)
				edgeSet.remove(pair);
		}*/
		
		Iterator<Map.Entry<Pair<Integer,Integer>, Map<Integer, Integer>>> it2 = edgeFeatures.entrySet().iterator();
		
		while(it2.hasNext()){
			Map.Entry<Pair<Integer,Integer>, Map<Integer, Integer>> item = (Map.Entry<Pair<Integer,Integer>, Map<Integer, Integer>>)it2.next();
			if(item.getKey().getFirst() == nodeid || item.getKey().getSecond() == nodeid){
				node.edFeatures.put(item.getKey(), edgeFeatures.get(item.getKey()));
				it2.remove();
			}
		}
		
		/*for(Pair<Integer, Integer> pair : edgeFeatures.keySet()){
			if(pair.getFirst() == nodeid || pair.getSecond() == nodeid){
				node.edFeatures.put(pair, edgeFeatures.get(pair));
				edgeFeatures.remove(pair);	
			}
			else {
				for(int key : edgeFeatures.get(pair).keySet()){
					if(key == nodeid)
						edgeFeatures.get(pair).remove(key);
				}	
			}
		}*/
		
		return node;
	}
	
	public static void main(String[] args){
		Graph graph = new Graph();
		
		String filePrefix = "./data/facebook/698"; //
		String nodeFeatureFile = filePrefix + ".feat"; // TODO
		String selfFeatureFile = filePrefix + ".egofeat";
		String clusterFile = filePrefix + ".circles";
		String edgeFile = filePrefix + ".edges";
		String which = "FRIENDFEATURES";
		boolean directed = false;
		
		boolean status = graph.loadGraphData(nodeFeatureFile, selfFeatureFile, 
				clusterFile, edgeFile, which, directed);
		
		graph.removeNode(0);
		
		if (status == false) {
			System.out.println("Could not build graph");
			return;
		}
	}

}
