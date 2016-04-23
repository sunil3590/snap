package edu.ncsu.snap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Computes similarity metrics between edges and nodes based on the corresponding features
//and implements common data preprocessing methods
public class Util {

	public Map<Integer, Integer> makeSparse(int[] d, int n){
		//Data preprocessing to convert an array into a hashmap
		Map<Integer, Integer> res = new HashMap<Integer, Integer>();
		
		for(int i = 0; i < d.length; i++){
			if(d[i] != -1){
				res.put(i, d[i]);
			}
		}
		return res;
	}

	public List<Integer> diff(List<Integer> f1, List<Integer> f2, int D) {
		//Computes similarity values between the nodes and edges with the passed feature set
		List<Integer> res = new ArrayList<Integer>();

		for (int i = 0; i < D; i++) {
			res.add(Math.abs(f1.get(i) - f2.get(i)));
		}

		return res;
	}

	// Inner product
	public static double inp(Map<Integer,Integer> phi, double theta[])
	{
		double res = 0;
		for (Map.Entry<Integer,Integer> entry : phi.entrySet()) {
			res += entry.getValue() * theta[entry.getKey()];
		}
		return res;
	}
	
}
