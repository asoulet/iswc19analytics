package histogram;

import java.util.concurrent.BlockingQueue;

import histogram.MPMC.HistogramMonitor;
import histogram.MPMC.RCCDatabaseConsumer;
import histogram.MPMC.RCCProducer;
import histogram.MPMC.RCCSampler;
import histogram.representativeness.RepresentativenessAnalyzer;
import robusta.MPMCFactory;
import robusta.Robusta;
import robusta.RobustaClient;
import robusta.RobustaCommand;
import robusta.RobustaConfiguration;
import robusta.consumer.Pattern;
import robusta.consumer.PatternConsumer;
import robusta.lod.Triplestore;
import robusta.lod.TriplestoreSampler;
import robusta.producer.PatternProducer;

public class TestHistogram {

	public static void main(String[] args) {
		Robusta robusta = new Robusta(new MPMCFactory() {
			
			private TriplestoreSampler sampler = null;
			
			@Override
			public PatternProducer newProducer(BlockingQueue<Pattern> queue, TriplestoreSampler sampler) {
				return new RCCProducer(queue, sampler);
			}
			
			@Override
			public PatternConsumer newConsumer(BlockingQueue<Pattern> queue) {
				return new RCCDatabaseConsumer(queue, "properties/histogram.properties");
			}

			@Override
			public TriplestoreSampler getTriplestoreSampler() {
				if (sampler == null)
					sampler = new RCCSampler("properties/histogram.properties");
				return sampler;
			}

		}, new RobustaConfiguration("properties/robusta_histogram.properties"));
		
		robusta.addTriplestore(new Triplestore("http://dbpedia.org/sparql"));
				
		robusta.addMonitor(new HistogramMonitor("properties/histogram.properties"));
		robusta.addMonitor(new RepresentativenessAnalyzer("properties/histogram.properties"));
		
		robusta.open();
		
		RobustaClient client = new RobustaClient(new RobustaConfiguration("properties/robusta_histogram.properties"));

		client.send(RobustaCommand.START);
		try {
			Thread.sleep(60 * 60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		client.send(RobustaCommand.STOP);
		client.send(RobustaCommand.CLOSE);
	}

}
