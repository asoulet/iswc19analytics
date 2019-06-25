package histogram.MPMC;

import java.util.concurrent.BlockingQueue;

import org.apache.jena.query.QuerySolution;
import robusta.consumer.Pattern;
import robusta.lod.SimpleSparqlQuerier;
import robusta.lod.Triplestore;
import robusta.lod.TriplestoreSample;
import robusta.lod.TriplestoreSampler;
import robusta.producer.PatternProducer;

public class RCCProducer extends PatternProducer {

	//private static Logger logger = Logger.getLogger(RCCProducer.class);

	public RCCProducer(BlockingQueue<Pattern> queue, TriplestoreSampler sampler) {
		super(queue, sampler);
	}

	@Override
	public void draw(TriplestoreSample sample) throws InterruptedException {
		Triplestore triplestore = sample.triplestore;
		long index = sample.index;
		double weight = sample.weight;
		new SimpleSparqlQuerier("select ?r (count(*) as ?count) where {?x ?r ?s. {select ?s where {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o} LIMIT 1000 OFFSET " + index + "}} group by ?s ?r", triplestore) {
			
			@Override
			public boolean fact(QuerySolution qs) throws InterruptedException {
				if (qs.get("r") != null && qs.get("r").asResource() != null) {
					String r = qs.get("r").asResource().toString();
					int count = qs.get("count").asLiteral().getInt();
					consume(new RelationCardinalityCounter(r, count, weight));
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
