package edu.ncsu.snap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {
	
	public Map<Integer, List<Integer>> makeSparse(List<List<Integer>> list, int[] d, int n){
		Map<Integer, List<Integer>> res = new HashMap<Integer, List<Integer>>();
		
		return res;
	}
	
	public List<Integer> diff(List<Integer> f1, List<Integer> f2, int D){
		List<Integer> res = new ArrayList<Integer>();
		
		for(int i = 0; i < D; i++){
			res.add(Math.abs(f1.get(i) - f2.get(i)));
		}
		
		return res;
	}

}
