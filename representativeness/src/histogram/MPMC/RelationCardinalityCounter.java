package histogram.MPMC;

import robusta.consumer.Pattern;

public class RelationCardinalityCounter extends Pattern {

	private String relation;
	private int cardinality;
	private double weight;
	
	public RelationCardinalityCounter(String relation, int cardinality, double weight) {
		this.relation = relation;
		this.cardinality = cardinality;
		this.weight = weight;
	}

	public String getRelation() {
		return relation;
	}

	public int getCardinality() {
		return cardinality;
	}

	public double getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return "[relation=" + relation + ", cardinality=" + cardinality + ", weight=" + weight + "]";
	}

}
