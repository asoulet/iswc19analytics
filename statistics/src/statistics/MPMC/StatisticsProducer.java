package statistics.MPMC;

import java.util.concurrent.BlockingQueue;

import org.apache.jena.query.QuerySolution;
import org.apache.log4j.Logger;

import robusta.consumer.Pattern;
import robusta.lod.SimpleSparqlQuerier;
import robusta.lod.Triplestore;
import robusta.lod.TriplestoreSample;
import robusta.lod.TriplestoreSampler;
import robusta.producer.PatternProducer;

public class StatisticsProducer extends PatternProducer {

	//private static Logger logger = Logger.getLogger(StatisticsProducer.class);

	public StatisticsProducer(BlockingQueue<Pattern> queue, TriplestoreSampler sampler) {
		super(queue, sampler);
	}

	@Override
	public void draw(TriplestoreSample sample) throws InterruptedException {
		Triplestore triplestore = sample.triplestore;
		long index = sample.index;
		double weight = sample.weight;
		String queryStr = "select ?s ?p ?o where {select ?s ?p ?o where {?s ?p ?o} LIMIT 1 OFFSET " + index + "}";
		new SimpleSparqlQuerier(queryStr, triplestore) {
			
			@Override
			public boolean fact(QuerySolution qs) throws InterruptedException {
				if (qs.get("s") != null && qs.get("s").asResource() != null) {
					String s = qs.get("s").asResource().toString();
					String p = qs.get("p").asResource().toString();
					String o = "";
					if (qs.get("o").isResource())
						o = qs.get("o").asResource().toString();
					else
						o = qs.get("o").asLiteral().toString();
					consume(new StatisticsPattern(s, p, o, triplestore, weight));
					return true;
				}
				return false;
			}			
		}.execute();
	}

	@Override
	protected void begin() {
	}

	@Override
	protected void end() {
	}


}
