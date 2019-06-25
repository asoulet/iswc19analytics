package statistics.MPMC;

import robusta.consumer.Pattern;
import robusta.lod.Triplestore;

public class StatisticsPattern extends Pattern {

	private String subject;
	private Triplestore triplestore;
	private double weight;
	private String property;
	private String object;
	
	public StatisticsPattern(String s, String p, String o, Triplestore triplestore, double weight) {
		this.subject = s;
		this.property = p;
		this.object = o;
		this.triplestore = triplestore;
		this.weight = weight;
	}

	public String getSubject() {
		return subject;
	}

	public Triplestore getTriplestore() {
		return triplestore;
	}

	public double getWeight() {
		return weight;
	}

	public String getProperty() {
		return property;
	}

	public String getObject() {
		return object;
	}

	@Override
	public String toString() {
		return "StatisticsPattern [subject=" + subject + ", property=" + property + ", object=" + object
				+ ", triplestore=" + triplestore + "]";
	}


}
