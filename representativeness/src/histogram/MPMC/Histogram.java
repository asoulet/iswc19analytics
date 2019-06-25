package histogram.MPMC;

import java.util.HashMap;
import java.util.Map.Entry;

public class Histogram {
	
	private HashMap<Integer, Integer> counters = new HashMap<>();
	
	public void add(int cardinality) {
		Integer counter = counters.get(cardinality);
		if (counter == null)
			counter = Integer.valueOf(0);
		counter++;
		counters.put(cardinality, counter);		
	}
	
	public void show() {
		for (Entry<Integer, Integer> c : counters.entrySet())
			System.out.print(c.getKey() + ":" + c.getValue() + " ");
		System.out.println();
	}

}
