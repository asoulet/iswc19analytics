package statistics.MPMC;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.jena.query.QuerySolution;
import org.apache.log4j.Logger;

import robusta.lod.SimpleSparqlQuerier;
import robusta.lod.Triplestore;
import robusta.lod.TriplestoreSampler;

public class StatisticsSampler extends TriplestoreSampler {

	private static Logger logger = Logger.getLogger(StatisticsSampler.class);

	public enum WeightTransformation { ID, CONST, LOG};
	
	private long weight = 0;
	private WeightTransformation weightTransformation = WeightTransformation.LOG;
	private long weightingTimeout = 60 * 1000;
	
	public StatisticsSampler(String filename) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(filename));
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		weightTransformation = WeightTransformation.valueOf(properties.getProperty("weight_transformation"));
		weightingTimeout = Long.parseLong(properties.getProperty("weighting_timeout")) * 1000;
	}

	@Override
	public long getWeight(Triplestore triplestore) {
		weight = 0;
		try {
			SimpleSparqlQuerier q = new SimpleSparqlQuerier("select (count(*) as ?count) where {?s ?p ?o}", triplestore) {
				
				@Override
				public boolean fact(QuerySolution qs) {
					if (qs.get("count") != null && qs.get("count").asLiteral() != null) {
						weight = qs.get("count").asLiteral().getInt();
						return true;
					}
					return false;
				}
			};
			q.safeExecute(weightingTimeout);
		} catch (InterruptedException e) {
			logger.error(e);
		}
		return weight;
	}

	@Override
	public double getDrawnWeight(long weight) {
		switch (weightTransformation) {
		case ID:
			return weight;
		case CONST:
			return 1;
		case LOG: 
			if (weight == 0)
				return 0;
			else
				return Math.log(weight);
		}
		return 0;
	}

}
